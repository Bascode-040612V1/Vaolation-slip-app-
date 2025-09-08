package com.yourapp.test.a01violationapplist.data.local

import com.yourapp.test.a01violationapplist.data.model.ViolationType

object ViolationData {
    
    fun getDefaultViolationTypes(): List<ViolationType> {
        return listOf(
            // Dress Code Violations
            ViolationType(1, "No ID", "Dress Code", true),
            ViolationType(2, "Wearing of rubber slippers", "Dress Code", true),
            ViolationType(3, "Improper wearing of uniform", "Dress Code", true),
            ViolationType(4, "Non-prescribed haircut", "Dress Code", true),
            ViolationType(5, "Wearing of earring", "Dress Code", true),
            ViolationType(6, "Wearing of multiple earrings", "Dress Code", true),
            
            // Conduct Violations
            ViolationType(7, "Cutting Classes", "Conduct", true),
            ViolationType(8, "Cheating/Academic Dishonesty", "Conduct", true),
            ViolationType(9, "Theft/Stealing", "Conduct", true),
            ViolationType(10, "Inflicting/Direct Assault", "Conduct", true),
            ViolationType(11, "Gambling", "Conduct", true),
            ViolationType(12, "Smoking within the school vicinity", "Conduct", true),
            ViolationType(13, "Possession/Use of Prohibited Drugs", "Conduct", true),
            ViolationType(14, "Possession/Use of Liquor/Alcoholic Beverages", "Conduct", true),
            ViolationType(15, "Others", "Conduct", true),
            
            // Minor Offenses
            ViolationType(16, "Using cellphones/ gadgets during class hours", "Minor", true),
            ViolationType(17, "Eating inside the laboratories", "Minor", true),
            ViolationType(18, "Improper not wearing/ tampering of ID", "Minor", true),
            ViolationType(19, "Improper hairstyle", "Minor", true),
            ViolationType(20, "Improper Uniform", "Minor", true),
            
            // Major Offenses
            ViolationType(21, "Stealing", "Major", true),
            ViolationType(22, "Vandalism", "Major", true),
            ViolationType(23, "Verbal assault", "Major", true),
            ViolationType(24, "Organizing, planning or joining to any group or fraternity activity", "Major", true)
        )
    }
    
    fun getPenaltyMatrix(): Map<String, Map<Int, String>> {
        return mapOf(
            // Dress Code Violations
            "No ID" to mapOf(1 to "Warning", 2 to "Grounding", 3 to "Suspension"),
            "Wearing of rubber slippers" to mapOf(1 to "Warning", 2 to "Grounding", 3 to "Suspension"),
            "Improper wearing of uniform" to mapOf(1 to "Warning", 2 to "Grounding", 3 to "Suspension"),
            "Non-prescribed haircut" to mapOf(1 to "Warning", 2 to "Grounding", 3 to "Suspension"),
            "Wearing of earring" to mapOf(1 to "Warning", 2 to "Grounding", 3 to "Suspension"),
            "Wearing of multiple earrings" to mapOf(1 to "Warning", 2 to "Grounding", 3 to "Suspension"),
            
            // Conduct Violations
            "Cutting Classes" to mapOf(1 to "Warning", 2 to "Grounding", 3 to "Suspension"),
            "Cheating/Academic Dishonesty" to mapOf(1 to "Suspension", 2 to "Probation", 3 to "Expulsion"),
            "Theft/Stealing" to mapOf(1 to "Suspension", 2 to "Non-readmission", 3 to "Expulsion"),
            "Inflicting/Direct Assault" to mapOf(1 to "Suspension", 2 to "Non-readmission", 3 to "Expulsion"),
            "Gambling" to mapOf(1 to "Probation", 2 to "Suspension", 3 to "Expulsion"),
            "Smoking within the school vicinity" to mapOf(1 to "Grounding", 2 to "Suspension", 3 to "Expulsion"),
            "Possession/Use of Prohibited Drugs" to mapOf(1 to "Suspension", 2 to "Non-readmission", 3 to "Expulsion"),
            "Possession/Use of Liquor/Alcoholic Beverages" to mapOf(1 to "Suspension", 2 to "Non-readmission", 3 to "Expulsion"),
            "Others" to mapOf(1 to "Warning/Probation", 2 to "Suspension", 3 to "Expulsion"),
            
            // Minor Offenses
            "Using cellphones/ gadgets during class hours" to mapOf(1 to "Warning", 2 to "Grounding", 3 to "Probation"),
            "Eating inside the laboratories" to mapOf(1 to "Warning", 2 to "Grounding", 3 to "Community service"),
            "Improper not wearing/ tampering of ID" to mapOf(1 to "Warning", 2 to "Grounding", 3 to "Probation"),
            "Improper hairstyle" to mapOf(1 to "Warning", 2 to "Grounding", 3 to "Suspension"),
            "Improper Uniform" to mapOf(1 to "Warning", 2 to "Grounding", 3 to "Suspension"),
            
            // Major Offenses
            "Stealing" to mapOf(1 to "Suspension", 2 to "Non-readmission", 3 to "Expulsion"),
            "Vandalism" to mapOf(1 to "Suspension", 2 to "Community service + Probation", 3 to "Expulsion"),
            "Verbal assault" to mapOf(1 to "Probation", 2 to "Suspension", 3 to "Non-readmission"),
            "Organizing, planning or joining to any group or fraternity activity" to mapOf(1 to "Suspension", 2 to "Non-readmission", 3 to "Expulsion")
        )
    }
}