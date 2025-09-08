package com.yourapp.test.a01violationapplist.data.api

import com.yourapp.test.a01violationapplist.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    @POST("auth/login.php")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<User>>
    
    @POST("auth/register.php")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<User>>
    
    @GET("students/search.php")
    suspend fun searchStudent(@Query("student_id") studentId: String): Response<ApiResponse<Student>>
    
    @GET("violations/types.php")
    suspend fun getViolationTypes(): Response<ApiResponse<List<ViolationType>>>
    
    @POST("violations/submit.php")
    suspend fun submitViolation(@Body request: ViolationRequest): Response<ApiResponse<ViolationResponse>>
    
    @GET("violations/student.php")
    suspend fun getStudentViolations(@Query("student_id") studentId: String): Response<ApiResponse<List<Violation>>>
    
    @GET("violations/offense_counts.php")
    suspend fun getOffenseCounts(@Query("student_id") studentId: String): Response<ApiResponse<Map<String, Int>>>
    
    @GET("test/connection.php")
    suspend fun testConnection(): Response<ApiResponse<String>>
}