package com.example.todoaiassist.net

import com.example.todoaiassist.data.Prefs
import com.example.todoaiassist.notify.Notifier
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.InetSocketAddress
import java.net.Proxy

class OpenAIClient(private val prefs: Prefs, private val notifier: Notifier) {

    private fun buildClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .callTimeout(java.time.Duration.ofSeconds(60))
            .connectTimeout(java.time.Duration.ofSeconds(30))
            .readTimeout(java.time.Duration.ofSeconds(60))
            .writeTimeout(java.time.Duration.ofSeconds(60))

        when (prefs.proxyType.lowercase()) {
            "http", "https" -> builder.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(prefs.proxyHost, prefs.proxyPort)))
            "socks" -> builder.proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress(prefs.proxyHost, prefs.proxyPort)))
        }
        return builder.build()
    }

    fun chat(prompt: String): String {
        val client = buildClient()
        val baseUrl = if (prefs.gatewayEnabled && prefs.gatewayBaseUrl.isNotBlank())
            prefs.gatewayBaseUrl.trimEnd('/')
        else
            "https://api.openai.com"

        val url = "$baseUrl/v1/chat/completions"

        val payload = buildString {
            append("{"model":"")
            append(prefs.model)
            append("","messages":[{"role":"user","content":")
            append(jsonString(prompt))
            append("}],"temperature":0.2}")
        }

        val reqBuilder = Request.Builder()
            .url(url)
            .post(payload.toRequestBody("application/json".toMediaType()))

        if (!prefs.gatewayEnabled) {
            reqBuilder.addHeader("Authorization", "Bearer ${prefs.openaiApiKey}")
        } else {
            prefs.basicAuthHeader()?.let { reqBuilder.addHeader("Authorization", it) }
        }

        client.newCall(reqBuilder.build()).execute().use { resp ->
            if (!resp.isSuccessful) throw IllegalStateException("HTTP ${resp.code}: ${resp.message}")
            val body = resp.body?.string().orEmpty()
            val text = body.substringAfter(""content":"", missingDelimiterValue = "").substringBefore(""").replace("\n", "
").replace("\"", """)
            val finalText = if (text.isBlank()) "（AI 无回复内容）" else text
            notifier.notifyAIReply(finalText)
            return finalText
        }
    }

    private fun jsonString(s: String): String {
        return """ + s.replace("\", "\\").replace(""", "\"").replace("
", "\n") + """
    }
}
