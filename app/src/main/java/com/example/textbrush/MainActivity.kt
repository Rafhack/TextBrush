package com.example.textbrush

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.textbrush.ui.theme.TextBrushTheme
import com.example.textbrush.view.TextBrushView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TextBrushTheme {
                var undoTrigger: Boolean by remember { mutableStateOf(false) }
                Content(undoTrigger) { undoTrigger = it }
            }
        }
    }
}

@Composable
fun Content(undoTrigger: Boolean, onUndoTrigger: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentScale = ContentScale.Crop,
            contentDescription = ""
        )
        TextBrush(undoTrigger, onUndoTrigger)
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = {
                onUndoTrigger(true)
            }) {
                Icon(
                    imageVector = Icons.Filled.Undo,
                    contentDescription = "Undo",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun TextBrush(undoTrigger: Boolean, onUndoTrigger: (Boolean) -> Unit) {
    AndroidView(
        factory = {
            TextBrushView(it, null)
        },
        update = {
            if (undoTrigger) {
                it.undo()
                onUndoTrigger(false)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TextBrushTheme {
        Content(false) {}
    }
}