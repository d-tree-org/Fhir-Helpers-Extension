package codeviewer.ui.debugger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.consumeAsFlow
import repositories.LoggerRepository

@Composable
fun LoggerView(loggerRepository: LoggerRepository) {
    val flow by loggerRepository.channel.consumeAsFlow().collectAsState("")
    var messages by remember {
        mutableStateOf(listOf<String>())
    }

    LaunchedEffect(flow) {
        val newList = messages.toMutableList()
        newList.add(flow)
        messages = newList
    }
    LazyColumn(Modifier.background(Color.Blue).fillMaxSize()) {
        items(messages) { message ->
            Text(message, modifier = Modifier.fillMaxWidth())
        }
    }
}