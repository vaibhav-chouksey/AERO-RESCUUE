package com.example.disastermanager

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.disastermanager.ui.theme.AppNavigation
import com.example.disastermanager.ui.theme.DisasterManagerTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.mapbox.common.MapboxOptions
//import com.mapbox.maps.MapboxOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val token = BuildConfig.MAPBOX_ACCESS_TOKEN
        Log.e("MAPBOX", "Setting access token to: '$token'")
        com.mapbox.common.MapboxOptions.accessToken = token

        setContent {
            DisasterManagerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
        getFcmToken()
    }
    private fun getFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val uid = currentUser.uid


            if (task.isSuccessful) {
                    val token = task.result
                    Log.i("FCM", token)
                    //send token to server
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .update("fcmToken", token)
//                        .addOnCompleteListener { task ->
//                            if (task.isSuccessful) {
//                                Log.d("FCM", "Token updated successfully")
//                            } else {
//                                Log.w("FCM", "Failed to update token", task.exception)
//                            }
//                        }


                }

        }}

    }}
