package com.yourapp.test.a01violationapplist.ui.screens.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yourapp.test.a01violationapplist.data.model.Student
import com.yourapp.test.a01violationapplist.data.model.ViolationType
import com.yourapp.test.a01violationapplist.data.model.ViolationResponse
import com.yourapp.test.a01violationapplist.ui.theme.*

@Composable
fun StudentDataCard(student: Student) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = PrimaryBlue.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = student.student_name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    Text(
                        text = student.student_id,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Divider(
                color = Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = student.course,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryBlue
                    )
                }
                Column {
                    Text(
                        text = "${student.year_level} - ${student.section}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryBlue
                    )
                }
            }
        }
    }
}

@Composable
fun ViolationCategorySection(
    title: String,
    violations: List<ViolationType>,
    selectedViolations: Set<String>,
    onViolationToggle: (String) -> Unit,
    getOffenseCount: (String) -> Int,
    othersText: String = "",
    onOthersTextChange: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            violations.forEach { violation ->
                if (violation.violation_name == "Others") {
                    OthersViolationItem(
                        violation = violation,
                        isSelected = selectedViolations.contains(violation.violation_name) || 
                                   selectedViolations.any { it.startsWith("Others:") },
                        offenseCount = getOffenseCount(violation.violation_name),
                        othersText = othersText,
                        onToggle = { onViolationToggle(violation.violation_name) },
                        onTextChange = onOthersTextChange
                    )
                } else {
                    ViolationItem(
                        violation = violation,
                        isSelected = selectedViolations.contains(violation.violation_name),
                        offenseCount = getOffenseCount(violation.violation_name),
                        onToggle = { onViolationToggle(violation.violation_name) }
                    )
                }
            }
        }
    }
}

@Composable
fun OthersViolationItem(
    violation: ViolationType,
    isSelected: Boolean,
    offenseCount: Int,
    othersText: String,
    onToggle: () -> Unit,
    onTextChange: (String) -> Unit
) {
    val offenseColor = when (offenseCount) {
        1 -> FirstOffenseGreen
        2 -> SecondOffenseOrange
        3 -> ThirdOffenseRed
        else -> Color.Gray
    }
    
    Card(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryBlue.copy(alpha = 0.1f) else Color.Transparent
        ),
        border = if (isSelected) BorderStroke(1.dp, PrimaryBlue) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggle() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PrimaryBlue
                        )
                    )
                    
                    Column(
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = violation.violation_name,
                            fontSize = 14.sp,
                            color = if (isSelected) PrimaryBlue else Color.White
                        )
                        
                        if (isSelected && offenseCount > 0) {
                            Text(
                                text = "${offenseCount}${when(offenseCount) { 1 -> "st"; 2 -> "nd"; 3 -> "rd"; else -> "th" }} Offense",
                                fontSize = 12.sp,
                                color = offenseColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // Show text input when "Others" is selected
            if (isSelected) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = othersText,
                    onValueChange = onTextChange,
                    label = { Text("Enter violation description") },
                    placeholder = { Text("Describe the violation...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 40.dp), // Align with checkbox text
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue
                    ),
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
fun ViolationItem(
    violation: ViolationType,
    isSelected: Boolean,
    offenseCount: Int,
    onToggle: () -> Unit
) {
    val offenseColor = when (offenseCount) {
        1 -> FirstOffenseGreen
        2 -> SecondOffenseOrange
        3 -> ThirdOffenseRed
        else -> Color.Gray
    }
    
    Card(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryBlue.copy(alpha = 0.1f) else Color.Transparent
        ),
        border = if (isSelected) BorderStroke(1.dp, PrimaryBlue) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryBlue
                    )
                )
                
                Column(
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = violation.violation_name,
                        fontSize = 14.sp,
                        color = if (isSelected) PrimaryBlue else Color.White
                    )
                    
                    if (isSelected && offenseCount > 0) {
                        Text(
                            text = "${offenseCount}${when(offenseCount) { 1 -> "st"; 2 -> "nd"; 3 -> "rd"; else -> "th" }} Offense",
                            fontSize = 12.sp,
                            color = offenseColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    selectedViolations: List<String>,
    highestOffenseCount: Int,
    offenseMessage: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val offenseColor = when (highestOffenseCount) {
        1 -> FirstOffenseGreen
        2 -> SecondOffenseOrange
        3 -> ThirdOffenseRed
        else -> Color.Gray
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Confirm Violations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Edit", color = Color.White)
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Send", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessMessageDialog(
    studentName: String,
    violationResponse: ViolationResponse,
    violations: List<String>,
    onDismiss: () -> Unit
) {
    val offenseIcon = when (violationResponse.offense_count) {
        1 -> "✅"
        2 -> "⚠️"
        3 -> "❌"
        else -> "ℹ️"
    }
    
    val offenseTitle = when (violationResponse.offense_count) {
        1 -> "1st Offense – Warning"
        2 -> "2nd Offense – Disciplinary Reminder"
        3 -> "3rd Offense – Penalty Imposed"
        else -> "Offense Recorded"
    }
    
    val offenseMessage = when (violationResponse.offense_count) {
        1 -> "$studentName received a warning for violations. Please follow school rules to avoid further consequences."
        2 -> "This is $studentName's 2nd offense. Please be reminded that continued violations may lead to suspension or other penalties."
        3 -> "$studentName committed a 3rd offense. Disciplinary action has been taken. Please report to the Guidance Office."
        else -> "Violation has been recorded for $studentName."
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$offenseIcon $offenseTitle",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (violationResponse.offense_count) {
                        1 -> FirstOffenseGreen
                        2 -> SecondOffenseOrange
                        3 -> ThirdOffenseRed
                        else -> PrimaryBlue
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = offenseMessage,
                    fontSize = 14.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Gray.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Violation List:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryBlue
                        )
                        violations.forEach { violation ->
                            Text(
                                text = "• $violation",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        
                        violationResponse.penalty?.let { penalty ->
                            Text(
                                text = "Penalty: $penalty",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = ThirdOffenseRed,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("OK", color = Color.White)
                }
            }
        }
    }
}