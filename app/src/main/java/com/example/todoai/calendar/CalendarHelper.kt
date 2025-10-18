package com.example.todoai.calendar
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import java.util.TimeZone
object CalendarHelper {
  fun insertEvent(context: Context, title: String, begin: Long, end: Long, description: String? = null, calendarId: Long = 1L): Long? = try {
    val values = ContentValues().apply {
      put(CalendarContract.Events.DTSTART, begin)
      put(CalendarContract.Events.DTEND, end)
      put(CalendarContract.Events.TITLE, title)
      put(CalendarContract.Events.DESCRIPTION, description ?: "")
      put(CalendarContract.Events.CALENDAR_ID, calendarId)
      put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
    }
    context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)?.lastPathSegment?.toLongOrNull()
  } catch (_: SecurityException) { null }
}
