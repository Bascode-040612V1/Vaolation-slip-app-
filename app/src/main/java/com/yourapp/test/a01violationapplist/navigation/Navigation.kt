package com.yourapp.test.a01violationapplist.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yourapp.test.a01violationapplist.ui.screens.auth.LoginScreen
import com.yourapp.test.a01violationapplist.ui.screens.home.HomeScreen
import com.yourapp.test.a01violationapplist.ui.screens.settings.Settings01Screen
import com.yourapp.test.a01violationapplist.ui.screens.settings.Settings02Screen
import com.yourapp.test.a01violationapplist.ui.screens.student.StudentViolationScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Settings01 : Screen("settings01")
    object Settings02 : Screen("settings02")
    object StudentViolation : Screen("student_violation/{studentId}") {
        fun createRoute(studentId: String) = "student_violation/$studentId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(Screen.Settings01.route) {
            Settings01Screen(navController = navController)
        }
        
        composable(Screen.Settings02.route) {
            Settings02Screen(navController = navController)
        }
        
        composable(Screen.StudentViolation.route) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            StudentViolationScreen(
                studentId = studentId,
                navController = navController
            )
        }
    }
}