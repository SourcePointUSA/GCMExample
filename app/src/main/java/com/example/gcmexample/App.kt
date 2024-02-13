package com.example.gcmexample

import android.app.Application
import com.google.firebase.FirebaseApp

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize FirebaseApp
        FirebaseApp.initializeApp(this)
    }
}