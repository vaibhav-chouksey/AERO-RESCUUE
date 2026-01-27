package com.example.disastermanager.ViewModel

// Data class to hold the image information
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

data class DisasterImage(
    val filename: String = "",
    val timestamp: Timestamp? = null,
    val url: String = ""
)


/**
 * Fetches the latest image data (URL) from Firestore.
 * Path: /image/image1s
 */
class ImageViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val TAG = "ImageViewModel"

    // State to hold the fetched image data
    private val _disasterImage = mutableStateOf<DisasterImage?>(null)
    val disasterImage: State<DisasterImage?> = _disasterImage

    // State for loading and error handling
    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    // Document path derived from the provided image: /image/image1s
    private val COLLECTION_PATH = "image"
    private val DOCUMENT_ID = "image1s"

    init {
        startRealtimeImageUpdates()
    }

    private fun startRealtimeImageUpdates() {
        _isLoading.value = true
        _errorMessage.value = null

        db.collection(COLLECTION_PATH)
            .document(DOCUMENT_ID)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    _errorMessage.value = "Error fetching image data: ${e.message}"
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        // Map the document fields to the DisasterImage data class
                        val image = snapshot.toObject(DisasterImage::class.java)
                        _disasterImage.value = image
                        Log.d(TAG, "Current image data: ${image?.url}")
                    } catch (ex: Exception) {
                        _errorMessage.value = "Data parsing error."
                        Log.e(TAG, "Data parsing error", ex)
                    }
                } else {
                    _disasterImage.value = null
                    Log.d(TAG, "Current data: null")
                }
            }
    }
}