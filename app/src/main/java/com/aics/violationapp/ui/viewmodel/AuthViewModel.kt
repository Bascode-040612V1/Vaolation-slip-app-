package com.aics.violationapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aics.violationapp.data.model.User
import com.aics.violationapp.data.repository.ViolationRepository
import com.aics.violationapp.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isLoginMode: Boolean = true,
    val rfidNumber: String? = null,
    val isRfidLoading: Boolean = false
)

class AuthViewModel(
    private val repository: ViolationRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        val user = preferencesManager.getUser()
        _uiState.value = _uiState.value.copy(
            isLoggedIn = preferencesManager.isLoggedIn(),
            user = user
        )
    }
    
    fun toggleAuthMode() {
        _uiState.value = _uiState.value.copy(
            isLoginMode = !_uiState.value.isLoginMode,
            error = null
        )
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.login(email, password)
                .onSuccess { user ->
                    preferencesManager.saveUser(user)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        user = user,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }
    
    fun register(username: String, email: String, password: String, rfid: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.register(username, email, password, rfid)
                .onSuccess { user ->
                    preferencesManager.saveUser(user)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        user = user,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }
    
    fun refreshRfidNumber() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRfidLoading = true)
            
            repository.getRfidNumber()
                .onSuccess { rfidNumber ->
                    _uiState.value = _uiState.value.copy(
                        isRfidLoading = false,
                        rfidNumber = rfidNumber,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isRfidLoading = false,
                        error = exception.message
                    )
                }
        }
    }
    
    fun logout() {
        preferencesManager.logout()
        _uiState.value = _uiState.value.copy(
            isLoggedIn = false,
            user = null,
            error = null
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
