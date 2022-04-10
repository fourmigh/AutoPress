package org.caojun.library.accessibilityservice

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class MainAccessibilityService : AccessibilityService() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var mainManager: MainManager? = null

        fun isDestroyed(): Boolean {
            return mainManager == null
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        mainManager = MainManager(object : MainManager.Listener {
            override fun onFound(view: AccessibilityNodeInfo) {
                val text = "Click: ${view.viewIdResourceName} | ${view.text}"
                Log.d("onAccessibilityEvent", text)
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this@MainAccessibilityService, text, Toast.LENGTH_LONG).show()
                }
            }
        })
        mainManager?.onCreate(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        mainManager?.onAccessibilityEvent(event)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        mainManager?.onDestroy()
        mainManager = null
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
    }
}