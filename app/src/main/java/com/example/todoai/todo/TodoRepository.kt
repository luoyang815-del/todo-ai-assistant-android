package com.example.todoai.todo
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
data class TodoItem(val id: Long, val title: String, val important: Boolean = false)
class TodoRepository(ctx: Context) {
    private val sp = ctx.getSharedPreferences("todo", Context.MODE_PRIVATE)
    fun list(): List<TodoItem> {
        val raw = sp.getString("items", "[]") ?: "[]"
        val arr = JSONArray(raw)
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(TodoItem(o.getLong("id"), o.getString("title"), o.optBoolean("important", false)))
            }
        }
    }
    fun add(title: String, important: Boolean) {
        val list = list().toMutableList()
        list.add(0, TodoItem(System.currentTimeMillis(), title, important))
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id)
                put("title", it.title)
                put("important", it.important)
            })
        }
        sp.edit().putString("items", arr.toString()).apply()
    }
}
