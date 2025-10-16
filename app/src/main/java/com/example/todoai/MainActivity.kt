
package com.example.todoai

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todoai.ui.AppViewModel
import com.example.todoai.ui.ToDoItemRow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestPerms = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

        setContent {
            MaterialTheme {
                val vm: AppViewModel = viewModel(factory = AppViewModel.factory(applicationContext))
                LaunchedEffect(Unit) {
                    requestPerms.launch(arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR, Manifest.permission.POST_NOTIFICATIONS))
                }
                Surface(Modifier.fillMaxSize()) {
                    AppScreen(vm)
                }
            }
        }
    }
}

@Composable
fun AppScreen(vm: AppViewModel) {
    var title by remember { mutableStateOf("") }
    Column(Modifier.padding(16.dp)) {
        Text(text = "代办清单（M1）", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Row {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("新增代办标题") }, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Button(onClick = { vm.addQuickTodo(title); title = "" }) { Text("新增") }
        }
        Spacer(Modifier.height(12.dp))
        Row {
            Button(onClick = { vm.writeTodayToSystemCalendar() }) { Text("写入系统日历") }
        }
        Spacer(Modifier.height(12.dp))
        val list = vm.todos.collectAsState(emptyList()).value
        LazyColumn {
            items(list) { t ->
                ToDoItemRow(todo = t, onToggle = { vm.toggleDone(t.id) })
            }
        }
    }
}
