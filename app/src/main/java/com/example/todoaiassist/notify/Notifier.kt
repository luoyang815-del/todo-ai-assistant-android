package com.example.todoaiassist.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class Notifier(private val ctx: Context) {
    private val channelId = "todo_ai_assist"

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(channelId, "Todo AI Notification", NotificationManager.IMPORTANCE_DEFAULT)
            mgr.createNotificationChannel(channel)
        }
    }

    fun notifyAIReply(content: String) {
        ensureChannel()
        val text = if (content.length > 30) content.substring(0, 30) + "..." else content
        val noti = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("AI Reply")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(ctx).notify(1001, noti)
    }
}