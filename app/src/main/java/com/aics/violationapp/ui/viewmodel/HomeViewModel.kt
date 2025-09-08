package com.aics.violationapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aics.violationapp.data.model.Student
import com.aics.violationapp.data.repository.ViolationRepository
import com.aics.violationapp.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchResult: Student? = null,
    val connectionTestResult: String? = null
)

class HomeViewModel(
    private val repository: ViolationRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    fun searchStudent(studentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.searchStudent(studentId)
                .onSuccess { student ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        searchResult = student,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Student not found. Please make sure the student is registered.",
                        searchResult = null
                    )
                }
        }
    }
    
    fun testDatabaseConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.testConnection()
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        connectionTestResult = "Database connection successful!",
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        connectionTestResult = "Database connection failed: ${exception.message}",
                        error = exception.message
                    )
                }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            connectionTestResult = null,
            searchResult = null
        )
    }
}
