package com.aics.violationapp.data.repository

import com.aics.violationapp.data.api.ApiService
import com.aics.violationapp.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ViolationRepository(
    private val apiService: ApiService
) {
    
    suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { user ->
                    Result.success(user)
                } ?: Result.failure(Exception("User data is null"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(username: String, email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(RegisterRequest(username, email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { user ->
                    Result.success(user)
                } ?: Result.failure(Exception("User data is null"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchStudent(studentId: String): Result<Student> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchStudent(studentId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { student ->
                    Result.success(student)
                } ?: Result.failure(Exception("Student not found"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Student not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getViolationTypes(): Result<List<ViolationType>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getViolationTypes()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { types ->
                    Result.success(types)
                } ?: Result.failure(Exception("No violation types found"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load violation types"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun submitViolation(studentId: String, violations: List<String>, recordedBy: String): Result<ViolationResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.submitViolation(ViolationRequest(studentId, violations, recordedBy))
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { violationResponse ->
                    Result.success(violationResponse)
                } ?: Result.failure(Exception("Failed to submit violation"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to submit violation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getOffenseCounts(studentId: String): Result<Map<String, Int>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getOffenseCounts(studentId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { offenseCounts ->
                    Result.success(offenseCounts)
                } ?: Result.failure(Exception("Offense counts data is null"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get offense counts"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.testConnection()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: "Connection successful")
            } else {
                Result.failure(Exception(response.body()?.message ?: "Connection failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
