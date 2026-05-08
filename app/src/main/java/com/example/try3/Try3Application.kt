package com.example.try3

import android.app.Application

class Try3Application : Application() {
    override fun onCreate() {
        super.onCreate()
        com.example.try3.data.supabase.App.context = this
    }
}
