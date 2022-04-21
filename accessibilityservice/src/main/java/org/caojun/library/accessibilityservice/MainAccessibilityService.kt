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
            return mainManager == null || mainManager?.isDestroyed() == true
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        mainManager = MainManager(object : MainManager.Listener {
            override fun onFound(
                view: AccessibilityNodeInfo?,
                action: String,
                typeCompare: String,
                keyCompare: String
            ) {
                val text = "Click.$action: ${view?.viewIdResourceName} | ${view?.text} | $typeCompare | $keyCompare | ${view?.className}"
                Log.d("onAccessibilityEvent", text)
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this@MainAccessibilityService, text, Toast.LENGTH_LONG).show()
                }
            }

            override fun onPressFailed(
                view: AccessibilityNodeInfo?,
                typeCompare: String,
                keyCompare: String
            ) {
                val text = "onPressFailed: ${view?.viewIdResourceName} | ${view?.text} | $typeCompare | $keyCompare | ${view?.className}"
                Log.d("onAccessibilityEvent", text)
            }
        })
        mainManager?.onCreate(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d("onAccessibilityEvent", "onAccessibilityEvent")
        mainManager?.onAccessibilityEvent(event)
    }

    override fun onUnbind(intent: Intent?): Boolean {
//        mainManager?.onDestroy()
//        mainManager = null
        Log.d("onAccessibilityEvent", "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        Log.d("onAccessibilityEvent", "onInterrupt")
    }
}