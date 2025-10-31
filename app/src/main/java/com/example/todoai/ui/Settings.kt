package com.example.todoai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var useCustomGateway by remember { mutableStateOf(false) }
    var gatewayUrl by remember { mutableStateOf("") }
    var openAiKey by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("gpt-3.5-turbo") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "设置", style = MaterialTheme.typography.titleLarge)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "使用自定义网关")
            Switch(checked = useCustomGateway, onCheckedChange = { useCustomGateway = it })
        }

        if (useCustomGateway) {
            OutlinedTextField(
                value = gatewayUrl,
                onValueChange = { gatewayUrl = it },
                label = { Text("网关地址") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            OutlinedTextField(
                value = openAiKey,
                onValueChange = { openAiKey = it },
                label = { Text("OpenAI API Key") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "选择模型")
        DropdownMenu(expanded = true, onDismissRequest = { }) {
            DropdownMenuItem(text = { Text("gpt-3.5-turbo") }, onClick = { selectedModel = "gpt-3.5-turbo" })
            DropdownMenuItem(text = { Text("gpt-4") }, onClick = { selectedModel = "gpt-4" })
        }
    }
}