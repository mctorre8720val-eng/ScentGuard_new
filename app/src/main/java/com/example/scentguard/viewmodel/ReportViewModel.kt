package com.example.scentguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scentguard.data.model.ChartData
import com.example.scentguard.data.model.ReportSummary
import com.example.scentguard.data.repository.AuthRepository
import com.example.scentguard.data.repository.ChartRepository
import com.example.scentguard.data.repository.ReportRepository
import com.example.scentguard.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReportViewModel(
    private val reportRepository: ReportRepository,
    private val chartRepository: ChartRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _reportState = MutableStateFlow<Resource<ReportSummary>>(Resource.Idle())
    val reportState: StateFlow<Resource<ReportSummary>> = _reportState

    private val _chartState = MutableStateFlow<Resource<ChartData>>(Resource.Idle())
    val chartState: StateFlow<Resource<ChartData>> = _chartState

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.userSession.collectLatest { session ->
                if (session != null) {
                    fetchDailyReport(session.restaurantId)
                    fetchChartData(session.restaurantId)
                } else {
                    _reportState.value = Resource.Idle()
                    _chartState.value = Resource.Idle()
                }
            }
        }
    }

    fun fetchChartData(restaurantId: String? = null) {
        val rid = restaurantId ?: authRepository.userSession.value?.restaurantId ?: return
        viewModelScope.launch {
            _chartState.value = Resource.Loading()
            val result = chartRepository.getGasLevelHistory(rid)
            result.onSuccess {
                _chartState.value = Resource.Success(it)
            }.onFailure {
                _chartState.value = Resource.Error(it.message ?: "Failed to load chart")
            }
        }
    }

    fun fetchDailyReport(restaurantId: String? = null) {
        val rid = restaurantId ?: authRepository.userSession.value?.restaurantId ?: return
        viewModelScope.launch {
            _reportState.value = Resource.Loading()
            val result = reportRepository.getDailyReport(rid)
            result.onSuccess {
                _reportState.value = Resource.Success(it)
            }.onFailure {
                _reportState.value = Resource.Error(it.message ?: "Failed to load report")
            }
        }
    }

    fun fetchWeeklyReport(restaurantId: String? = null) {
        val rid = restaurantId ?: authRepository.userSession.value?.restaurantId ?: return
        viewModelScope.launch {
            _reportState.value = Resource.Loading()
            val result = reportRepository.getWeeklyReport(rid)
            result.onSuccess {
                _reportState.value = Resource.Success(it)
            }.onFailure {
                _reportState.value = Resource.Error(it.message ?: "Failed to load report")
            }
        }
    }
}
