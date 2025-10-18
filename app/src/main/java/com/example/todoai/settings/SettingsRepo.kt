package com.example.todoai.settings
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.yaml.snakeyaml.Yaml
private val Context.dataStore by preferencesDataStore(name = "settings")
object Keys {
  val OPENAI_BASE_URL = stringPreferencesKey("openai_base_url")
  val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
  val OPENAI_MODEL = stringPreferencesKey("openai_model")
  val PROXY_JSON = stringPreferencesKey("proxy_json")
  val GATEWAY_BASIC = stringPreferencesKey("gateway_basic")
}
class SettingsRepo(private val context: Context) {
  val baseUrl: Flow<String> = context.dataStore.data.map { it[Keys.OPENAI_BASE_URL] ?: "https://api.openai.com" }
  val apiKey: Flow<String> = context.dataStore.data.map { it[Keys.OPENAI_API_KEY] ?: "" }
  val model: Flow<String> = context.dataStore.data.map { it[Keys.OPENAI_MODEL] ?: "gpt-4.1-mini" }
  val proxyJson: Flow<String> = context.dataStore.data.map { it[Keys.PROXY_JSON] ?: "{\"type\":\"NONE\"}" }
  val gatewayBasic: Flow<String> = context.dataStore.data.map { it[Keys.GATEWAY_BASIC] ?: "" }
  suspend fun save(baseUrl: String, apiKey: String, model: String, proxyJson: String, gatewayBasic: String) {
    context.dataStore.edit {
      it[Keys.OPENAI_BASE_URL] = baseUrl
      it[Keys.OPENAI_API_KEY] = apiKey
      it[Keys.OPENAI_MODEL] = model
      it[Keys.PROXY_JSON] = proxyJson
      it[Keys.GATEWAY_BASIC] = gatewayBasic
    }
  }
  @Suppress("UNCHECKED_CAST")
  suspend fun importYaml(yamlText: String) {
    val map = Yaml().load<Map<String, Any?>>(yamlText) ?: emptyMap()
    val openai = map["openai"] as? Map<String, Any?> ?: emptyMap()
    val proxy = map["proxy"] as? Map<String, Any?> ?: emptyMap()
    val gateway = map["gateway"] as? Map<String, Any?> ?: emptyMap()
    save((openai["base_url"] as? String) ?: "https://api.openai.com",
         (openai["api_key"] as? String) ?: "",
         (openai["model"] as? String) ?: "gpt-4.1-mini",
         buildProxyJson(proxy),
         (gateway["basic"] as? String) ?: "")
  }
  private fun buildProxyJson(proxy: Map<String, Any?>): String {
    fun anyToInt(x: Any?) = when (x) { null -> null; is Number -> x.toInt(); is String -> x.toIntOrNull(); else -> null }
    val type = proxy["type"]?.toString() ?: "NONE"
    val host = proxy["host"]?.toString()
    val port = anyToInt(proxy["port"])
    val user = proxy["auth_user"]?.toString()
    val pass = proxy["auth_pass"]?.toString()
    val parts = mutableListOf<String>()
    parts += "\"type\":\"$type\""
    host?.let { parts += "\"host\":\"$it\"" }
    port?.let { parts += "\"port\":$port" }
    user?.let { parts += "\"authUser\":\"$user\"" }
    pass?.let { parts += "\"authPass\":\"$pass\"" }
    return "{"+parts.joinToString(",")+"}"
  }
}
