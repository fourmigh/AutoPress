package org.caojun.autopress

import android.app.Application

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(GlobalException())
    }
}