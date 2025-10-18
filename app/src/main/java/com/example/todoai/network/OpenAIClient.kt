package com.example.todoai.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

data class OpenAIConfig(
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val proxyType: String = "NONE",
    val proxyHost: String? = null,
    val proxyPort: Int? = null,
    val proxyUser: String? = null,
    val proxyPass: String? = null,
    val gatewayBasic: String? = null,
    val timeoutSec: Long = 30
)

class OpenAIClient(private val cfg: OpenAIConfig) {

    private val client: OkHttpClient by lazy {
        val b = OkHttpClient.Builder()
            .connectTimeout(cfg.timeoutSec, TimeUnit.SECONDS)
            .readTimeout(cfg.timeoutSec, TimeUnit.SECONDS)
            .writeTimeout(cfg.timeoutSec, TimeUnit.SECONDS)
            .addInterceptor(Interceptor { chain ->
                val rb = chain.request().newBuilder()
                    .header("Authorization", "Bearer ${cfg.apiKey}")
                    .header("Content-Type", "application/json")
                cfg.gatewayBasic?.takeIf { it.isNotBlank() }?.let {
                    // 你的网关需要 Basic，则这里会自动加头
                    rb.header("Proxy-Authorization", "Basic $it")
                }
                chain.proceed(rb.build())
            })
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })

        if (cfg.proxyType != "NONE" && cfg.proxyHost != null && cfg.proxyPort != null) {
            val type = when (cfg.proxyType) {
                "HTTP", "HTTPS" -> Proxy.Type.HTTP
                "SOCKS5" -> Proxy.Type.SOCKS
                else -> Proxy.Type.DIRECT
            }
            if (type != Proxy.Type.DIRECT) {
                b.proxy(Proxy(type, InetSocketAddress(cfg.proxyHost, cfg.proxyPort)))
                if (!cfg.proxyUser.isNullOrBlank()) {
                    b.proxyAuthenticator { _, response ->
                        val cred = Credentials.basic(cfg.proxyUser, cfg.proxyPass ?: "")
                        response.request.newBuilder().header("Proxy-Authorization", cred).build()
                    }
                }
            }
        }
        b.build()
    }

    /** 网关连通性测试：GET {baseUrl}/ping */
    suspend fun pingGateway(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = cfg.baseUrl.trimEnd('/') + "/ping"
            val req = Request.Builder().url(url).get().build()
            client.newCall(req).execute().use { resp ->
                val body = resp.body?.string() ?: ""
                if (!resp.isSuccessful) return@withContext Result.failure(IllegalStateException("HTTP ${resp.code}: $body"))
                Result.success(if (body.isBlank()) "OK" else body)
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    /** 常规汇总（写“汇总记录”用） */
    suspend fun summarizeTodos(jsonPayload: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "${cfg.baseUrl.trimEnd('/')}/v1/chat/completions"
            val body = """
                {
                  "model": "${cfg.model}",
                  "messages": [
                    {"role":"system","content":"你是执行助理，擅长GTD与周报总结。输出Markdown和简要清单。"},
                    {"role":"user","content": $jsonPayload }
                  ],
                  "temperature": 0.2
                }
            """.trimIndent()
            val req = Request.Builder().url(url).post(body.toRequestBody("application/json".toMediaType())).build()
            client.newCall(req).execute().use { resp ->
                val respBody = resp.body?.string() ?: ""
                if (!resp.isSuccessful) return@withContext Result.failure(IllegalStateException("HTTP ${resp.code}: $respBody"))
                Result.success(respBody)
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    /** 计划日历事件：要求模型输出 {title, description, startEpochMs, endEpochMs} 的 JSON */
    suspend fun planCalendarEvent(todosJson: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "${cfg.baseUrl.trimEnd('/')}/v1/chat/completions"
            val prompt = """
                你将收到一个 todos 列表(JSON)。请输出 JSON（不要解释文本），字段：
                - title: string，适合写入系统日历的事件标题（不超过 30 字）。
                - description: string，简洁摘要（不超过 200 字）。
                - startEpochMs: number，建议的开始时间（毫秒时间戳）。
                - endEpochMs: number，建议的结束时间（毫秒时间戳，>start）。
                如果无法判断时间，请将 start/end 设为 0。
                todos: $todosJson
            """.trimIndent()
            val body = """
                {"model":"${cfg.model}","messages":[
                  {"role":"system","content":"你是资深日程助理。"},
                  {"role":"user","content": ${org.json.JSONObject.quote(prompt)} }
                ],"temperature":0.2}
            """.trimIndent()
            val req = Request.Builder().url(url).post(body.toRequestBody("application/json".toMediaType())).build()
            client.newCall(req).execute().use { resp ->
                val respBody = resp.body?.string() ?: ""
                if (!resp.isSuccessful) return@withContext Result.failure(IllegalStateException("HTTP ${resp.code}: $respBody"))
                Result.success(respBody)
            }
        } catch (e: Exception) { Result.failure(e) }
    }
}
