package com.example.todoai.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todoai.R

@Composable
fun Header(modifier: Modifier = Modifier) {
    Row(modifier = modifier
        .fillMaxWidth()
        .height(56.dp)
        .background(MaterialTheme.colorScheme.primary),
        verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Logo",
            modifier = Modifier
                .padding(8.dp)
                .size(40.dp)
        )
        Text(
            text = "TODO AI Assistant",
            fontSize = 20.sp,
            color = Color.White,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}