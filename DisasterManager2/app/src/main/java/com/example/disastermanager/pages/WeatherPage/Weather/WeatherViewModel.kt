package com.example.disastermanager.pages.WeatherPage.Weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.disastermanager.pages.WeatherPage.retrofit.Constant
import com.example.disastermanager.pages.WeatherPage.retrofit.NetworkResponse
import com.example.disastermanager.pages.WeatherPage.retrofit.WeatherModel
import com.example.disastermanager.pages.WeatherPage.retrofit.retrofitInstance
import kotlinx.coroutines.launch

class weatherViewModel : ViewModel() {
    private val WeatherApi = retrofitInstance.weatherAPI
    private val _weatherResult = MutableLiveData<NetworkResponse<WeatherModel>>()
    val weatherResult: LiveData<NetworkResponse<WeatherModel>> = _weatherResult

    // Track the last searched city to prevent spamming the API
    private var lastFetchedCity: String? = null

    fun getData(city: String) {
        // 1. LOGIC CHECK: If the city is the same as the last one we fetched, STOP here.
        // We ignore case (e.g., "London" == "london") and trim spaces.
        if (lastFetchedCity != null && lastFetchedCity.equals(city.trim(), ignoreCase = true)) {
            return
        }

        viewModelScope.launch {
            _weatherResult.value = NetworkResponse.Loading
            try {
                val response = WeatherApi.getWeather(Constant.ApiKey, city)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _weatherResult.value = NetworkResponse.Success(it)
                        // 2. UPDATE: Only update this AFTER a successful fetch
                        lastFetchedCity = city.trim()
                    }
                } else {
                    _weatherResult.value = NetworkResponse.Error("Failed to Load Data")
                    // Optional: If it failed, reset lastFetchedCity so the user can try again immediately
                    lastFetchedCity = null
                }
            } catch (e: Exception) {
                _weatherResult.value = NetworkResponse.Error("Failed to Load Data")
                lastFetchedCity = null
            }
        }
    }

    // Optional: Call this if you explicitly WANT to force a refresh (e.g., Pull-to-Refresh)
    fun forceRefresh() {
        lastFetchedCity?.let { city ->
            lastFetchedCity = null // Reset cache
            getData(city) // Fetch again
        }
    }
}