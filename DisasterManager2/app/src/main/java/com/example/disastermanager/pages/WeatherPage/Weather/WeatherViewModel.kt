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
        viewModelScope.launch {
            _weatherResult.value = NetworkResponse.Loading
            try {
                val response = WeatherApi.getWeather(Constant.ApiKey, city.trim())
                if (response.isSuccessful) {
                    response.body()?.let {
                        _weatherResult.value = NetworkResponse.Success(it)
                        lastFetchedCity = city.trim()
                    }
                } else {
                    val errorMsg = if (response.code() == 400) {
                        "Location not found. Please verify spelling."
                    } else {
                        "Failed to load weather data (Code: ${response.code()})"
                    }
                    _weatherResult.value = NetworkResponse.Error(errorMsg)
                    lastFetchedCity = null
                }
            } catch (e: Exception) {
                _weatherResult.value = NetworkResponse.Error("Network error. Please check internet connection.")
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