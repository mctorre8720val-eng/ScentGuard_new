package com.example.scentguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scentguard.data.model.Restaurant
import com.example.scentguard.data.model.User
import com.example.scentguard.data.repository.UserRepository
import com.example.scentguard.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StaffViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _staffList = MutableStateFlow<Resource<List<User>>>(Resource.Idle())
    val staffList: StateFlow<Resource<List<User>>> = _staffList

    private val _restaurantInfo = MutableStateFlow<Resource<Restaurant>>(Resource.Idle())
    val restaurantInfo: StateFlow<Resource<Restaurant>> = _restaurantInfo

    fun fetchStaff(restaurantId: String) {
        if (restaurantId.isBlank()) return
        
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
        if (restaurantId.isBlank()) return
        
        viewModelScope.launch {
            _restaurantInfo.value = Resource.Loading()
            val result = userRepository.getRestaurantById(restaurantId)
            result.onSuccess {
                if (it != null) {
                    _restaurantInfo.value = Resource.Success(it)
                } else {
                    _restaurantInfo.value = Resource.Error("Restaurant not found")
                }
            }.onFailure {
                _restaurantInfo.value = Resource.Error(it.message ?: "Failed to fetch restaurant")
            }
        }
    }

    fun removeStaff(uid: String, restaurantId: String) {
        viewModelScope.launch {
            val result = userRepository.removeStaffFromRestaurant(uid)
            if (result.isSuccess) {
                // Refresh list
                fetchStaff(restaurantId)
            }
        }
    }
}
