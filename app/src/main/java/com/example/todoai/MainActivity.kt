
package com.example.todoai

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todoai.ui.AppViewModel
import com.example.todoai.ui.ToDoItemRow
import com.example.todoai.ui.SettingsScreen
import com.example.todoai.ui.SummaryScreen

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
                val nav = rememberNavController()
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(selected = false, onClick = { nav.navigate("home") }, label = { Text(getString(R.string.nav_home)) }, icon = { Text("📋") })
                            NavigationBarItem(selected = false, onClick = { nav.navigate("summary") }, label = { Text(getString(R.string.nav_summary)) }, icon = { Text("🧠") })
                            NavigationBarItem(selected = false, onClick = { nav.navigate("settings") }, label = { Text(getString(R.string.nav_settings)) }, icon = { Text("⚙️") })
                        }
                    }
                ) { pad ->
                    NavHost(navController = nav, startDestination = "home", modifier = Modifier.padding(pad)) {
                        composable("home") { HomeScreen(vm) }
                        composable("settings") { SettingsScreen(context = this@MainActivity) }
                        composable("summary") { SummaryScreen(vm) }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(vm: AppViewModel) {
    var title by remember { mutableStateOf("") }
    Column(Modifier.padding(16.dp)) {
        Text(text = "代办清单（M2）", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Row {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("新增代办标题") }, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Button(onClick = { vm.addQuickTodo(title); title = "" }) { Text("新增") }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.writeTodayToSystemCalendar() }) { Text("写入系统日历") }
            Button(onClick = { vm.summarizeAndStore() }) { Text("汇总到 OpenAI 并入库") }
        }
        Spacer(Modifier.height(12dp))
        val list = vm.todos.collectAsState(emptyList()).value
        LazyColumn {
            items(list) { t ->
                ToDoItemRow(todo = t, onToggle = { vm.toggleDone(t.id) })
            }
        }
    }
}
