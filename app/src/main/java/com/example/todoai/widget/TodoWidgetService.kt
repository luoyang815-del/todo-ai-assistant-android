package com.example.todoai.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.todoai.R
import com.example.todoai.todo.TodoRepository

class TodoWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = Factory(applicationContext)

    class Factory(private val ctx: Context) : RemoteViewsService.RemoteViewsFactory {
        private val items = mutableListOf<com.example.todoai.todo.TodoItem>()
        private lateinit var repo: TodoRepository

        override fun onCreate() { repo = TodoRepository(ctx) }
        override fun onDataSetChanged() {
            items.clear()
            val list = repo.list()
            val important = list.filter { it.important }
            val normal = list.filter { !it.important }.take(10)
            items.addAll(important + normal)
        }
        override fun onDestroy() { items.clear() }
        override fun getCount(): Int = items.size
        override fun getViewAt(position: Int): RemoteViews {
            val rv = RemoteViews(ctx.packageName, R.layout.widget_todo_item)
            val item = items[position]
            rv.setTextViewText(R.id.title, item.title)
            rv.setImageViewResource(R.id.dot, if (item.important) android.R.drawable.presence_busy else android.R.drawable.presence_invisible)
            return rv
        }
        override fun getLoadingView(): RemoteViews? = null
        override fun getViewTypeCount(): Int = 1
        override fun getItemId(position: Int): Long = position.toLong()
        override fun hasStableIds(): Boolean = true
    }
}
