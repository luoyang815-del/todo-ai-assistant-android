package com.example.todoai.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.room.Room
import com.example.todoai.MainActivity
import com.example.todoai.R
import com.example.todoai.data.AppDb

class TodoWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            val rv = RemoteViews(context.packageName, R.layout.widget_todo)

            val intent = Intent(context, MainActivity::class.java)
            val pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setOnClickPendingIntent(R.id.tvTitle, pi)

            // 简单读取数据库（注意：生产环境建议用 RemoteViewsService）
            val db = Room.databaseBuilder(context, AppDb::class.java, "todo-ai.db").build()
            val todos = try { db.todoDao().listOnce() } catch (_: Exception) { emptyList() }
            val important = todos.filter { it.priority.equals("HIGH", true) && it.status != "DONE" }
            val normal = todos.filter { !it.priority.equals("HIGH", true) && it.status != "DONE" }

            val totalOpen = todos.count { it.status != "DONE" }
            rv.setTextViewText(R.id.tvCounts, "$totalOpen 项，重要 ${important.size}")

            // 所有重要（无限制） + 前 10 条普通
            val lines = (important.map { "⭐ " + it.title } + normal.take(10).map { "• " + it.title })
                .joinToString("\n").ifBlank { "(暂无待办)" }
            rv.setTextViewText(R.id.tvItems, lines)

            // 重要时把计数染成红色，列表保持深色
            val colorCounts = if (important.isNotEmpty()) context.getColor(R.color.widget_important) else context.getColor(R.color.widget_text_muted)
            rv.setTextColor(R.id.tvCounts, colorCounts)
            rv.setTextColor(R.id.tvItems, context.getColor(R.color.widget_normal))

            appWidgetManager.updateAppWidget(id, rv)
        }
    }

    companion object {
        /** 在 ViewModel 的增删改操作后调用，刷新小插件 */
        fun notifyChanged(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, TodoWidgetProvider::class.java))
            if (ids.isNotEmpty()) (TodoWidgetProvider()).onUpdate(context, mgr, ids)
        }
    }
}
