package com.example.scentguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scentguard.data.model.ChartData
import com.example.scentguard.data.model.ReportSummary
import com.example.scentguard.data.repository.ChartRepository
import com.example.scentguard.data.repository.ReportRepository
import com.example.scentguard.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportViewModel(
    private val reportRepository: ReportRepository = ReportRepository(),
    private val chartRepository: ChartRepository = ChartRepository()
) : ViewModel() {

    private val _reportState = MutableStateFlow<Resource<ReportSummary>>(Resource.Idle())
    val reportState: StateFlow<Resource<ReportSummary>> = _reportState

    private val _chartState = MutableStateFlow<Resource<ChartData>>(Resource.Idle())
    val chartState: StateFlow<Resource<ChartData>> = _chartState

    init {
        fetchDailyReport()
        fetchChartData()
    }

    fun fetchChartData() {
        viewModelScope.launch {
            _chartState.value = Resource.Loading()
            val result = chartRepository.getGasLevelHistory()
            result.onSuccess {
                _chartState.value = Resource.Success(it)
            }.onFailure {
                _chartState.value = Resource.Error(it.message ?: "Failed to load chart")
            }
        }
    }

    fun fetchDailyReport() {
        viewModelScope.launch {
            _reportState.value = Resource.Loading()
            val result = reportRepository.getDailyReport()
            result.onSuccess {
                _reportState.value = Resource.Success(it)
            }.onFailure {
                _reportState.value = Resource.Error(it.message ?: "Failed to load report")
            }
        }
    }

    fun fetchWeeklyReport() {
        viewModelScope.launch {
            _reportState.value = Resource.Loading()
            val result = reportRepository.getWeeklyReport()
            result.onSuccess {
                _reportState.value = Resource.Success(it)
            }.onFailure {
                _reportState.value = Resource.Error(it.message ?: "Failed to load report")
            }
        }
    }
}
