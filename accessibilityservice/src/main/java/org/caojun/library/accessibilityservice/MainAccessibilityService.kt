package org.caojun.library.accessibilityservice

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
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
                typeCompare: String,
                keyCompare: String
            ) {
                val text = "Click: ${view?.viewIdResourceName} | ${view?.text} | $typeCompare | $keyCompare"
                Log.d("onAccessibilityEvent", text)
//                Handler(Looper.getMainLooper()).post {
//                    Toast.makeText(this@MainAccessibilityService, text, Toast.LENGTH_LONG).show()
//                }
            }

            override fun onPressFailed(
                view: AccessibilityNodeInfo?,
                typeCompare: String,
                keyCompare: String
            ) {
                val text = "onPressFailed: ${view?.viewIdResourceName} | ${view?.text} | $typeCompare | $keyCompare"
                Log.d("onAccessibilityEvent", text)
            }
        })
        mainManager?.onCreate(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        mainManager?.onAccessibilityEvent(event)
    }

//    override fun onUnbind(intent: Intent?): Boolean {
//        mainManager?.onDestroy()
//        mainManager = null
//        return super.onUnbind(intent)
//    }

    override fun onInterrupt() {
    }
}