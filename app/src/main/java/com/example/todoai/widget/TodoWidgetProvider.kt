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
class TodoWidgetProvider: AppWidgetProvider(){
  override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray){
    for(id in ids){ val rv=RemoteViews(context.packageName, R.layout.widget_todo); val it=Intent(context, MainActivity::class.java);
      val pi=PendingIntent.getActivity(context,0,it,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT); rv.setOnClickPendingIntent(R.id.tvTitle, pi); mgr.updateAppWidget(id, rv)} }
  companion object{ fun notifyChanged(ctx: Context){ val m=AppWidgetManager.getInstance(ctx); val ids=m.getAppWidgetIds(ComponentName(ctx, TodoWidgetProvider::class.java)); if(ids.isNotEmpty()) (TodoWidgetProvider()).onUpdate(ctx,m,ids) } }
}
