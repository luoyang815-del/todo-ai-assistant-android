package com.example.todoai.data
import android.content.Context
import android.util.Base64
class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    var gatewayEnabled: Boolean
        get() = sp.getBoolean("gateway_enabled", false)
        set(v) { sp.edit().putBoolean("gateway_enabled", v).apply() }
    var gatewayBaseUrl: String
        get() = sp.getString("gateway_base_url", "") ?: ""
        set(v) { sp.edit().putString("gateway_base_url", v).apply() }
    var gatewayUser: String
        get() = sp.getString("gateway_user", "") ?: ""
        set(v) { sp.edit().putString("gateway_user", v).apply() }
    var gatewayPass: String
        get() = sp.getString("gateway_pass", "") ?: ""
        set(v) { sp.edit().putString("gateway_pass", v).apply() }
    var proxyType: String
        get() = sp.getString("proxy_type", "none") ?: "none"
        set(v) { sp.edit().putString("proxy_type", v).apply() }
    var proxyHost: String
        get() = sp.getString("proxy_host", "") ?: ""
        set(v) { sp.edit().putString("proxy_host", v).apply() }
    var proxyPort: Int
        get() = sp.getInt("proxy_port", 0)
        set(v) { sp.edit().putInt("proxy_port", v).apply() }
    var openaiApiKey: String
        get() = sp.getString("openai_api_key", "") ?: ""
        set(v) { sp.edit().putString("openai_api_key", v).apply() }
    var model: String
        get() = sp.getString("model", "gpt-4o-mini") ?: "gpt-4o-mini"
        set(v) { sp.edit().putString("model", v).apply() }
    fun basicAuthHeader(): String? {
        val user = gatewayUser
        val pass = gatewayPass
        if (user.isBlank() && pass.isBlank()) return null
        val token = Base64.encodeToString(f"{user}:{pass}".encode(), Base64.NO_WRAP).decode()
        return "Basic " + token
    }
}
