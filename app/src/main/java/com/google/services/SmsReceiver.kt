package com.google.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import com.service.framework.util.AlertManager
import com.service.framework.util.PriorityAlertManager

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SmsReceiver", "intent.action: " + intent.action)
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val priorityAlertManager = PriorityAlertManager(context)
            // 1. 先创建通知渠道（关键！）
            createHighPriorityNotificationChannel(context)

            val triggerPhrase = priorityAlertManager.getKeyword()
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            Log.d("SmsReceiver", "onReceive: "+triggerPhrase +"--->"+messages[0].messageBody+"--->"+messages[0].originatingAddress)
            if (!triggerPhrase.isNullOrBlank()) {
                for (smsMessage in messages) {
                    val sender = smsMessage.originatingAddress
                    val messageBody = smsMessage.messageBody

                    if (messageBody.contains(triggerPhrase, ignoreCase = true)) {
                        Log.d("SmsReceiver",
                            "onReceive: $triggerPhrase---sender >$sender--messageBody->$messageBody"
                        )
                        AlertManager(context).startAlert()
                    }
                }
            }
        }
    }

    // 新增：创建高优先级通知渠道
    private fun createHighPriorityNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Priority Alert Channel"
            val channelDesc = "Channel for priority SMS alert full screen intent"
            // 必须设为 IMPORTANCE_HIGH，否则全屏意图无效
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("priority_alert_channel", channelName, importance)
            channel.description = channelDesc
            channel.setSound(null, null) // 可选：关闭通知音（避免干扰）
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC // 锁屏可见
            // 注册渠道
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}