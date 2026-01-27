package com.example.disastermanager.ViewModel


import androidx.lifecycle.ViewModel
import com.example.disastermanager.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


import com.google.firebase.firestore.firestore


class AuthViewModel: ViewModel() {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore



    fun login(email:String,password :String,onResult : (Boolean,String?) -> Unit){
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener{ dbTask->
                if(dbTask.isSuccessful){
                    onResult(true,null)
                }
                else{
                    onResult(false,dbTask.exception?.localizedMessage)
                }
            }}


    fun signup(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    if (userId != null) {
                        // ✅ Only return userId to move to ProfileSetupScreen
                        onResult(true, userId)
                    } else {
                        onResult(false, "User ID not found")
                    }
                } else {
                    onResult(false, task.exception?.localizedMessage)
                }
            }
    }


    fun updateUserProfile(
        name : String,
        uid: String,
        phone: String,
        address: String,
        gender: String,
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        onResult: (Boolean, String?) -> Unit
    ) {
        val userModel = UserModel(
            email = auth.currentUser?.email ?: "",
            uid = uid,
            phone = phone,
            address = address,
            gender = gender,
            latitude = latitude,
            longitude = longitude,
            name = name
        )

        firestore.collection("users").document(uid)
            .set(userModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage)
                }
            }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun saveUserLocation(
        uid: String,
        latitude: Double,
        longitude: Double,
        address: String? = null,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val data = mutableMapOf<String, Any>(
            "latitude" to latitude,
            "longitude" to longitude
        )

        if (address != null) {
            data["address"] = address
        }

        firestore.collection("users")
            .document(uid)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
    }

}
