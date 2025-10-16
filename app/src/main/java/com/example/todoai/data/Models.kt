
package com.example.todoai.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "todos")
data class ToDo(
    @PrimaryKey val id: String,
    val title: String,
    val desc: String? = null,
    val priority: String = "MEDIUM",
    val status: String = "PENDING",
    val tags: String = "",
    val dueAt: Long? = null,
    val remindAt: Long? = null,
    val calendarEventId: Long? = null,
    val createdAt: Long = Instant.now().toEpochMilli(),
    val updatedAt: Long = Instant.now().toEpochMilli()
)

@Entity(tableName = "summaries")
data class Summary(
    @PrimaryKey val id: String,
    val type: String,
    val promptUsed: String,
    val summaryMd: String,
    val createdAt: Long = Instant.now().toEpochMilli()
)
