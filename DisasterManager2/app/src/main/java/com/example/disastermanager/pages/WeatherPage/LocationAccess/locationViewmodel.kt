package com.example.disastermanager.pages.WeatherPage.LocationAccess

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class locationViewmodel: ViewModel() {
    private val _location = mutableStateOf<locationData?>(null)
    val location : State<locationData?> = _location

    fun updateLocation(newLocation:locationData){
        _location.value = newLocation
    }
}
