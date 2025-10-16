package com.example.todoai.network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
data class OpenAIConfig(val baseUrl:String,val apiKey:String,val model:String,val proxyType:String="NONE",val proxyHost:String?=None,val proxyPort:Int?=None,val proxyUser:String?=None,val proxyPass:String?=None,val gatewayBasic:String?=None,val timeoutSec:Long=30)
class OpenAIClient(private val cfg: OpenAIConfig){ private val client: OkHttpClient by lazy{ val b=OkHttpClient.Builder().connectTimeout(cfg.timeoutSec,TimeUnit.SECONDS).readTimeout(cfg.timeoutSec,TimeUnit.SECONDS).writeTimeout(cfg.timeoutSec,TimeUnit.SECONDS).addInterceptor(Interceptor{chain-> val rb=chain.request().newBuilder().header("Authorization","Bearer ${cfg.apiKey}").header("Content-Type","application/json"); cfg.gatewayBasic?.takeIf{it.isNotBlank()}?.let{ rb.header("Proxy-Authorization","Basic $it")}; chain.proceed(rb.build()) }).addInterceptor(HttpLoggingInterceptor().apply{ level=HttpLoggingInterceptor.Level.BASIC}); if(cfg.proxyType!="NONE"&&cfg.proxyHost!=null&&cfg.proxyPort!=null){ val type= when(cfg.proxyType){"HTTP","HTTPS"->Proxy.Type.HTTP; "SOCKS5"->Proxy.Type.SOCKS; else->Proxy.Type.DIRECT}; if(type!=Proxy.Type.DIRECT){ b.proxy(Proxy(type, InetSocketAddress(cfg.proxyHost,cfg.proxyPort))); if(!cfg.proxyUser.isNullOrBlank()){ b.proxyAuthenticator{_,resp-> val cred=Credentials.basic(cfg.proxyUser!!, cfg.proxyPass?:""); resp.request.newBuilder().header("Proxy-Authorization",cred).build() } } } } b.build() }
 suspend fun summarizeTodos(jsonPayload:String):Result<String>= withContext(Dispatchers.IO){ try{ val url="${cfg.baseUrl.trimEnd('/')}/v1/chat/completions"; val payload="{\"model\":\"${cfg.model}\",\"messages\":[{\"role\":\"system\",\"content\":\"你是执行助理，擅长GTD与周报总结。输出Markdown和简要清单。\"},{\"role\":\"user\",\"content\":"+jsonPayload+"}],\"temperature\":0.2}"; val req=Request.Builder().url(url).post(payload.toRequestBody("application/json".toMediaType())).build(); client.newCall(req).execute().use{resp-> val body=resp.body?.string()?:""; if(!resp.isSuccessful) return@withContext Result.failure(IllegalStateException("HTTP ${resp.code}: "+body)); Result.success(body) } }catch(e:Exception){ Result.failure(e) } }
}
