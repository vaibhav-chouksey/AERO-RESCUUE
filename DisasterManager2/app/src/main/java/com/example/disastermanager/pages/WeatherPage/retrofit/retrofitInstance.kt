package com.example.disastermanager.pages.WeatherPage.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object retrofitInstance {

    private const val baseurl = "https://api.weatherapi.com"
    //return->
    //    Retrofit.Builder()
    //    .baseUrl(baseurl)
    //    .addConverterFactory(GsonConverterFactory.create())
    //    .build()
    private fun getInstance() : Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseurl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val weatherAPI : WeatherAPI = getInstance().create(WeatherAPI::class.java)
}