package com.example.todoai.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.todoai.MainActivity
import com.example.todoai.R
import com.example.todoai.widget.TodoWidgetService

class TodoWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_todo_list)

            val svcIntent = Intent(context, TodoWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            views.setRemoteAdapter(R.id.todo_list, svcIntent)

            val openAppIntent = Intent(context, MainActivity::class.java)
            val flags = if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
            val pi = PendingIntent.getActivity(context, 0, openAppIntent, flags)
            views.setOnClickPendingIntent(R.id.header, pi)

            views.setEmptyView(R.id.todo_list, R.id.empty)
            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.todo_list)
        }
    }

    companion object {
        fun notifyAll(context: Context) {
            val man = AppWidgetManager.getInstance(context)
            val ids = man.getAppWidgetIds(ComponentName(context, TodoWidgetProvider::class.java))
            man.notifyAppWidgetViewDataChanged(ids, R.id.todo_list)
        }
    }
}
