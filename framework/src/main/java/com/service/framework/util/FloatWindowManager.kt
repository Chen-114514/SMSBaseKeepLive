package com.service.framework.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
 import com.service.framework.service.FloatWindowService

object FloatWindowManager {


    /**
     * 启动悬浮窗服务
     */
    fun startFloatWindow(context: Context, with: Int, marginTop: Int) {
        if (!hasPermission(context)){
            FwLog.d("startFloatWindow: ! hasPermission")
            return
        }
        val intent = Intent(context, FloatWindowService::class.java)
        intent.putExtra(FloatWindowService.EXTRA_WIDTH, with) // 传入宽度（单位：像素）

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        }else{
            context.startService(intent)
        }
    }

    /**
     * 停止悬浮窗服务
     */
    fun stopFloatWindow(context: Context) {
        val intent = Intent(context, FloatWindowService::class.java)
        context.stopService(intent)
    }

    /**
     * 检查是否有悬浮窗权限
     */
    fun hasPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
}