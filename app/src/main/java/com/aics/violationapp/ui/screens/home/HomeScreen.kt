package com.aics.violationapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aics.violationapp.data.network.NetworkModule
import com.aics.violationapp.data.repository.ViolationRepository
import com.aics.violationapp.navigation.Screen
import com.aics.violationapp.ui.theme.Blue80
import com.aics.violationapp.ui.theme.PrimaryBlue
import com.aics.violationapp.ui.viewmodel.HomeViewModel
import com.aics.violationapp.utils.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val homeViewModel: HomeViewModel = viewModel {
        HomeViewModel(
            NetworkModule.provideRepository(context, preferencesManager.getBaseUrl()),
            preferencesManager
        )
    }
    
    val uiState by homeViewModel.uiState.collectAsState()
    var studentNumber by remember { mutableStateOf("") }
    
    // Navigate to student violation screen when student is found
    LaunchedEffect(uiState.searchResult) {
        uiState.searchResult?.let { student ->
            navController.navigate(Screen.StudentViolation.createRoute(student.student_id))
            homeViewModel.clearMessages()
        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Violations App",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // Settings button at the bottom right
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Search Card
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
                        text = "Student Search",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    OutlinedTextField(
                        value = studentNumber,
                        onValueChange = { studentNumber = it },
                        label = { Text("Enter Student Number") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (studentNumber.isNotBlank()) {
                                    homeViewModel.searchStudent(studentNumber.trim())
                                }
                            }
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            if (studentNumber.isNotBlank()) {
                                homeViewModel.searchStudent(studentNumber.trim())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !uiState.isLoading && studentNumber.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Search Student",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                    
                    // Error message
                    uiState.error?.let { error ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // Settings button positioned at bottom right
        IconButton(
            onClick = { navController.navigate(Screen.Settings02.route) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .background(
                    color = Color.Black,
                    shape = RoundedCornerShape(50)
                )
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
