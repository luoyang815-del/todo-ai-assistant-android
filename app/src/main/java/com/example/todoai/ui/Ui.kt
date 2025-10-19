package com.example.todoai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.todoai.net.OpenAIClient
import com.example.todoai.todo.TodoRepository

@Composable
fun Ui(
    repo: TodoRepository,
    ai: OpenAIClient,
    modifier: Modifier = Modifier
) {
    var input by remember { mutableStateOf(TextFieldValue("")) }
    var important by remember { mutableStateOf(false) }
    var aiReply by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(repo.list()) }

    Column(modifier.padding(16.dp)) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("输入代办或向 AI 询问（点击下方按钮发送）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Checkbox(checked = important, onCheckedChange = { important = it })
                Text("标记为重要")
            }
            Button(
                onClick = {
                    val text = input.text.trim()
                    if (text.isNotEmpty()) {
                        repo.add(text, important)
                        items = repo.list()
                        input = TextFieldValue("")
                    }
                }
            ) { Text("添加代办") }
        }

        Row(Modifier.padding(top = 8.dp)) {
            Button(onClick = {
                val q = input.text.trim().ifEmpty { "请帮我优化今天的代办清单排期。" }
                try { aiReply = ai.chat(q) } catch (e: Exception) { aiReply = "请求失败：" + (e.message ?: "") }
            }) { Text("发送到 OpenAI") }
        }

        if (aiReply.isNotBlank()) {
            Text("AI 回复：", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
            Text(aiReply, modifier = Modifier.padding(top = 4.dp))
        }

        Divider(Modifier.padding(vertical = 12.dp))
        Text("清单（重要全部 + 普通前 10 条）", style = MaterialTheme.typography.titleMedium)

        val importantList = items.filter { it.important }
        val normalList = items.filter { !it.important }.take(10)

        LazyColumn(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            if (importantList.isNotEmpty()) {
                item { Text("重要（" + importantList.size + "）", color = MaterialTheme.colorScheme.error) }
                items(importantList) { it -> Text("• " + it.title) }
            }
            item { Spacer(Modifier.height(8.dp)) }
            item { Text("普通（最多展示 10 条）") }
            items(normalList) { it -> Text("• " + it.title) }
        }
    }
}