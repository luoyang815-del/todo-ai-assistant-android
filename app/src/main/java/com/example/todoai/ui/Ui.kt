package com.example.todoai.ui
import android.app.Application
import android.content.Context
import android.widget.Toast
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
import com.example.todoai.widget.TodoWidgetProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.json.JSONObject
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
    TodoWidgetProvider.notifyChanged(app)
  }
  fun toggleDone(id: String) = viewModelScope.launch {
    val cur = todos.value.find { it.id == id } ?: return@launch
    todoDao.updateStatus(id, if (cur.status == "DONE") "PENDING" else "DONE")
    TodoWidgetProvider.notifyChanged(app)
  }
  fun writeOptimizedCalendar() = viewModelScope.launch {
    val list = todos.value.filter { it.status != "DONE" }
    val todosJson = list.joinToString(",", prefix = "[", postfix = "]") {
      "{\"title\":\"" + it.title.replace("\\","\\\\").replace("\"","\\\"") + "\",\"priority\":\"" + it.priority + "\"}"
    }
    val cfg = loadCfg(); val client = OpenAIClient(cfg)
    val planResp = client.planCalendarEvent("{\"todos\":" + todosJson + "}").getOrElse {
      Toast.makeText(app, "AI 规划失败：" + (it.message ?: "unknown"), Toast.LENGTH_SHORT).show(); return@launch
    }
    val (title, desc, startMs, endMs) = try {
      val jsonStart = planResp.indexOf('{'); val jsonEnd = planResp.lastIndexOf('}')
      val obj = JSONObject(planResp.substring(jsonStart, jsonEnd+1))
      Quad(obj.optString("title","今日代办"), obj.optString("description",""), obj.optLong("startEpochMs",0), obj.optLong("endEpochMs",0))
    } catch (_: Exception) { Quad("今日代办","",0,0) }
    val now = Instant.now().toEpochMilli()
    val begin = if (startMs > 0) startMs else now
    val end = if (endMs > begin) endMs else (begin + 60*60*1000)
    val eid = CalendarHelper.insertEvent(app, title, begin, end, desc, 1L)
    Toast.makeText(app, if (eid != null) "日历已写入：" + title else "写入日历失败（权限或日历不存在）", Toast.LENGTH_SHORT).show()
  }
  data class Quad(val a:String,val b:String,val c:Long,val d:Long)
  private suspend fun loadCfg(): OpenAIConfig {
    val baseUrl = settings.baseUrl.first(); val apiKey = settings.apiKey.first(); val model = settings.model.first()
    val proxy = settings.proxyJson.first(); val gateway = settings.gatewayBasic.first()
    fun find(re:String): String? { return Regex(re).find(proxy)?.groupValues?.getOrNull(1) }
    val type = find("\\"type\\"\s*:\s*\\"([^\\"]+)\\"") ?: "NONE"
    val host = find("\\"host\\"\s*:\s*\\"([^\\"]+)\\"")
    val port = find("\\"port\\"\s*:\s*(\d+)")?.toIntOrNull()
    val puser = find("\\"authUser\\"\s*:\s*\\"([^\\"]+)\\"")
    val ppass = find("\\"authPass\\"\s*:\s*\\"([^\\"]+)\\"")
    return OpenAIConfig(baseUrl, apiKey, model, type, host, port, puser, ppass, gateway)
  }
  fun summarizeAndStore() = viewModelScope.launch {
    val cfg = loadCfg(); val client = OpenAIClient(cfg)
    val titles = todos.value.take(20).joinToString(","){ "{\"title\":\"" + it.title.replace("\\","\\\\").replace("\"","\\\"") + "\",\"status\":\"" + it.status + "\"}" }
    val payload = "{\"todos\":[${titles}]}"
    val result = client.summarizeTodos(payload)
    val body = result.getOrElse { err -> "# 汇总失败\n\n" + (err.message ?: "unknown") }
    summaryDao.upsert(Summary(id = UUID.randomUUID().toString(), promptUsed = "todos->summary", summaryMd = body))
  }
  fun testConnectivity(onResult: (Boolean, String) -> Unit) = viewModelScope.launch {
    val cfg = loadCfg(); val client = OpenAIClient(cfg); val r = client.pingGateway()
    onResult(r.isSuccess, r.fold(onSuccess = { it }, onFailure = { it.message ?: "error" }))
  }
  companion object {
    fun factory(appContext: Context) = object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T {
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
    Text(text = ("[" + todo.priority + "] " + todo.title), style = MaterialTheme.typography.bodyLarge)
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
  var toast by remember { mutableStateOf("") }
  var testing by remember { mutableStateOf(false) }
  var testMsg by remember { mutableStateOf("") }
  LaunchedEffect(Unit) { repo.baseUrl.collect { baseUrl = it } }
  LaunchedEffect(Unit) { repo.apiKey.collect { apiKey = it } }
  LaunchedEffect(Unit) { repo.model.collect { model = it } }
  LaunchedEffect(Unit) { repo.proxyJson.collect { proxyJson = it } }
  LaunchedEffect(Unit) { repo.gatewayBasic.collect { gatewayBasic = it } }
  val vm: AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = AppViewModel.factory(context))
  Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text("OpenAI / 网关 / 代理设置", style = MaterialTheme.typography.titleLarge)
    Text("说明：当 Base URL 填写你的网关地址，且“网关 Basic”非空时，所有请求将通过网关转发。",
      style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
    OutlinedTextField(value = baseUrl, onValueChange = { baseUrl = it }, label = { Text("Base URL（例如 https://my-gateway.company.com/openai）") }, singleLine = true)
    OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("API Key") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
    OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model（如 gpt-4.1-mini / gpt-4.1 / o4-mini）") }, singleLine = true)
    OutlinedTextField(value = proxyJson, onValueChange = { proxyJson = it }, label = { Text("代理 JSON（{\"type\":\"NONE\"} 或 {\"type\":\"HTTP\",\"host\":\"127.0.0.1\",\"port\":7890}）") }, singleLine = true)
    OutlinedTextField(value = gatewayBasic, onValueChange = { gatewayBasic = it }, label = { Text("网关 Basic（base64(user:pass)）") }, singleLine = true)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Button(onClick = { scope.launch { repo.save(baseUrl, apiKey, model, proxyJson, gatewayBasic); toast = "已保存" } }) { Text("保存") }
      Button(enabled = !testing, onClick = {
        testing = true; testMsg = "测试中..."
        vm.testConnectivity { ok, msg -> testing = false; testMsg = ( "✅ 网关可用：" if ok else "❌ 网关失败：" ) + msg }
      }) { Text(if (testing) "测试中..." else "连通性测试") }
    }
    if (toast.isNotBlank()) Text("提示："+toast, color = MaterialTheme.colorScheme.primary)
    if (testMsg.isNotBlank()) Text(testMsg, color = if (testMsg.startswith("✅")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
  }
}
@Composable
fun SummaryScreen(vm: AppViewModel) {
  val list = vm.summaries.collectAsState(emptyList()).value
  Column(Modifier.padding(16.dp)) {
    Text("汇总记录", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(8.dp))
    if (list.isEmpty()) { Text("暂无汇总记录") } else {
      list.forEach {
        Text("— " + it.createdAt + ": " + it.promptUsed)
        Spacer(Modifier.height(4.dp)); Text(it.summaryMd.take(1000)); Spacer(Modifier.height(12.dp)); Divider()
      }
    }
  }
}
