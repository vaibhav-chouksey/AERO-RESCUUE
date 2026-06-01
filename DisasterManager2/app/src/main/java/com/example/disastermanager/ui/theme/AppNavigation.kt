package com.example.disastermanager.ui.theme

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.disastermanager.pages.DroneStatusScreen
import com.example.disastermanager.screen.AssignDroneScreen
import com.example.disastermanager.screen.AuthScreen
import com.example.disastermanager.screen.DisasterReportsPage
import com.example.disastermanager.screen.DroneDetailScreen
import com.example.disastermanager.screen.EmergencyContactsScreen
import com.example.disastermanager.screen.EmergencySOSPage
import com.example.disastermanager.screen.LoginScreen
import com.example.disastermanager.screen.NearbyAlertsScreen
import com.example.disastermanager.screen.ProfileSetupScreen
import com.example.disastermanager.screen.ReportDetailScreen
import com.example.disastermanager.screen.SignupScreen
import com.example.disastermanager.screen.homeScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


@Composable
fun AppNavigation(modifier: Modifier) {
    val navController = rememberNavController()
    val modifier = Modifier.padding(top = 0.dp)

    val isLoggedIn = Firebase.auth.currentUser != null
    val firstpage = if (isLoggedIn) "homescreen" else "auth"
    NavHost(navController = navController, startDestination = firstpage, modifier = modifier) {
        composable("auth") {
            AuthScreen(modifier = modifier, navController)
        }
        composable("login") {
            LoginScreen(modifier, navController)
        }
        composable("signup") {
            SignupScreen(modifier, navController)
        }
        composable("profile_setup/{userId}") {
            var userId = it.arguments?.getString("userId")

            if (userId != null) {
                ProfileSetupScreen(
                    modifier = modifier,
                    navController = navController,
                    userId = userId,

                )
            }
        }
        composable("homescreen") {
            homeScreen(modifier, navController)
        }
        composable("emergency_contacts"){
            EmergencyContactsScreen(modifier,navController)
        }
        composable("report_disaster"){

            DisasterReportsPage(modifier, navController)
        }

        composable("emerg_sos"){

            EmergencySOSPage(
                modifier,
                navController
            )
        }

        composable("nearby_alert"){

            NearbyAlertsScreen(modifier,navController)
        }

//        composable("homescreen") {
//            DroneStatusQuickCard(modifier, navController)
//        }
        composable("drone_status"){

            DroneStatusScreen(modifier,navController)
        }

        composable("assign_drone") {
            AssignDroneScreen(modifier, navController)
        }

        composable(
            route = "report_detail/{reportId}",
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ){
            ReportDetailScreen(
                reportId = it.arguments?.getString("reportId").orEmpty(),
                onBackClick = {navController.popBackStack()}

            )
        }
        composable(
            route = "drone_detail/{droneId}",
            arguments = listOf(navArgument("droneId") { type = NavType.StringType })
        ){
            val droneId = it.arguments?.getString("droneId").orEmpty()
            DroneDetailScreen(
                droneId = droneId,
                navController = navController
            )

        }




    }
}
