
package com.example.todoai.calendar

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import java.util.*

object CalendarHelper {
    fun insertOrUpdateEvent(
        context: Context,
        title: String,
        begin: Long,
        end: Long,
        description: String? = null,
        calendarId: Long = 1L,
        eventId: Long? = null
    ): Long? {
        return try {
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, begin)
                put(CalendarContract.Events.DTEND, end)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description ?: "")
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }
            val uri: Uri? = if (eventId == null) {
                context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            } else {
                val updateUri = Uri.withAppendedPath(CalendarContract.Events.CONTENT_URI, eventId.toString())
                val rows = context.contentResolver.update(updateUri, values, null, null)
                if (rows > 0) updateUri else null
            }
            uri?.lastPathSegment?.toLongOrNull()
        } catch (e: SecurityException) {
            null
        }
    }
}
