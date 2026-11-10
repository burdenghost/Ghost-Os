package com.ghostos.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghostos.mobile.core.terminal.CommandRouter
import com.ghostos.mobile.core.terminal.TerminalLine

private val Bg = Color(0xFF080B0D)
private val Panel = Color(0xFF11171A)
private val Green = Color(0xFF77E6A5)
private val Muted = Color(0xFF8B9A92)

@Composable
fun GhostHomeScreen() {
    var input by remember { mutableStateOf("") }
    var lines by remember { mutableStateOf(listOf(
        TerminalLine("GHOST OS // MOBILE WORKSTATION", TerminalLine.Kind.SYSTEM),
        TerminalLine("Build 0.1.0-alpha · Android 7+ target", TerminalLine.Kind.SYSTEM),
        TerminalLine("Bootstrap shell ready. Type 'help' to begin.", TerminalLine.Kind.SYSTEM)
    )) }
    val router = remember { CommandRouter() }

    fun runCommand() {
        val cmd = input.trim()
        if (cmd.isNotEmpty()) {
            val result = router.execute(cmd)
            lines = if (result.lines.any { it.text == "\u0000CLEAR\u0000" }) emptyList()
                else lines + TerminalLine("$ " + cmd, TerminalLine.Kind.COMMAND) + result.lines
        }
        input = ""
    }

    Column(Modifier.fillMaxSize().background(Bg).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("GHOST OS", color = Green, fontSize = 23.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Text("MOBILE WORKSTATION", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
            }
            Surface(color = Color(0xFF173426), shape = RoundedCornerShape(50)) {
                Text("ALPHA", color = Green, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusTile("LINUX", "NOT INSTALLED")
            StatusTile("AI", "PLANNED")
            StatusTile("SECURITY", "LOCKED")
        }
        Spacer(Modifier.height(14.dp))
        Text("TERMINAL / SESSION 01", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(8.dp))
        Surface(Modifier.weight(1f).fillMaxWidth(), color = Panel, shape = RoundedCornerShape(14.dp)) {
            LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                items(lines) { line ->
                    val color = when (line.kind) {
                        TerminalLine.Kind.COMMAND -> Green
                        TerminalLine.Kind.ERROR -> Color(0xFFFF8C82)
                        TerminalLine.Kind.SYSTEM -> Muted
                        TerminalLine.Kind.OUTPUT -> Color(0xFFE1EAE4)
                    }
                    Text(line.text, color = color, fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 13.dp)) {
            Text("$", color = Green, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(9.dp))
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { runCommand() })
            )
            TextButton(onClick = { runCommand() }) { Text("RUN", color = Green, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(10.dp))
        Text("SAFE BOOTSTRAP MODE · NO ROOT REQUIRED · LINUX RUNTIME NOT CONNECTED", color = Muted, fontSize = 9.sp, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun RowScope.StatusTile(title: String, value: String) {
    Surface(Modifier.weight(1f), color = Panel, shape = RoundedCornerShape(10.dp)) {
        Column(Modifier.padding(10.dp)) {
            Text(title, color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
