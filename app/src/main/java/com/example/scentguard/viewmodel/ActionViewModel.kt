package com.example.scentguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scentguard.data.model.*
import com.example.scentguard.data.repository.AuthRepository
import com.example.scentguard.data.repository.HistoryRepository
import com.example.scentguard.data.repository.UserRepository
import com.example.scentguard.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ActionViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _activeIncident = MutableStateFlow<Resource<Incident?>>(Resource.Idle())
    val activeIncident: StateFlow<Resource<Incident?>> = _activeIncident.asStateFlow()

    private val _sendState = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val sendState: StateFlow<Resource<Unit>> = _sendState.asStateFlow()

    private var incidentListener: ListenerRegistration? = null

    // Observable flow for live telemetry
    @OptIn(ExperimentalCoroutinesApi::class)
    val liveRestaurantData: StateFlow<Restaurant?> = authRepository.userSession
        .flatMapLatest { session ->
            if (session != null) userRepository.getRestaurantFlow(session.restaurantId)
            else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        startRealTimeIncidentListener()
    }

    private fun startRealTimeIncidentListener() {
        viewModelScope.launch {
            authRepository.userSession.collect { session ->
                incidentListener?.remove()
                if (session == null) {
                    _activeIncident.value = Resource.Success(null)
                    return@collect
                }

                _activeIncident.value = Resource.Loading()
                
                // First fetch the latest active incident ID to listen to
                val activeResult = historyRepository.getActiveIncident(session.restaurantId)
                val incident = activeResult.getOrNull()
                
                if (incident != null) {
                    incidentListener = FirebaseFirestore.getInstance()
                        .collection("restaurants")
                        .document(session.restaurantId)
                        .collection("incidents")
                        .document(incident.id)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                _activeIncident.value = Resource.Error(error.message ?: "Sync failed")
                                return@addSnapshotListener
                            }
                            val updated = snapshot?.toObject(Incident::class.java)
                            _activeIncident.value = Resource.Success(updated)
                        }
                } else {
                    _activeIncident.value = Resource.Success(null)
                }
            }
        }
    }

    fun sendResponse(user: UserProfile, incident: Incident, message: String) {
        viewModelScope.launch {
            if (message.isBlank()) return@launch
            
            _sendState.value = Resource.Loading()
            
            val response = StaffAction(
                id = UUID.randomUUID().toString(),
                staffUid = user.uid,
                staffName = user.fullName,
                startTime = Timestamp.now(),
                message = message,
                isResponse = true,
                status = "COMPLETED"
            )

            val result = historyRepository.addStaffResponse(user.restaurantId, incident.id, response)
            if (result.isSuccess) {
                _sendState.value = Resource.Success(Unit)
                
                // Add a log entry for history consistency
                historyRepository.addLogEntry(user.restaurantId, HistoryItem(
                    id = "msg_${response.id}",
                    title = "Staff Response Posted",
                    description = "${user.fullName}: $message",
                    type = HistoryType.INFO,
                    eventType = "STAFF_UPDATE",
                    source = "MANUAL"
                ))
            } else {
                _sendState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Failed to post response")
            }
        }
    }

    fun resetSendState() {
        _sendState.value = Resource.Idle()
    }

    override fun onCleared() {
        incidentListener?.remove()
        super.onCleared()
    }
}
