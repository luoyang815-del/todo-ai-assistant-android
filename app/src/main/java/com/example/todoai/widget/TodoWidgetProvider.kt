
package com.example.todoai.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.todoai.MainActivity
import com.example.todoai.R

class TodoWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            val rv = RemoteViews(context.packageName, R.layout.widget_todo)
            val intent = Intent(context, MainActivity::class.java)
            val pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setOnClickPendingIntent(R.id.tvTitle, pi)
            appWidgetManager.updateAppWidget(id, rv)
        }
    }

    companion object {
        fun notifyChanged(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, TodoWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                (TodoWidgetProvider()).onUpdate(context, mgr, ids)
            }
        }
    }
}
