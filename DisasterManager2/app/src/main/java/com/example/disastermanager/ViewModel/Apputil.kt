package com.example.disastermanager.ViewModel

import android.content.Context
import android.widget.Toast


object Apputil {
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}