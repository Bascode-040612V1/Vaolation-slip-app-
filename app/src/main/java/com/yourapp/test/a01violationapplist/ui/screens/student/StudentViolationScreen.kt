package com.yourapp.test.a01violationapplist.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yourapp.test.a01violationapplist.data.network.NetworkModule
import com.yourapp.test.a01violationapplist.data.repository.ViolationRepository
import com.yourapp.test.a01violationapplist.ui.theme.*
import com.yourapp.test.a01violationapplist.ui.viewmodel.StudentViolationViewModel
import com.yourapp.test.a01violationapplist.utils.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentViolationScreen(
    studentId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val viewModel: StudentViolationViewModel = viewModel {
        StudentViolationViewModel(
            ViolationRepository(NetworkModule.provideRetrofit(preferencesManager.getBaseUrl())),
            preferencesManager
        )
    }
    
    val uiState by viewModel.uiState.collectAsState()
    val user = preferencesManager.getUser()
    
    LaunchedEffect(studentId) {
        viewModel.loadStudent(studentId)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Blue80, // top = strong blue
                        PrimaryBlue      // bottom = lighter blue
                    )
                )
            )
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryBlue
                        )
                    }
                    
                    Text(
                        text = "Student Violations",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Student Data Card
                    item {
                        uiState.student?.let { student ->
                            StudentDataCard(student = student)
                        }
                    }
                    
                    // Violation List Title
                    item {
                        Text(
                            text = "VIOLATION LIST",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    // Violation Categories
                    val violationsByCategory = viewModel.getViolationsByCategory()
                    
                    // Dress Code Violations
                    item {
                        ViolationCategorySection(
                            title = "DRESS CODE VIOLATION",
                            violations = violationsByCategory["Dress Code"] ?: emptyList(),
                            selectedViolations = uiState.selectedViolations,
                            onViolationToggle = { viewModel.toggleViolationSelection(it) },
                            getOffenseCount = { viewModel.getOffenseCount(it) }
                        )
                    }
                    
                    // Conduct Violations
                    item {
                        ViolationCategorySection(
                            title = "CONDUCT VIOLATION",
                            violations = violationsByCategory["Conduct"] ?: emptyList(),
                            selectedViolations = uiState.selectedViolations,
                            onViolationToggle = { viewModel.toggleViolationSelection(it) },
                            getOffenseCount = { viewModel.getOffenseCount(it) },
                            othersText = uiState.othersText,
                            onOthersTextChange = { viewModel.updateOthersText(it) }
                        )
                    }
                    
                    // Minor Offenses
                    item {
                        ViolationCategorySection(
                            title = "MINOR OFFENSE",
                            violations = violationsByCategory["Minor"] ?: emptyList(),
                            selectedViolations = uiState.selectedViolations,
                            onViolationToggle = { viewModel.toggleViolationSelection(it) },
                            getOffenseCount = { viewModel.getOffenseCount(it) }
                        )
                    }
                    
                    // Major Offenses
                    item {
                        ViolationCategorySection(
                            title = "MAJOR OFFENSE",
                            violations = violationsByCategory["Major"] ?: emptyList(),
                            selectedViolations = uiState.selectedViolations,
                            onViolationToggle = { viewModel.toggleViolationSelection(it) },
                            getOffenseCount = { viewModel.getOffenseCount(it) }
                        )
                    }
                    
                    // Recorded By
                    item {
                        Text(
                            text = "Recorded By: ${user?.username ?: "Unknown"}",
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                    
                    // Send Button
                    item {
                        Button(
                            onClick = { viewModel.showConfirmation() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = uiState.selectedViolations.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Send Violations",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                    
                    // Error message
                    item {
                        uiState.error?.let { error ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = ErrorRed.copy(alpha = 0.1f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = error,
                                    color = ErrorRed,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    
                    // Add bottom padding
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
        
        // Confirmation Dialog
        if (uiState.showConfirmation) {
            ConfirmationDialog(
                selectedViolations = uiState.selectedViolations.toList(),
                highestOffenseCount = viewModel.getHighestOffenseCount(),
                offenseMessage = viewModel.getOffenseMessage(),
                onConfirm = { viewModel.submitViolations() },
                onDismiss = { viewModel.hideConfirmation() }
            )
        }
        
        // Success Message Dialog
        if (uiState.showSuccessMessage) {
            uiState.submitResult?.let { result ->
                SuccessMessageDialog(
                    studentName = uiState.student?.student_name ?: "",
                    violationResponse = result,
                    violations = uiState.selectedViolations.toList(),
                    onDismiss = { viewModel.clearMessages() }
                )
            }
        }
    }
}