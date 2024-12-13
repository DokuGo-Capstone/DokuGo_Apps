package com.dokugo

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize ThreeTenABP library
        AndroidThreeTen.init(this)
    }
}