package com.example.todoai.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import com.example.todoai.R
import com.example.todoai.todo.TodoRepository

class TodoWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateAll(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun refreshAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, TodoWidgetProvider::class.java))
            updateAll(context, mgr, ids)
        }

        private fun updateAll(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val repo = TodoRepository(context)
            val all = repo.list()
            val important = all.filter { it.important }
            val normals = all.filter { !it.important }.take(10)

            val rv = RemoteViews(context.packageName, R.layout.widget_todo)
            rv.setTextViewText(R.id.summary, "鎬绘暟锛? + all.size + "锛岄噸瑕侊細" + important.size)

            val importantText = if (important.isEmpty()) "锛堟棤锛? else important.joinToString("\n") { "鈥?" + it.title }
            val normalText = if (normals.isEmpty()) "锛堟棤锛? else normals.joinToString("\n") { "鈥?" + it.title }

            rv.setTextViewText(R.id.importantList, "閲嶈浠ｅ姙锛歕n" + importantText)
            rv.setTextViewText(R.id.topList, "鍓?0鏉★細\n" + normalText)

            appWidgetIds.forEach { id -> appWidgetManager.updateAppWidget(id, rv) }
        }
    }
}

