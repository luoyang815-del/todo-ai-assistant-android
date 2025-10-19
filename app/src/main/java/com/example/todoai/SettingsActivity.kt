package com.example.todoai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todoai.data.Prefs

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(this)
        setContent {
            MaterialTheme {
                Surface {
                    SettingsScreen(prefs)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(prefs: Prefs) {
    var apiKey by remember { mutableStateOf(prefs.openaiApiKey) }
    var model by remember { mutableStateOf(prefs.model) }
    var useGateway by remember { mutableStateOf(prefs.gatewayEnabled) }
    var baseUrl by remember { mutableStateOf(prefs.gatewayBaseUrl) }
    var user by remember { mutableStateOf(prefs.gatewayUser) }
    var pass by remember { mutableStateOf(prefs.gatewayPass) }
    var proxyType by remember { mutableStateOf(prefs.proxyType) }
    var proxyHost by remember { mutableStateOf(prefs.proxyHost) }
    var proxyPort by remember { mutableStateOf(prefs.proxyPort.toString()) }

    Column(Modifier.padding(16.dp)) {
        Text("设置", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = apiKey, onValueChange = { apiKey = it },
            label = { Text("OpenAI API Key") },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        OutlinedTextField(
            value = model, onValueChange = { model = it },
            label = { Text("Model（如 gpt-4o-mini）") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        Row(Modifier.padding(top = 8.dp)) {
            Checkbox(checked = useGateway, onCheckedChange = { useGateway = it })
            Text("使用自定义网关")
        }

        if (useGateway) {
            OutlinedTextField(
                value = baseUrl, onValueChange = { baseUrl = it },
                label = { Text("网关 Base URL") },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
            OutlinedTextField(
                value = user, onValueChange = { user = it },
                label = { Text("网关用户名") },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
            OutlinedTextField(
                value = pass, onValueChange = { pass = it },
                label = { Text("网关密码") },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        }

        OutlinedTextField(
            value = proxyType, onValueChange = { proxyType = it },
            label = { Text("代理类型（none/http/https/socks）") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        OutlinedTextField(
            value = proxyHost, onValueChange = { proxyHost = it },
            label = { Text("代理主机") },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        )
        OutlinedTextField(
            value = proxyPort, onValueChange = { proxyPort = it.filter { ch -> ch.isDigit() } },
            label = { Text("代理端口") },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        )

        Button(
            onClick = {
                prefs.openaiApiKey = apiKey
                prefs.model = model
                prefs.gatewayEnabled = useGateway
                prefs.gatewayBaseUrl = baseUrl
                prefs.gatewayUser = user
                prefs.gatewayPass = pass
                prefs.proxyType = proxyType
                prefs.proxyHost = proxyHost
                prefs.proxyPort = proxyPort.toIntOrNull() ?: 0
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("保存")
        }
    }
}