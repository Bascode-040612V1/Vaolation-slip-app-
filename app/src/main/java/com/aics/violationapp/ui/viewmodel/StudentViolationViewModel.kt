package com.aics.violationapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aics.violationapp.data.local.ViolationData
import com.aics.violationapp.data.model.Student
import com.aics.violationapp.data.model.ViolationType
import com.aics.violationapp.data.model.ViolationResponse
import com.aics.violationapp.data.repository.ViolationRepository
import com.aics.violationapp.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ViolationUiState(
    val isLoading: Boolean = false,
    val student: Student? = null,
    val violationTypes: List<ViolationType> = emptyList(),
    val selectedViolations: Set<String> = emptySet(),
    val othersText: String = "",
    val error: String? = null,
    val submitResult: ViolationResponse? = null,
    val showConfirmation: Boolean = false,
    val showSuccessMessage: Boolean = false
)

class StudentViolationViewModel(
    private val repository: ViolationRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ViolationUiState())
    val uiState: StateFlow<ViolationUiState> = _uiState.asStateFlow()
    
    // Store consistent offense counts for each violation to prevent random changes
    private val violationOffenseCounts = mutableMapOf<String, Int>()
    // Store real offense counts from database
    private val databaseOffenseCounts = mutableMapOf<String, Int>()
    
    fun loadStudent(studentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Load violation types first (use local data as fallback)
            val violationTypes = try {
                val result = repository.getViolationTypes()
                result.getOrNull() ?: ViolationData.getDefaultViolationTypes()
            } catch (e: Exception) {
                ViolationData.getDefaultViolationTypes()
            }
            
            _uiState.value = _uiState.value.copy(violationTypes = violationTypes)
            
            // Load student data
            repository.searchStudent(studentId)
                .onSuccess { student ->
                    _uiState.value = _uiState.value.copy(
                        student = student,
                        isLoading = false
                    )
                    
                    // Load real offense counts from database
                    loadOffenseCounts(studentId)
                }
                .onFailure { exception ->
                    // For demo purposes, create a mock student if API fails
                    val mockStudent = Student(
                        id = 1,
                        student_id = studentId,
                        student_name = "Demo Student",
                        year_level = "4th Year",
                        course = "BSIT",
                        section = "A"
                    )
                    _uiState.value = _uiState.value.copy(
                        student = mockStudent,
                        isLoading = false,
                        error = "Using demo data - API not available"
                    )
                    
                    // Load real offense counts from database (will fall back to mock if API fails)
                    loadOffenseCounts(studentId)
                }
        }
    }
    
    private fun loadOffenseCounts(studentId: String) {
        viewModelScope.launch {
            repository.getOffenseCounts(studentId)
                .onSuccess { offenseCounts ->
                    databaseOffenseCounts.clear()
                    databaseOffenseCounts.putAll(offenseCounts)
                }
                .onFailure { 
                    // If API fails, keep using the mock system
                }
        }
    }
    
    fun toggleViolationSelection(violation: String) {
        val currentSelections = _uiState.value.selectedViolations.toMutableSet()
        if (currentSelections.contains(violation)) {
            currentSelections.remove(violation)
        } else {
            currentSelections.add(violation)
        }
        _uiState.value = _uiState.value.copy(selectedViolations = currentSelections)
    }
    
    fun updateOthersText(text: String) {
        _uiState.value = _uiState.value.copy(othersText = text)
    }
    
    fun showConfirmation() {
        if (_uiState.value.selectedViolations.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(showConfirmation = true)
        }
    }
    
    fun hideConfirmation() {
        _uiState.value = _uiState.value.copy(showConfirmation = false)
    }
    
    fun submitViolations() {
        viewModelScope.launch {
            val student = _uiState.value.student
            val user = preferencesManager.getUser()
            val violations = _uiState.value.selectedViolations.toList()
            
            if (student != null && user != null && violations.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                repository.submitViolation(student.student_id, violations, user.username)
                    .onSuccess { response ->
                        // Update local offense counts after successful submission
                        violations.forEach { violation ->
                            val nextOffense = getNextOffenseCountForViolation(violation)
                            violationOffenseCounts[violation] = nextOffense
                            // Also update database tracking
                            databaseOffenseCounts[violation] = nextOffense
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            submitResult = response,
                            showConfirmation = false,
                            showSuccessMessage = true,
                            selectedViolations = emptySet(),
                            othersText = ""
                        )
                    }
                    .onFailure { exception ->
                        // For demo purposes, create a mock response using highest offense
                        val highestOffense = getHighestOffenseCount()
                        val mockResponse = ViolationResponse(
                            violation_id = 1,
                            offense_count = highestOffense,
                            penalty = when(highestOffense) {
                                1 -> "Warning"
                                2 -> "Grounding"
                                3 -> "Suspension"
                                else -> "Warning"
                            },
                            message = "Demo violation submitted successfully - ${getOffenseMessage()}"
                        )
                        
                        // Update local tracking for demo
                        violations.forEach { violation ->
                            val nextOffense = getNextOffenseCountForViolation(violation)
                            violationOffenseCounts[violation] = nextOffense
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            submitResult = mockResponse,
                            showConfirmation = false,
                            showSuccessMessage = true,
                            selectedViolations = emptySet(),
                            othersText = "",
                            error = "Using demo data - API not available"
                        )
                    }
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            submitResult = null,
            showSuccessMessage = false
        )
        // Don't clear offense counts here - they should persist for proper tracking
        // Only clear when starting a fresh session or after successful submission
    }
    
    fun getViolationsByCategory(): Map<String, List<ViolationType>> {
        return _uiState.value.violationTypes.groupBy { it.category }
    }
    
    fun getOffenseCount(violation: String): Int {
        return if (_uiState.value.selectedViolations.contains(violation)) {
            // Get current offense count for this specific violation type
            // Priority: database > local tracking > default (1)
            getNextOffenseCountForViolation(violation)
        } else {
            // If violation is not selected, return 0
            0
        }
    }
    
    private fun getNextOffenseCountForViolation(violation: String): Int {
        // First try to get the NEXT offense count from database (current + 1)
        val currentFromDatabase = databaseOffenseCounts[violation] ?: 0
        
        if (currentFromDatabase > 0) {
            // Calculate next offense count with cycling (1 -> 2 -> 3 -> 1)
            val nextOffense = currentFromDatabase + 1
            return if (nextOffense > 3) 1 else nextOffense
        }
        
        // If no database record, use consistent local tracking
        // This ensures the same violation always shows the same offense count
        return violationOffenseCounts.getOrPut(violation) {
            // First time selecting this violation - start with 1st offense
            1
        }
    }
    
    fun getHighestOffenseCount(): Int {
        // Get the highest offense count among all selected violations
        val selectedViolations = _uiState.value.selectedViolations
        if (selectedViolations.isEmpty()) return 0
        
        return selectedViolations.maxOfOrNull { violation ->
            getOffenseCount(violation)
        } ?: 0
    }
    
    fun getOffenseMessage(): String {
        val highestOffense = getHighestOffenseCount()
        return when (highestOffense) {
            1 -> "1st Offense"
            2 -> "2nd Offense" 
            3 -> "3rd Offense"
            else -> ""
        }
    }
}
