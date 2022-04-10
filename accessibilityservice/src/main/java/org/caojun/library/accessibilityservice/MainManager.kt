package org.caojun.library.accessibilityservice

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.SystemClock
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MainManager(private val listener: Listener? = null) {

    companion object {
        private val KEYS_TEXT = arrayOf("跳过", "我知道了")
        private val KEYS_ID = arrayOf("skip")
    }

    interface Listener {
        fun onFound(view: AccessibilityNodeInfo)
    }

    private var service: AccessibilityService? = null
    private var isRunning = false

    fun onCreate(service: AccessibilityService) {
        val asi = service.serviceInfo
        asi.eventTypes = asi.eventTypes or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
//        asi.eventTypes = asi.eventTypes or AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
//        asi.eventTypes = asi.eventTypes or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
//        asi.flags = asi.flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS

        service.serviceInfo = asi

        this.service = service
        ResearchThread().start()
    }

    fun onDestroy() {
        isRunning = false
    }

    fun onAccessibilityEvent(event: AccessibilityEvent) {
//        Log.d("onAccessibilityEvent", "event.eventType: ${event.eventType}")
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                searchView()
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {

            }
        }
    }

    private inner class ResearchThread : Thread() {
        override fun run() {
            isRunning = true
            while (isRunning) {
                SystemClock.sleep(500)
                researchView()
            }
        }
    }

    private fun searchView(): Boolean {
        val root = service?.rootInActiveWindow ?: return false
        var found = false
        for (key in KEYS_TEXT) {
            val list = root.findAccessibilityNodeInfosByText(key) ?: continue
            if (list.isEmpty()) {
                continue
            }
            for (view in list) {
                if (!view.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    if (view.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        found = true
//                        Log.d("onAccessibilityEvent", "parent.searchView: ${view.parent.text}")
                        listener?.onFound(view.parent)
                    }
                } else {
                    found = true
//                    Log.d("onAccessibilityEvent", "searchView: ${view.text}")
                    listener?.onFound(view)
                }
                view.recycle()
            }
        }
        return found
    }

    private fun researchView(): Boolean {
        val root = service?.rootInActiveWindow ?: return false
        val roots = ArrayList<AccessibilityNodeInfo>()
        roots.add(root)
        val nodeList = ArrayList<AccessibilityNodeInfo>()
        findAllNode(roots, nodeList)
//        Log.d("onAccessibilityEvent", "nodeList.size: ${nodeList.size}")
        var found = false
        for (i in 0 until nodeList.size) {
            val view = nodeList[i]
            val idResourceName = view.viewIdResourceName
            if (TextUtils.isEmpty(idResourceName)) {
                continue
            }
            for (key in KEYS_ID) {
                if (idResourceName.contains(key)) {
                    if (!view.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        if (view.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                            found = true
//                            Log.d("onAccessibilityEvent", "parent.researchView: $idResourceName")
                            listener?.onFound(view.parent)
                        }
                    } else {
                        found = true
//                        Log.d("onAccessibilityEvent", "researchView: $idResourceName")
                        listener?.onFound(view)
                    }
                    view.recycle()
                }
            }
        }
        return found
    }

    private fun findAllNode(
        roots: List<AccessibilityNodeInfo>,
        list: MutableList<AccessibilityNodeInfo>
    ) {
        try {
            val tem = ArrayList<AccessibilityNodeInfo>()
            for (e in roots) {
                val rect = Rect()
                e.getBoundsInScreen(rect)
                if (rect.width() <= 0 || rect.height() <= 0) continue
                list.add(e)
                for (n in 0 until e.childCount) {
                    tem.add(e.getChild(n))
                }
            }
            if (tem.isNotEmpty()) {
                findAllNode(tem, list)
            }
        } catch (e: Throwable) {
        }
    }
}