package org.caojun.library.accessibilityservice

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.SystemClock
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.View.OnFocusChangeListener
import android.view.View.OnTouchListener
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import org.caojun.library.accessibilityservice.widget.SkipPositionDescribe
import org.caojun.library.accessibilityservice.widget.WidgetButtonDescribe
import java.util.*

class MainManager(private val listener: Listener? = null) {

    companion object {
        private val KEYS_TEXT = arrayOf("跳过", "知道了", "还剩")
        private val KEYS_ID = arrayOf("skip")
        private const val TYPE_TEXT = "text"
        private const val TYPE_ID = "id"
    }

    interface Listener {
        fun onFound(view: AccessibilityNodeInfo?, action: String, typeCompare: String, keyCompare: String)
        fun onPressFailed(view: AccessibilityNodeInfo?, typeCompare: String, keyCompare: String)
    }

    private var service: AccessibilityService? = null
    private var isRunning = false
    private var currentPackageName = ""
    private var currentActivityName = ""

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

    fun isDestroyed(): Boolean {
        return !isRunning
    }

    fun onAccessibilityEvent(event: AccessibilityEvent) {
//        Log.d("onAccessibilityEvent", "event.eventType: ${event.eventType}")
        currentPackageName = event.packageName.toString()
        currentActivityName = event.className?.toString() ?: ""

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
                val time = (System.currentTimeMillis() / 1000).toInt()
                if (time % 10 == 0) {
                    Log.d("onAccessibilityEvent", "ResearchThread")
                }
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
                found = clickView(view, key)
//                if (!view.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
//                    if (view.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
//                        found = true
////                        Log.d("onAccessibilityEvent", "parent.searchView: ${view.parent.text}")
//                        listener?.onFound(view.parent, TYPE_TEXT, key)
//                    }
//                } else {
//                    found = true
////                    Log.d("onAccessibilityEvent", "searchView: ${view.text}")
//                    listener?.onFound(view, TYPE_TEXT, key)
//                }
//                view.recycle()
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

            val text = view.text?.toString() ?: ""
            if (!TextUtils.isEmpty(text)) {
                for (key in KEYS_TEXT) {
                    if (text.contains(key)) {
                        found = clickView(view, key)
//                        if (!view.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
//                            if (view.parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) {
//                                found = true
////                            Log.d("onAccessibilityEvent", "parent.researchView: $idResourceName")
//                                listener?.onFound(view.parent, TYPE_TEXT, key)
//                            } else {
//                                listener?.onPressFailed(view, TYPE_TEXT, key)
//                            }
//                        } else {
//                            found = true
////                        Log.d("onAccessibilityEvent", "researchView: $idResourceName")
//                            listener?.onFound(view, TYPE_TEXT, key)
//                        }
//                        view.recycle()
                    }
                }
            }

            val idResourceName = view.viewIdResourceName
            if (TextUtils.isEmpty(idResourceName)) {
                continue
            }
            for (key in KEYS_ID) {
                if (idResourceName.contains(key)) {
                    found = clickView(view, key)
//                    if (!view.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
//                        if (view.parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) {
//                            found = true
////                            Log.d("onAccessibilityEvent", "parent.researchView: $idResourceName")
//                            listener?.onFound(view.parent, TYPE_ID, key)
//                        } else {
//                            listener?.onPressFailed(view, TYPE_ID, key)
//                        }
//                    } else {
//                        found = true
////                        Log.d("onAccessibilityEvent", "researchView: $idResourceName")
//                        listener?.onFound(view, TYPE_ID, key)
//                    }
//                    view.recycle()
                }
            }
        }
        return found
    }

    private fun clickView(view: AccessibilityNodeInfo, key: String): Boolean {
        var found = false
        if (!view.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            when {
                view.parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true -> {
                    found = true
                    listener?.onFound(view.parent, "parent", TYPE_ID, key)
                }
                click(view) -> {
                    found = true
                    listener?.onFound(view, "click", TYPE_ID, key)
                }
                click(view.parent) -> {
                    found = true
                    listener?.onFound(view, "clickParent", TYPE_ID, key)
                }
                else -> {
                    listener?.onPressFailed(view, TYPE_ID, key)
                }
            }
        } else {
            found = true
            listener?.onFound(view, "performAction", TYPE_ID, key)
        }
        view.recycle()
        return found
    }

    private fun click(view: AccessibilityNodeInfo): Boolean {
        val rect = Rect()
        view.getBoundsInScreen(rect)
        return click(rect.centerX(), rect.centerY(), 0, 200)
    }
    private fun click(x: Int, y: Int, start_time: Long, duration: Long): Boolean {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val builder = GestureDescription.Builder()
                .addStroke(StrokeDescription(path, start_time, duration))
            service?.dispatchGesture(builder.build(), null, null) ?: false
        } else {
            false
        }
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

    fun showSearchView() {

        val windowManager =
            service?.getSystemService(AccessibilityService.WINDOW_SERVICE) as WindowManager ?: return
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        val b = metrics.heightPixels > metrics.widthPixels
        val width = if (b) metrics.widthPixels else metrics.heightPixels
        val height = if (b) metrics.heightPixels else metrics.widthPixels

        val widgetDescribe = WidgetButtonDescribe()
        val positionDescribe = SkipPositionDescribe("", "", 0, 0, 500, 500, 1)

        val inflater = LayoutInflater.from(service)
        val adv_view = inflater.inflate(R.layout.advertise_desc, null)
        val pacName: TextView = adv_view.findViewById(R.id.pacName)
        val actName: TextView = adv_view.findViewById(R.id.actName)
        val widget: TextView = adv_view.findViewById(R.id.widget)
        val xyP: TextView = adv_view.findViewById(R.id.xy)
        val switchWid: Button = adv_view.findViewById(R.id.switch_wid)
        val saveWidgetButton: Button = adv_view.findViewById(R.id.save_wid)
        val switchAim: Button = adv_view.findViewById(R.id.switch_aim)
        val savePositionButton: Button = adv_view.findViewById(R.id.save_aim)
        val quitButton: Button = adv_view.findViewById(R.id.quit)
        val layout_win = inflater.inflate(R.layout.accessibilitynode_desc, null)
        val layout_add: FrameLayout = layout_win.findViewById(R.id.frame)
        val target_xy = ImageView(service)
        target_xy.setImageResource(R.drawable.p)
        val aParams = WindowManager.LayoutParams()
        aParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        aParams.format = PixelFormat.TRANSPARENT
        aParams.gravity = Gravity.START or Gravity.TOP
        aParams.flags =
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        aParams.width = width
        aParams.height = height / 5
        aParams.x = (metrics.widthPixels - aParams.width) / 2
        aParams.y = metrics.heightPixels - aParams.height
        aParams.alpha = 0.8f
        val bParams = WindowManager.LayoutParams()
        bParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        bParams.format = PixelFormat.TRANSPARENT
        bParams.gravity = Gravity.START or Gravity.TOP
        bParams.width = metrics.widthPixels
        bParams.height = metrics.heightPixels
        bParams.flags =
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        bParams.alpha = 0f
        val cParams = WindowManager.LayoutParams()
        cParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        cParams.format = PixelFormat.TRANSPARENT
        cParams.flags =
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        cParams.gravity = Gravity.START or Gravity.TOP
        cParams.height = width / 4
        cParams.width = cParams.height
        cParams.x = (metrics.widthPixels - cParams.width) / 2
        cParams.y = (metrics.heightPixels - cParams.height) / 2
        cParams.alpha = 0f
        adv_view.setOnTouchListener(object : OnTouchListener {
            var x = 0
            var y = 0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = Math.round(event.rawX)
                        y = Math.round(event.rawY)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        aParams.x = Math.round(aParams.x + (event.rawX - x))
                        aParams.y = Math.round(aParams.y + (event.rawY - y))
                        x = Math.round(event.rawX)
                        y = Math.round(event.rawY)
                        windowManager.updateViewLayout(adv_view, aParams)
                    }
                }
                return true
            }
        })
        target_xy.setOnTouchListener(object : OnTouchListener {
            var x = 0
            var y = 0
            var width: Int = cParams.width / 2
            var height: Int = cParams.height / 2
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        savePositionButton.isEnabled = true
                        cParams.alpha = 0.9f
                        windowManager.updateViewLayout(target_xy, cParams)
                        x = Math.round(event.rawX)
                        y = Math.round(event.rawY)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        cParams.x = Math.round(cParams.x + (event.rawX - x))
                        cParams.y = Math.round(cParams.y + (event.rawY - y))
                        x = Math.round(event.rawX)
                        y = Math.round(event.rawY)
                        windowManager.updateViewLayout(target_xy, cParams)
                        positionDescribe.packageName = currentPackageName
                        positionDescribe.activityName = currentActivityName
                        positionDescribe.x = cParams.x + width
                        positionDescribe.y = cParams.y + height
                        pacName.setText(positionDescribe.packageName)
                        actName.setText(positionDescribe.activityName)
                        xyP.text =
                            "X轴：" + positionDescribe.x.toString() + "    " + "Y轴：" + positionDescribe.y.toString() + "    " + "(其他参数默认)"
                    }
                    MotionEvent.ACTION_UP -> {
                        cParams.alpha = 0.5f
                        windowManager.updateViewLayout(target_xy, cParams)
                    }
                }
                return true
            }
        })
        switchWid.setOnClickListener(View.OnClickListener { v ->
            val button = v as Button
            if (bParams.alpha == 0f) {
                val root = service!!.rootInActiveWindow ?: return@OnClickListener
                widgetDescribe.packageName = currentPackageName
                widgetDescribe.activityName = currentActivityName
                layout_add.removeAllViews()
                val roots = ArrayList<AccessibilityNodeInfo>()
                roots.add(root)
                val nodeList = ArrayList<AccessibilityNodeInfo>()
                findAllNode(roots, nodeList)
                for (i in nodeList.indices) {
                    val ani = nodeList[i]
                    Log.d("nodeList", i.toString() + " : " + ani.viewIdResourceName)
                }
                Collections.sort(nodeList, object : Comparator<AccessibilityNodeInfo?> {
                    override fun compare(a: AccessibilityNodeInfo?, b: AccessibilityNodeInfo?): Int {
                        val rectA = Rect()
                        val rectB = Rect()
                        a?.getBoundsInScreen(rectA)
                        b?.getBoundsInScreen(rectB)
                        return rectB.width() * rectB.height() - rectA.width() * rectA.height()
                    }
                })
                for (e in nodeList) {
                    val temRect = Rect()
                    e.getBoundsInScreen(temRect)
                    val params = FrameLayout.LayoutParams(temRect.width(), temRect.height())
                    params.leftMargin = temRect.left
                    params.topMargin = temRect.top
                    val img = ImageView(service)
                    img.setBackgroundResource(R.drawable.node)
                    img.isFocusableInTouchMode = true
                    img.setOnClickListener { v -> v.requestFocus() }
                    img.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
                        if (hasFocus) {
                            widgetDescribe.bonus = temRect
                            widgetDescribe.clickable = e.isClickable
                            widgetDescribe.className = e.className.toString()
                            val cId: CharSequence? = e.viewIdResourceName
                            widgetDescribe.idName = cId?.toString() ?: ""
                            val cDesc = e.contentDescription
                            widgetDescribe.describe = cDesc?.toString() ?: ""
                            val cText = e.text
                            widgetDescribe.text = cText?.toString() ?: ""
                            saveWidgetButton.isEnabled = true
                            pacName.setText(widgetDescribe.packageName)
                            actName.setText(widgetDescribe.activityName)
                            widget.text =
                                "click:" + (if (e.isClickable) "true" else "false") + " " + "bonus:" + temRect.toShortString() + " " + "id:" + (cId?.toString()
                                    ?.substring(cId.toString().indexOf("id/") + 3)
                                    ?: "null") + " " + "desc:" + (cDesc?.toString()
                                    ?: "null") + " " + "text:" + (cText?.toString() ?: "null")
                            v.setBackgroundResource(R.drawable.node_focus)
                        } else {
                            v.setBackgroundResource(R.drawable.node)
                        }
                    }
                    layout_add.addView(img, params)
                }
                bParams.alpha = 0.5f
                bParams.flags =
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                windowManager.updateViewLayout(layout_win, bParams)
                pacName.setText(widgetDescribe.packageName)
                actName.setText(widgetDescribe.activityName)
                button.text = "隐藏布局"
            } else {
                bParams.alpha = 0f
                bParams.flags =
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                windowManager.updateViewLayout(layout_win, bParams)
                saveWidgetButton.isEnabled = false
                button.text = "显示布局"
            }
        })
        switchAim.setOnClickListener { v ->
            val button = v as Button
            if (cParams.alpha == 0f) {
                positionDescribe.packageName = currentPackageName
                positionDescribe.activityName = currentActivityName
                cParams.alpha = 0.5f
                cParams.flags =
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                windowManager.updateViewLayout(target_xy, cParams)
                pacName.setText(positionDescribe.packageName)
                actName.setText(positionDescribe.activityName)
                button.text = "隐藏准心"
            } else {
                cParams.alpha = 0f
                cParams.flags =
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                windowManager.updateViewLayout(target_xy, cParams)
                savePositionButton.isEnabled = false
                button.text = "显示准心"
            }
        }
        saveWidgetButton.setOnClickListener {
//            val temWidget = WidgetButtonDescribe(widgetDescribe)
//            var set: MutableSet<WidgetButtonDescribe?>? =
//                act_widget.get(widgetDescribe.activityName)
//            if (set == null) {
//                set = HashSet<WidgetButtonDescribe?>()
//                set.add(temWidget)
//                act_widget.put(widgetDescribe.activityName, set)
//            } else {
//                set.add(temWidget)
//            }
//            saveWidgetButton.isEnabled = false
//            pacName.setText(widgetDescribe.packageName.toString() + " (以下控件数据已保存)")
            //TODO
        }
        savePositionButton.setOnClickListener {
//            act_position.put(
//                positionDescribe.activityName,
//                SkipPositionDescribe(positionDescribe)
//            )
//            savePositionButton.isEnabled = false
//            pacName.setText(positionDescribe.packageName.toString() + " (以下坐标数据已保存)")
            //TODO
        }
        quitButton.setOnClickListener {
//            val gson = Gson()
//            sharedPreferences.edit().putString(
//                com.lgh.accessibilitytool.MainFunctions.ACTIVITY_POSITION,
//                gson.toJson(act_position)
//            ).putString(
//                com.lgh.accessibilitytool.MainFunctions.ACTIVITY_WIDGET,
//                gson.toJson(act_widget)
//            ).apply()
            windowManager.removeViewImmediate(layout_win)
            windowManager.removeViewImmediate(adv_view)
            windowManager.removeViewImmediate(target_xy)
//            layout_win = null
//            adv_view = null
//            target_xy = null
//            aParams = null
//            bParams = null
//            cParams = null
        }
        windowManager.addView(layout_win, bParams)
        windowManager.addView(adv_view, aParams)
        windowManager.addView(target_xy, cParams)
//        dialog_adv.dismiss()
    }
}