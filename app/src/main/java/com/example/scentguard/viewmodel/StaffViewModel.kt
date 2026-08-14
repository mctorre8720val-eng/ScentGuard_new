package com.example.scentguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scentguard.data.model.Restaurant
import com.example.scentguard.data.model.UserProfile
import com.example.scentguard.data.repository.UserRepository
import com.example.scentguard.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StaffViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _staffList = MutableStateFlow<Resource<List<UserProfile>>>(Resource.Idle())
    val staffList: StateFlow<Resource<List<UserProfile>>> = _staffList

    private val _restaurantInfo = MutableStateFlow<Resource<Restaurant>>(Resource.Idle())
    val restaurantInfo: StateFlow<Resource<Restaurant>> = _restaurantInfo

    private val _isRefreshingCode = MutableStateFlow(false)
    val isRefreshingCode: StateFlow<Boolean> = _isRefreshingCode

    private val _removalState = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val removalState: StateFlow<Resource<Unit>> = _removalState

    fun fetchStaff(restaurantId: String) {
        viewModelScope.launch {
            _staffList.value = Resource.Loading()
            val result = userRepository.getStaffByRestaurant(restaurantId)
            result.onSuccess {
                _staffList.value = Resource.Success(it)
            }.onFailure {
                _staffList.value = Resource.Error(it.message ?: "Failed to fetch staff")
            }
        }
    }

    fun fetchRestaurantInfo(restaurantId: String) {
        viewModelScope.launch {
            _restaurantInfo.value = Resource.Loading()
            val result = userRepository.getRestaurantById(restaurantId)
            result.onSuccess { restaurant ->
                if (restaurant != null) {
                    _restaurantInfo.value = Resource.Success(restaurant)
                } else {
                    _restaurantInfo.value = Resource.Error("Restaurant not found")
                }
            }.onFailure {
                _restaurantInfo.value = Resource.Error(it.message ?: "Error fetching restaurant")
            }
        }
    }

    fun refreshInviteCode(restaurantId: String) {
        viewModelScope.launch {
            _isRefreshingCode.value = true
            val result = userRepository.refreshInviteCode(restaurantId)
            if (result.isSuccess) {
                fetchRestaurantInfo(restaurantId)
            }
            _isRefreshingCode.value = false
        }
    }

    fun removeStaff(uid: String, restaurantId: String) {
        viewModelScope.launch {
            _removalState.value = Resource.Loading()
            val result = userRepository.removeStaffFromRestaurant(uid)
            if (result.isSuccess) {
                _removalState.value = Resource.Success(Unit)
                fetchStaff(restaurantId)
            } else {
                _removalState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Failed to remove staff")
            }
        }
    }
    
    fun resetRemovalState() {
        _removalState.value = Resource.Idle()
    }
}
