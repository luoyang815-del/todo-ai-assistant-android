package com.example.todoai.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.todoai.data.AppDb
import com.example.todoai.data.ToDo
import com.example.todoai.data.Summary
import com.example.todoai.calendar.CalendarHelper
import com.example.todoai.network.OpenAIClient
import com.example.todoai.network.OpenAIConfig
import com.example.todoai.settings.SettingsRepo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

class AppViewModel(private val app: Application) : ViewModel() {
    private val db = Room.databaseBuilder(app, AppDb::class.java, "todo-ai.db").build()
    private val todoDao = db.todoDao()
    private val summaryDao = db.summaryDao()
    private val settings = SettingsRepo(app)

    val todos = todoDao.watchAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val summaries = summaryDao.latest().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addQuickTodo(title: String) = viewModelScope.launch {
        if (title.isBlank()) return@launch
        todoDao.upsert(ToDo(id = UUID.randomUUID().toString(), title = title))
    }
    fun toggleDone(id: String) = viewModelScope.launch {
        val cur = todos.value.find { it.id == id } ?: return@launch
        todoDao.updateStatus(id, if (cur.status == "DONE") "PENDING" else "DONE")
    }
    fun writeTodayToSystemCalendar() = viewModelScope.launch {
        val now = Instant.now().toEpochMilli()
        CalendarHelper.insertEvent(app, "今日代办（示例）", now, now + 60*60*1000, "由 Todo AI Assistant 写入", 1L)
    }
    fun summarizeAndStore() = viewModelScope.launch {
        val baseUrl = settings.baseUrl.first()
        val apiKey = settings.apiKey.first()
        val model = settings.model.first()
        val proxy = settings.proxyJson.first()
        val gateway = settings.gatewayBasic.first()

        val type = Regex("\\\"type\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").find(proxy)?.groupValues?.get(1) ?: "NONE"
        val host = Regex("\\\"host\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").find(proxy)?.groupValues?.get(1)
        val port = Regex("\\\"port\\\"\\s*:\\s*(\\d+)").find(proxy)?.groupValues?.get(1)?.toIntOrNull()
        val puser = Regex("\\\"authUser\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").find(proxy)?.groupValues?.get(1)
        val ppass = Regex("\\\"authPass\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").find(proxy)?.groupValues?.get(1)

        val client = OpenAIClient(OpenAIConfig(
            baseUrl = baseUrl, apiKey = apiKey, model = model,
            proxyType = type, proxyHost = host, proxyPort = port,
            proxyUser = puser, proxyPass = ppass, gatewayBasic = gateway
        ))

        val titles = todos.value.take(20).joinToString(",") {
            "{\"title\":\"" + it.title.replace("\"","\\\"") + "\",\"status\":\"" + it.status + "\"}"
        }
        val payload = "{\"todos\":[$titles]}"
        val result = client.summarizeTodos(payload)
        val body = result.getOrElse { err -> "# 汇总失败\\n\\n" + (err.message ?: "unknown") }
        summaryDao.upsert(Summary(id = UUID.randomUUID().toString(), promptUsed = "todos->summary", summaryMd = body))
    }
    companion object {
        fun factory(appContext: Context) = object : ViewModelProvider.Factory {
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

@Composable
fun SettingsScreen(context: Context) {
    val repo = remember { SettingsRepo(context) }
    val scope = rememberCoroutineScope()

    var baseUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("gpt-4.1-mini") }
    var proxyJson by remember { mutableStateOf("{\"type\":\"NONE\"}") }
    var gatewayBasic by remember { mutableStateOf("") }
    var yaml by remember { mutableStateOf("") }
    var toast by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { repo.baseUrl.collect { baseUrl = it } }
    LaunchedEffect(Unit) { repo.apiKey.collect { apiKey = it } }
    LaunchedEffect(Unit) { repo.model.collect { model = it } }
    LaunchedEffect(Unit) { repo.proxyJson.collect { proxyJson = it } }
    LaunchedEffect(Unit) { repo.gatewayBasic.collect { gatewayBasic = it } }

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("OpenAI / 网关 / 代理设置", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(value = baseUrl, onValueChange = { baseUrl = it }, label = { Text("Base URL（可填网关）") }, singleLine = true)
        OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("API Key") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
        OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model（gpt-4.1-mini 等）") }, singleLine = true)
        OutlinedTextField(value = proxyJson, onValueChange = { proxyJson = it }, label = { Text("代理 JSON（{\"type\":\"HTTP\",\"host\":\"127.0.0.1\",\"port\":7890}）") }, singleLine = true)
        OutlinedTextField(value = gatewayBasic, onValueChange = { gatewayBasic = it }, label = { Text("网关 Basic（base64(user:pass)）") }, singleLine = true)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                scope.launch {
                    repo.save(baseUrl, apiKey, model, proxyJson, gatewayBasic)
                    toast = "已保存"
                }
            }) { Text("保存") }
            Button(onClick = {
                toast = if (baseUrl.isNotBlank() && apiKey.isNotBlank()) "基本检查通过（实际请求看“汇总”）" else "请填写 baseUrl 与 apiKey"
            }) { Text("连通性测试") }
        }

        Divider()
        Text("导入 YAML（可含 openai/proxy/gateway）")
        OutlinedTextField(value = yaml, onValueChange = { yaml = it }, label = { Text("粘贴 YAML 配置") }, minLines = 4)
        Button(onClick = {
            scope.launch {
                try {
                    repo.importYaml(yaml)
                    toast = "YAML 导入成功"
                } catch (e: Exception) {
                    toast = "YAML 导入失败：" + (e.message ?: "未知错误")
                }
            }
        }) { Text("导入 YAML") }

        if (toast.isNotBlank()) {
            Text("提示：$toast", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun SummaryScreen(vm: AppViewModel) {
    val list = vm.summaries.collectAsState(emptyList()).value
    Column(Modifier.padding(16.dp)) {
        Text("汇总记录", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        if (list.isEmpty()) {
            Text("暂无汇总记录")
        } else {
            LazyColumn {
                items(list) {
                    Text("— ${it.createdAt}: ${it.promptUsed}")
                    Spacer(Modifier.height(4.dp))
                    Text(it.summaryMd.take(1000))
                    Spacer(Modifier.height(12.dp))
                    Divider()
                }
            }
        }
    }
}
