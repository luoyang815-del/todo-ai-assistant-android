package com.example.todoai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.todoai.ui.Ui
import com.example.todoai.data.Prefs
import com.example.todoai.notify.Notifier
import com.example.todoai.net.OpenAIClient
import com.example.todoai.todo.TodoRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prepare dependencies
        val prefs = Prefs(this)
        val notifier = Notifier(this)
        val ai = OpenAIClient(prefs, notifier)
        val repo = TodoRepository()

        setContent {
            MaterialTheme {
                Surface {
                    Ui(repo = repo, ai = ai)
                }
            }
        }
    }
}