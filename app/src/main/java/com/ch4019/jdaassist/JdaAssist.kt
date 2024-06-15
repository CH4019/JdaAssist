package com.ch4019.jdaassist

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class JdaAssist : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: Application
            private set
    }
}