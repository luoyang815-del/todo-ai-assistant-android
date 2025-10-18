package com.example.todoai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.todoai.network.OpenAIClient
import com.example.todoai.todo.TodoRepository

@Composable
fun Ui(repo: TodoRepository, ai: OpenAIClient, modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf(TextFieldValue("")) }
    var important by remember { mutableStateOf(false) }
    var aiReply by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(repo.list()) }

    Column(modifier.padding(16.dp)) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("杈撳叆浠ｅ姙鎴栧悜 AI 璇㈤棶锛堢偣鍑讳笅鏂规寜閽彂閫侊級") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Row {
                Checkbox(checked = important, onCheckedChange = { important = it })
                Text("鏍囪涓洪噸瑕?)
            }
            Button(onClick = {
                val text = input.text.trim()
                if (text.isNotEmpty()) {
                    repo.add(text, important)
                    items = repo.list()
                    input = TextFieldValue("")
                }
            }) { Text("娣诲姞浠ｅ姙") }
        }
        Row(Modifier.padding(top = 8.dp)) {
            Button(onClick = {
                val q = input.text.trim().ifEmpty { "璇峰府鎴戜紭鍖栦粖澶╃殑浠ｅ姙娓呭崟鎺掓湡銆? }
                try { aiReply = ai.chat(q) } catch (e: Exception) { aiReply = "璇锋眰澶辫触锛? + e.message }
            }) { Text("鍙戦€佸埌 OpenAI") }
        }
        if (aiReply.isNotBlank()) {
            Text("AI 鍥炲锛?, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
            Text(aiReply, modifier = Modifier.padding(top = 4.dp))
        }
        Divider(Modifier.padding(vertical = 12.dp))
        Text("娓呭崟锛堥噸瑕佸叏閮?+ 鏅€氬墠 10 鏉★級", style = MaterialTheme.typography.titleMedium)

        val importantList = items.filter { it.important }
        val normalList = items.filter { !it.important }.take(10)

        LazyColumn(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            if (importantList.isNotEmpty()) {
                item { Text("閲嶈锛? + importantList.size + "锛?, color = MaterialTheme.colorScheme.error) }
                items(importantList) { it -> Text("鈥?" + it.title) }
            }
            item { Spacer(Modifier.height(8.dp)) }
            item { Text("鏅€氾紙鏈€澶氬睍绀?10 鏉★級") }
            items(normalList) { it -> Text("鈥?" + it.title) }
        }
    }
}

