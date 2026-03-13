package com.service.framework.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.service.framework.Fw
import com.service.framework.R
import com.service.framework.util.FwLog

class FloatWindowService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatView: View
    private var layoutParams: WindowManager.LayoutParams? = null
    private var isViewAdded = false

    // 用于拖动悬浮窗的变量
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    companion object {
        private const val CHANNEL_ID = "float_window_channel"
        private const val CHANNEL_NAME = "悬浮窗服务"
        private const val NOTIFICATION_ID = 10010000
        const val EXTRA_WIDTH = "extra_width"
        const val EXTRA_TOP_MARGIN = "extra_top_margin"  // 顶部距离参数
        const val DEFAULT_WIDTH = 0

        val MARGIN = 10 //这是对对话框的微调

    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        FwLog.d("短信  NOTIFICATION_ID " + NOTIFICATION_ID)
        // 初始化WindowManager但不立即创建视图
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {

            val width = it.getIntExtra(EXTRA_WIDTH, DEFAULT_WIDTH)
            val topMargin =
                100
            if (width > 0 && !isViewAdded) {
                createFloatView(width, topMargin)
            } else if (width > 0) {
                // 如果视图已添加，更新宽度和顶部距离
                updateFloatView(width, topMargin)
            } else if (width == 0 && isViewAdded) {
                // 如果宽度为0，移除视图
                removeFloatView()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "悬浮窗服务运行中"
                setSound(null, null)
                enableLights(false)
                enableVibration(false)
                lightColor = Color.BLUE
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }

            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("快快快快")
            .setContentText("悬浮窗服务运行中")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }

    private fun createFloatView(width: Int, topMargin: Int) {
        // 防止重复添加
        if (isViewAdded && ::floatView.isInitialized && floatView.windowToken != null) {
            return
        }

        floatView = LayoutInflater.from(this).inflate(R.layout.activity_alert, null)

        println("zhaihongyuan >>> 创建悬浮窗，宽度: $width, 顶部距离: $topMargin")

        layoutParams =
            WindowManager.LayoutParams(
                width, // 使用传入的宽度
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )

        // 设置悬浮窗位置
        layoutParams?.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        layoutParams?.y = topMargin  // 设置顶部距离

        setupViewStyle()
        setupTouchListener()

        windowManager.addView(floatView, layoutParams)
        isViewAdded = true
    }

    private fun updateFloatView(width: Int, topMargin: Int) {
        if (!isViewAdded || !::floatView.isInitialized || floatView.windowToken == null) {
            FwLog.d("updateFloatView return")
            return
        }

        // 更新宽度
        layoutParams?.width = width

        // 更新顶部距离
        layoutParams?.y = topMargin

        windowManager.updateViewLayout(floatView, layoutParams)

        println("zhaihongyuan >>> 更新悬浮窗，宽度: $width, 顶部距离: $topMargin")
    }

    private fun removeFloatView() {
        if (!isViewAdded || !::floatView.isInitialized) {
            return
        }

        try {
            windowManager.removeView(floatView)
            isViewAdded = false
            println("zhaihongyuan >>> 移除悬浮窗")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupViewStyle() {
//        val textView = floatView.findViewById<TextView>(R.id.floatText)
////        textView.text = "请将智能感知区对准食材"
//        textView.setTextColor(Color.WHITE)
    }

    private fun setupTouchListener() {
        floatView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 记录初始位置
                    initialX = layoutParams?.x ?: 0
                    initialY = layoutParams?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    // 计算移动距离并更新位置
                    layoutParams?.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams?.y = initialY + (event.rawY - initialTouchY).toInt()

//                    // 限制悬浮窗在屏幕内移动
//                    val maxX = ScreenUtils.getScreenWidth(this) - view.width
//                    val maxY = ScreenUtils.getScreenHeight(this) - view.height
//
//                    layoutParams?.x = layoutParams?.x?.coerceIn(0, maxX) ?: 0
//                    layoutParams?.y = layoutParams?.y?.coerceIn(0, maxY) ?: 0

                    windowManager.updateViewLayout(floatView, layoutParams)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    // 添加点击效果（可选）
                    view.performClick()
                    true
                }

                else -> false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFloatView()
    }
}