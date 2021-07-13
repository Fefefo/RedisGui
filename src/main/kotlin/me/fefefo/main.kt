package me.fefefo

import androidx.compose.desktop.DesktopTheme
import androidx.compose.desktop.Window
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands
import java.io.File


fun main() = Window(title = "Redis GUI", size = IntSize(1600, 900)) {

    val urls: MutableList<Array<String>> = ArrayList()
    File("redis.csv").forEachLine {
        val a = it.split(',')
        urls.add(arrayOf(a[0], a[1]))
    }
    val usedServer = remember { mutableStateOf(arrayOf("", ""))}
    val sessionFile = File("session.txt")
    usedServer.value = if (sessionFile.exists()) {
        val text = sessionFile.readText()
        arrayOf(text.split(',')[0],text.split(',')[1])
    } else {
        sessionFile.writeText(urls[0][0] + "," + urls[0][1])
        urls[0]
    }

    val redisClient =
        RedisClient.create(usedServer.value[1])
    val connection = redisClient.connect()
    val syncCommands = remember { mutableStateOf(connection.sync()) }

    MaterialTheme {
        DesktopTheme {

            val mainType = remember { mutableStateOf(arrayOf("", "")) }
            Row(
                modifier = Modifier.fillMaxSize()
                    .background(color = Color(180, 180, 180))
                    .padding(4.dp)
            ) {
                ButtonsSide(syncCommands, mainType, usedServer.value[0])

                DisplayPanel(
                    modifier = Modifier.fillMaxSize(),
                    mainType,
                    syncCommands
                )
            }
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.End) {
                TestDropdownMenu(list = urls, current = usedServer, commands = syncCommands, mainType =mainType)
            }
        }
    }
}

@Composable
fun DisplayPanel(
    modifier: Modifier,
    mainOutput: MutableState<Array<String>>,
    syncCommands: MutableState<RedisCommands<String, String>>
) {
    if (mainOutput.value[0] == "string") {
        Column(
            modifier = modifier
                .fillMaxSize()
                .border(color = Color.Gray, width = 1.dp)
                .padding(end = 10.dp, start = 10.dp, top = 40.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {
            TextField(
                value = syncCommands.value.get(mainOutput.value[1]),
                textStyle = TextStyle(color = Color.DarkGray),
                onValueChange = { },
                label = { Text(mainOutput.value[1]) }
            )
        }
    } else if (mainOutput.value[0] == "hash")
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(end=30.dp, start=30.dp),
            verticalArrangement = Arrangement.Center

        ) {
            val hgetall = syncCommands.value.hgetall(mainOutput.value[1])
            var light = false
            Row(
                Modifier
                    .border(color = Color.Red, width = 1.dp)
                    .background(Color.Red)
                    .fillMaxWidth()
                    .height(50.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(text = "KEY", style = TextStyle(Color.White, fontWeight = FontWeight.Bold))
                Text(text = "VALUE", style = TextStyle(Color.White, fontWeight = FontWeight.Bold))
            }
            hgetall.forEach { (k, v) ->
                light = !light
                print("CIAO $k $v")
                Row(
                    Modifier
                        .background(if (light) Color(180, 180, 180) else Color(170, 170, 170))
                        .fillMaxWidth()
                        .height(30.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                        Text(text = k)
                        Text(text = v)
                }
            }
        }
}

@Composable
fun ButtonsSide(syncCommands: MutableState<RedisCommands<String, String>>, mainType: MutableState<Array<String>>, server: String) {
    val verticalState = rememberScrollState(0)
    Box {
        val keys = syncCommands.value.keys("*")
        val endPadding = if (keys.size < 20) 10.dp else 20.dp
        Column(
            modifier = Modifier
                .width(275.dp)
                .background(color = Color(140, 140, 140))
                .fillMaxSize()
                .border(color = Color.Gray, width = 1.dp)
                .padding(end = endPadding, start = 10.dp, top = 10.dp, bottom = 10.dp)
                .verticalScroll(verticalState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = server, style=TextStyle(fontWeight = FontWeight.Bold))
            keys.forEach {
                val typeOfKey = remember { mutableStateOf("") }
                Button(
                    onClick = {
                        val arr = arrayOf(typeOfKey.value, it)
                        mainType.value = arr
                    },
                    modifier = Modifier
                        .padding(3.dp)
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = buttonColor(syncCommands, it, typeOfKey),
                        contentColor = Color.Black
                    )
                ) {
                    Text(text = it)
                }
            }
        }
        VerticalScrollbar(
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 6.dp, top = 5.dp, bottom = 5.dp)
                .width(8.dp)
                .align(Alignment.CenterEnd),
            adapter = rememberScrollbarAdapter(verticalState)
        )
    }
}

fun buttonColor(
    syncCommands: MutableState<RedisCommands<String, String>>,
    it: String,
    typeOfKey: MutableState<String>
): Color {
    return when (syncCommands.value.type(it)) {
        "string" -> {
            typeOfKey.value = "string"; Color(204, 255, 255)
        }
        "hash" -> {
            typeOfKey.value = "hash"; Color(255, 204, 153)
        }
        else -> {
            typeOfKey.value = "null"; Color(0, 0, 0)
        }
    }
}