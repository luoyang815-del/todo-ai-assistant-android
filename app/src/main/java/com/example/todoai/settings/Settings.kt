
package com.example.todoai.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.yaml.snakeyaml.Yaml
import kotlinx.serialization.json.*

private val Context.dataStore by preferencesDataStore(name = "settings")

object Keys {
    val OPENAI_BASE_URL = stringPreferencesKey("openai_base_url")
    val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
    val OPENAI_MODEL = stringPreferencesKey("openai_model")
    val PROXY_JSON = stringPreferencesKey("proxy_json")
    val GATEWAY_BASIC = stringPreferencesKey("gateway_basic")
}

data class ProxyCfg(
    val type: String = "NONE",
    val host: String? = null,
    val port: Int? = null,
    val authUser: String? = null,
    val authPass: String? = null
)

class SettingsRepo(private val context: Context) {
    val baseUrl: Flow<String> = context.dataStore.data.map { it[Keys.OPENAI_BASE_URL] ?: "https://api.openai.com" }
    val apiKey: Flow<String> = context.dataStore.data.map { it[Keys.OPENAI_API_KEY] ?: "" }
    val model: Flow<String> = context.dataStore.data.map { it[Keys.OPENAI_MODEL] ?: "gpt-4.1-mini" }
    val proxy: Flow<String> = context.dataStore.data.map { it[Keys.PROXY_JSON] ?: "{"type":"NONE"}" }
    val gatewayBasic: Flow<String> = context.dataStore.data.map { it[Keys.GATEWAY_BASIC] ?: "" }

    suspend fun importYaml(yamlText: String) {
        val map = Yaml().load<Map<String, Any?>>(yamlText) ?: emptyMap()
        val openai = map["openai"] as? Map<String, Any?> ?: emptyMap()
        val proxy = map["proxy"] as? Map<String, Any?> ?: emptyMap()
        val gateway = map["gateway"] as? Map<String, Any?> ?: emptyMap()

        context.dataStore.edit {
            it[Keys.OPENAI_BASE_URL] = (openai["base_url"] as? String) ?: "https://api.openai.com"
            it[Keys.OPENAI_API_KEY] = (openai["api_key"] as? String) ?: ""
            it[Keys.OPENAI_MODEL] = (openai["model"] as? String) ?: "gpt-4.1-mini"
            val pj = buildJsonObject {
                proxy["type"]?.let { v -> put("type", JsonPrimitive(v.toString())) }
                proxy["host"]?.let { v -> put("host", JsonPrimitive(v.toString())) }
                proxy["port"]?.let { v -> put("port", JsonPrimitive(((v as? Number)?.toInt()) ?: 0)) }
                proxy["auth_user"]?.let { v -> put("authUser", JsonPrimitive(v.toString())) }
                proxy["auth_pass"]?.let { v -> put("authPass", JsonPrimitive(v.toString())) }
            }
            it[Keys.PROXY_JSON] = Json.encodeToString(JsonObject.serializer(), pj)
            it[Keys.GATEWAY_BASIC] = (gateway["basic"] as? String) ?: ""
        }
    }
}
