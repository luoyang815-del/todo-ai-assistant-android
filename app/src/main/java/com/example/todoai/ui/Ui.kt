
package com.example.todoai.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.todoai.data.AppDb
import com.example.todoai.data.ToDo
import com.example.todoai.calendar.CalendarHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class AppViewModel(private val app: Application) : ViewModel() {
    private val db = Room.databaseBuilder(app, AppDb::class.java, "todo-ai.db").build()
    private val todoDao = db.todoDao()

    val todos = todoDao.watchAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addQuickTodo(title: String) = viewModelScope.launch {
        if (title.isBlank()) return@launch
        todoDao.upsert(ToDo(id = UUID.randomUUID().toString(), title = title))
    }

    fun toggleDone(id: String) = viewModelScope.launch {
        val cur = todos.value.find { it.id == id } ?: return@launch
        val target = if (cur.status == "DONE") "PENDING" else "DONE"
        todoDao.updateStatus(id, target)
    }

    fun writeTodayToSystemCalendar() = viewModelScope.launch {
        val now = java.time.Instant.now().toEpochMilli()
        val end = now + 60 * 60 * 1000
        CalendarHelper.insertOrUpdateEvent(app, "今日代办（示例）", now, end, "由 Todo AI Assistant 写入", 1L, null)
    }

    companion object {
        fun factory(appContext: android.content.Context) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppViewModel(appContext.applicationContext as Application) as T
            }
        }
    }
}

@Composable
fun ToDoItemRow(todo: com.example.todoai.data.ToDo, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Checkbox(checked = todo.status == "DONE", onCheckedChange = { onToggle() })
        Spacer(Modifier.width(8.dp))
        Text(text = todo.title, style = MaterialTheme.typography.bodyLarge)
    }
}
