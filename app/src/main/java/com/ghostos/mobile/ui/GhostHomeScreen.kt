package com.ghostos.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.ghostos.mobile.R
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghostos.mobile.core.diagnostics.DiagnosticsStore
import com.ghostos.mobile.core.runtime.Distribution
import com.ghostos.mobile.core.runtime.GhostProotRuntime
import com.ghostos.mobile.core.runtime.RuntimeSession
import com.ghostos.mobile.core.runtime.RuntimeStatus
import com.ghostos.mobile.core.terminal.CommandRouter
import com.ghostos.mobile.core.terminal.TerminalLine
import com.ghostos.mobile.core.tools.SecurityTool
import com.ghostos.mobile.core.tools.SecurityToolCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.connectbot.terminal.Terminal
import org.connectbot.terminal.TerminalEmulatorFactory

private val Bg = Color(0xFF080B0D)
private val Panel = Color(0xFF11171A)
private val Green = Color(0xFF77E6A5)
private val Muted = Color(0xFF8B9A92)
private val Output = Color(0xFFE1EAE4)

/**
 * Ghost OS home screen.
 *
 * Bootstrap mode uses the small safe built-in router. Once Debian is started,
 * all keyboard input, ANSI/VT sequences, cursor movement, resizing and terminal
 * output are handled by the upstream ConnectBot terminal emulator rather than
 * treating a PTY as a plain text log.
 */
@Composable
fun GhostHomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val runtime = remember { GhostProotRuntime(context) }
    val router = remember { CommandRouter() }
    var input by remember { mutableStateOf("") }
    var runtimeStatus by remember { mutableStateOf(runtime.status()) }
    var session by remember { mutableStateOf<RuntimeSession?>(null) }
    var busy by remember { mutableStateOf(false) }
    var showLogs by remember { mutableStateOf(false) }
    var showTools by remember { mutableStateOf(false) }
    var logText by remember { mutableStateOf(DiagnosticsStore.read(context)) }
    var progress by remember { mutableStateOf("") }
    var lines by remember {
        mutableStateOf(
            listOf(
                TerminalLine("GHOST OS // MOBILE WORKSTATION", TerminalLine.Kind.SYSTEM),
                TerminalLine("Build 0.3.0-alpha · Android 9+ · ARM64", TerminalLine.Kind.SYSTEM),
                TerminalLine("Install Debian to activate the Linux guest terminal."),
            )
        )
    }

    val terminalEmulator = remember {
        TerminalEmulatorFactory.create(
            initialRows = 32,
            initialCols = 100,
            defaultForeground = Output,
            defaultBackground = Panel,
            onKeyboardInput = { bytes ->
                scope.launch(Dispatchers.IO) { session?.write(bytes) }
            },
            onResize = { dimensions ->
                scope.launch(Dispatchers.IO) {
                    session?.resize(dimensions.rows, dimensions.columns)
                }
            },
        )
    }

    fun append(text: String, kind: TerminalLine.Kind = TerminalLine.Kind.OUTPUT) {
        lines = lines + TerminalLine(text, kind)
    }

    fun installSecurityTool(tool: SecurityTool) {
        if (!tool.installable) return
        scope.launch {
            busy = true
            try {
                val active = session ?: runtime.startSession().also {
                    session = it
                    runtimeStatus = runtime.status()
                }
                val command = "apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends " + tool.packages.joinToString(" ")
                DiagnosticsStore.append(context, "TOOL install requested: ${tool.id}; command=$command")
                active.write(command + "\n")
                showTools = false
                append("Installing ${tool.name} inside rootless Debian…", TerminalLine.Kind.SYSTEM)
            } catch (error: Exception) {
                append("Could not start tool installation: ${error.message}", TerminalLine.Kind.ERROR)
                DiagnosticsStore.append(context, "TOOL install error ${tool.id}: ${error.stackTraceToString()}")
            } finally {
                busy = false
            }
        }
    }

    fun runBootstrapCommand() {
        val command = input
        if (command.isBlank()) return
        input = ""
        if (command.equals("logs", ignoreCase = true)) { showLogs = true; logText = DiagnosticsStore.read(context); return }
        if (command.equals("clearlogs", ignoreCase = true)) { DiagnosticsStore.clear(context); logText = ""; append("Diagnostics log cleared", TerminalLine.Kind.SYSTEM); return }
        if (command.equals("diagnostics", ignoreCase = true)) {
            val report = "Android API ${android.os.Build.VERSION.SDK_INT}; ABI ${android.os.Build.SUPPORTED_ABIS.joinToString()}; app storage available ${context.filesDir.usableSpace / (1024 * 1024)} MB; runtime ${runtime.status()}"
            DiagnosticsStore.append(context, report)
            append(report, TerminalLine.Kind.SYSTEM)
            return
        }
        val result = router.execute(command)
        lines = if (result.lines.any { it.text == "\u0000CLEAR\u0000" }) {
            emptyList()
        } else {
            lines + TerminalLine("$ $command", TerminalLine.Kind.COMMAND) + result.lines
        }
        DiagnosticsStore.append(context, "$ $command")
        result.lines.forEach { DiagnosticsStore.append(context, it.text) }
    }

    LaunchedEffect(session?.id) {
        val active = session ?: return@LaunchedEffect
        val buffer = ByteArray(8192)
        while (true) {
            val count = withContext(Dispatchers.IO) { active.read(buffer) }
            if (count < 0) break
            if (count > 0) terminalEmulator.writeInput(buffer, 0, count)
        }
        if (session?.id == active.id) {
            active.close()
            session = null
            runtimeStatus = runtime.status()
            append("Linux session ended", TerminalLine.Kind.SYSTEM)
        }
    }

    DisposableEffect(runtime, terminalEmulator) {
        onDispose {
            scope.launch(Dispatchers.IO) { runtime.stop() }
            terminalEmulator.close()
        }
    }

    Column(Modifier.fillMaxSize().background(Bg).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.ghost_logo),
                    contentDescription = "Ghost OS logo",
                    modifier = Modifier.size(42.dp),
                )
                Column {
                    Text("GHOST OS", color = Green, fontSize = 23.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text("MOBILE WORKSTATION", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
                }
            }
            Surface(color = Color(0xFF173426), shape = RoundedCornerShape(50)) {
                Text("ALPHA", color = Green, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusTile("LINUX", runtimeStatus.name)
            StatusTile("SECURITY", "LOCKED")
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    scope.launch {
                        busy = true
                        progress = "Starting Debian installation…"
                        try {
                            runtime.installDistribution(Distribution.DEBIAN_STABLE) { percent, message ->
                                progress = message
                                DiagnosticsStore.append(context, "INSTALL progress=$percent: $message")
                            }
                            runtimeStatus = runtime.status()
                            append("Debian stable is installed", TerminalLine.Kind.SYSTEM)
                            DiagnosticsStore.append(context, "INSTALL success; runtime status=$runtimeStatus")
                        } catch (error: Exception) {
                            append("Install failed: ${error.message}", TerminalLine.Kind.ERROR)
                            DiagnosticsStore.append(context, "INSTALL exception: ${error.stackTraceToString()}")
                            runtimeStatus = runtime.status()
                            DiagnosticsStore.append(context, "INSTALL ended; runtime status=$runtimeStatus")
                        } finally {
                            busy = false
                        }
                    }
                },
                enabled = !busy && session == null && runtimeStatus == RuntimeStatus.NOT_INSTALLED,
                modifier = Modifier.weight(1f),
            ) { Text(if (busy) "INSTALLING…" else "INSTALL DEBIAN") }

            Button(
                onClick = {
                    scope.launch {
                        busy = true
                        try {
                            terminalEmulator.clearScreen()
                            session = runtime.startSession()
                            runtimeStatus = runtime.status()
                            append("Connected to Debian PTY terminal", TerminalLine.Kind.SYSTEM)
                        } catch (error: Exception) {
                            append("Could not start Linux: ${error.message}", TerminalLine.Kind.ERROR)
                        } finally {
                            busy = false
                        }
                    }
                },
                enabled = !busy && session == null && runtimeStatus == RuntimeStatus.READY,
                modifier = Modifier.weight(1f),
            ) { Text("START LINUX") }
        }

        if (busy || progress.isNotBlank()) {
            Text(progress, color = Muted, fontSize = 10.sp, modifier = Modifier.padding(vertical = 4.dp))
        }

        if (session != null) {
            TextButton(
                onClick = {
                    scope.launch {
                        runtime.stop()
                        session = null
                        runtimeStatus = runtime.status()
                    }
                },
            ) {
                Text("STOP LINUX SESSION", color = Color(0xFFFF8C82))
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { showTools = !showTools; showLogs = false }) {
                Text(if (showTools) "CLOSE TOOLS" else "SECURITY TOOLS", color = Green, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = { showLogs = !showLogs; showTools = false; logText = DiagnosticsStore.read(context) }) {
                Text(if (showLogs) "CLOSE LOGS" else "VIEW LOGS", color = Green, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            if (showLogs) "GHOST OS / DIAGNOSTICS" else if (session == null) "BOOTSTRAP / SAFE COMMANDS" else "DEBIAN / INTERACTIVE TERMINAL",
            color = Muted,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
        )
        Spacer(Modifier.height(8.dp))

        if (showTools) {
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Text("TOOLS RUN INSIDE THE PRIVATE DEBIAN GUEST. ANDROID ROOT IS NOT USED.", color = Muted, fontSize = 10.sp, lineHeight = 15.sp) }
                items(SecurityToolCatalog.all) { tool ->
                    Surface(Modifier.fillMaxWidth(), color = Panel, shape = RoundedCornerShape(12.dp)) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tool.name, color = Output, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(if (tool.installable) "ARM64 / APT" else "REVIEW", color = if (tool.installable) Green else Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(tool.description, color = Muted, fontSize = 11.sp, lineHeight = 15.sp)
                            if (tool.installable) {
                                Button(onClick = { installSecurityTool(tool) }, enabled = !busy && runtimeStatus != RuntimeStatus.NOT_INSTALLED && runtimeStatus != RuntimeStatus.INSTALLING, modifier = Modifier.align(Alignment.End)) { Text("INSTALL IN DEBIAN") }
                            } else Text("Not enabled until ARM64 dependencies and permissions are verified.", color = Muted, fontSize = 10.sp)
                        }
                    }
                }
            }
        } else if (showLogs) {
            Surface(Modifier.weight(1f).fillMaxWidth(), color = Panel, shape = RoundedCornerShape(14.dp)) {
                LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    item {
                        Text("APP-PRIVATE LOG · ${logText.lineSequence().count { it.isNotBlank() }} entries", color = Muted, fontSize = 10.sp)
                    }
                    item {
                        TextButton(onClick = { DiagnosticsStore.clear(context); logText = "" }) { Text("CLEAR LOG", color = Color(0xFFFF8C82)) }
                    }
                    item {
                        Text(logText.ifBlank { "No diagnostic entries yet. Run diagnostics or retry installation." }, color = Output, fontFamily = FontFamily.Monospace, fontSize = 10.sp, lineHeight = 15.sp)
                    }
                }
            }
        } else if (session != null) {
            Terminal(
                terminalEmulator = terminalEmulator,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                backgroundColor = Panel,
                foregroundColor = Output,
                keyboardEnabled = true,
                showSoftKeyboard = true,
                initialFontSize = 12.sp,
            )
        } else if (!showLogs) {
            Surface(
                Modifier.weight(1f).fillMaxWidth(),
                color = Panel,
                shape = RoundedCornerShape(14.dp),
            ) {
                LazyColumn(
                    Modifier.fillMaxSize().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    items(lines) { line ->
                        val color = when (line.kind) {
                            TerminalLine.Kind.COMMAND -> Green
                            TerminalLine.Kind.ERROR -> Color(0xFFFF8C82)
                            TerminalLine.Kind.SYSTEM -> Muted
                            TerminalLine.Kind.OUTPUT -> Output
                        }
                        Text(
                            line.text,
                            color = color,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Panel, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 13.dp),
            ) {
                Text("$", color = Green, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(9.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Send,
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSend = { runBootstrapCommand() },
                    ),
                )
                TextButton(onClick = { runBootstrapCommand() }) {
                    Text("RUN", color = Green, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            if (session == null) "PRIVATE APP STORAGE · NO ROOT · DEBIAN RUNTIME"
            else "ROOTLESS DEBIAN · PTY ACTIVE · APP SANDBOXED",
            color = Muted,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp,
        )
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
