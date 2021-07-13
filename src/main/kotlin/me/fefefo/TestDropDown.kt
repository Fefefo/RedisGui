package me.fefefo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands
import java.io.File

@Composable
fun DropDownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    list: MutableList<Array<String>>,
    current: MutableState<Array<String>>,
    commands: MutableState<RedisCommands<String, String>>,
    mainType: MutableState<Array<String>>,
    content: @Composable () -> Unit,
) {
    Box (Modifier.padding(end = 8.dp, top = 8.dp)){
        content()
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = Modifier
                .width(200.dp)
                .padding(0.dp)
                .background(
                    color = Color(230, 230, 230)
                )
        ) {
            list.forEach {
                if (it[1] != current.value[1]) {
                    DropdownMenuItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color.Blue
                            ),
                        onClick = {
                            current.value[0] = it[0]
                            current.value[1] = it[1]
                            commands.value = RedisClient.create(current.value[1]).connect().sync()
                            File("session.txt").writeText(current.value[0] + "," + current.value[1])
                            mainType.value = arrayOf("", "")
                        }
                    ) {
                        Text(
                            text = it[0],
                            style = TextStyle(fontWeight = FontWeight.Bold, color = Color.White),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TestDropdownMenu(
    list: MutableList<Array<String>>,
    current: MutableState<Array<String>>,
    commands: MutableState<RedisCommands<String, String>>,
    mainType: MutableState<Array<String>>
) {
    var expanded by remember { mutableStateOf(false) }

    DropDownMenu(
        expanded = expanded,
        onDismissRequest = {
            expanded = false
        },
    list = list,
    current = current,
    commands = commands, mainType =mainType) {
        Button(
            onClick = {
                expanded = true
            }
        ) {
            Text(text="REDIS SERVER ▼")
            //Icon(Icons.Default.MoreVert, contentDescription = null)
        }
    }
}