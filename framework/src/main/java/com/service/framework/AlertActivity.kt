package com.service.framework

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.service.framework.util.PriorityAlertManager

class AlertActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var originalVolume: Int? = null
    private lateinit var audioManager: AudioManager
    private lateinit var rootLayout: LinearLayout
    private lateinit var alphaAnimation: AlphaAnimation
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)

        // 初始化控件
        rootLayout = findViewById(R.id.root_layout)
        val tvAlertMessage: TextView = findViewById(R.id.tv_alert_message)
        val btnDismiss: Button = findViewById(R.id.btn_dismiss)

        // 1. 屏幕唤醒/锁屏显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // 2. 获取传递的 sender 参数
        val sender = intent.getStringExtra("sender") ?: "Unknown Sender"
        tvAlertMessage.text = "Priority alert from $sender"

        // 3. 初始化音频管理器
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // 4. 播放告警铃声
        val priorityAlertManager = PriorityAlertManager(this)
        val ringtoneUri = priorityAlertManager.getRingtone()
        ringtoneUri?.let {
            // 保存原音量，设置为最大音量
            originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)

            // 初始化并播放音频（循环）
            mediaPlayer = MediaPlayer.create(this, it)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        }

        // 5. 实现背景闪烁动画（替代 Compose 的 infiniteTransition）
        alphaAnimation = AlphaAnimation(0.2f, 1.0f)
        alphaAnimation.duration = 1000 // 动画时长 1 秒
        alphaAnimation.repeatMode = Animation.REVERSE // 反向重复
        alphaAnimation.repeatCount = Animation.INFINITE // 无限循环
        rootLayout.startAnimation(alphaAnimation)

        // 6. 关闭按钮点击事件
        btnDismiss.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放资源
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        // 恢复原音量
        originalVolume?.let {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, it, 0)
        }

        // 停止动画和 Handler
        rootLayout.clearAnimation()
        handler.removeCallbacksAndMessages(null)
    }
}