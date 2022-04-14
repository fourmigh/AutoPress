package org.caojun.autopress

import android.util.Log

class GlobalException: Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        Log.e("onAccessibilityEvent", e?.stackTraceToString() ?: "")
    }
}