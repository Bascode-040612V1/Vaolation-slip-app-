package com.yourapp.test.a01violationapplist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.yourapp.test.a01violationapplist.navigation.AppNavigation
import com.yourapp.test.a01violationapplist.navigation.Screen
import com.yourapp.test.a01violationapplist.ui.theme._01VIOLATIONAPPLISTTheme
import com.yourapp.test.a01violationapplist.utils.PreferencesManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _01VIOLATIONAPPLISTTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val preferencesManager = remember { PreferencesManager(context) }
                
                // Determine start destination based on login status
                val startDestination = if (preferencesManager.isLoggedIn()) {
                    Screen.Home.route
                } else {
                    Screen.Login.route
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}