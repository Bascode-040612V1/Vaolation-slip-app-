package com.aics.violationapp.data.model

data class Student(
    val id: Int = 0,
    val student_id: String = "",
    val student_name: String = "",
    val year_level: String = "",
    val course: String = "",
    val section: String = "",
    val password: String? = null
)

data class Violation(
    val id: Int = 0,
    val student_id: String = "",
    val student_name: String = "",
    val year_level: String = "",
    val course: String = "",
    val section: String = "",
    val offense_count: Int = 1,
    val penalty: String? = null,
    val recorded_by: String = "",
    val recorded_at: String = "",
    val acknowledged: Boolean = false,
    val violations: List<ViolationDetail> = emptyList()
)

data class ViolationDetail(
    val id: Int = 0,
    val violation_id: Int = 0,
    val violation_type: String = "",
    val violation_description: String? = null,
    val message_subject: String? = null,
    val message_body: String? = null
)

data class ViolationType(
    val id: Int = 0,
    val violation_name: String = "",
    val category: String = "",
    val is_active: Boolean = true
)

data class ViolationRequest(
    val student_id: String,
    val violations: List<String>,
    val recorded_by: String
)

data class ViolationResponse(
    val violation_id: Int,
    val offense_count: Int,
    val penalty: String?,
    val message: String
)
