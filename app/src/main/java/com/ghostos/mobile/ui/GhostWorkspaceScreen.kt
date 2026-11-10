package com.ghostos.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ghostos.mobile.core.runtime.Distribution
import com.ghostos.mobile.core.runtime.GhostProotRuntime
import com.ghostos.mobile.core.runtime.RuntimeSession
import com.ghostos.mobile.core.runtime.RuntimeStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.connectbot.terminal.Terminal
import org.connectbot.terminal.TerminalEmulatorFactory

private val WorkspaceBg = Color(0xFF080B0D)
private val WorkspacePanel = Color(0xFF11171A)
private val WorkspaceGreen = Color(0xFF77E6A5)
private val WorkspaceMuted = Color(0xFF8B9A92)
private val WorkspaceText = Color(0xFFE1EAE4)

private enum class WorkspacePage(val label: String, val description: String) {
    HOME("Overview", "System status and workspace launcher"),
    TERMINAL("Terminal Manager", "Independent Debian PTY sessions"),
    SECURITY("Security Center", "A dedicated home for security utilities"),
    NETWORK("Network Tools", "Network diagnostics and connectivity tools"),
    OSINT("OSINT Center", "Investigation and information-gathering tools"),
    FILES("File Manager", "Browse the private Debian filesystem"),
    PACKAGES("Package Manager", "Manage Debian packages with APT"),
    AI("AI Assistant", "Local and optional cloud assistance"),
    SETTINGS("System Settings", "Runtime, permissions and appearance")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostWorkspaceScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val runtime = remember { GhostProotRuntime(context) }
    val sessions = remember { mutableStateListOf<RuntimeSession>() }
    var selectedSessionId by remember { mutableStateOf<String?>(null) }
    var page by remember { mutableStateOf(WorkspacePage.HOME) }
    var drawerOpen by remember { mutableStateOf(false) }
    var runtimeStatus by remember { mutableStateOf(runtime.status()) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Debian stable workspace") }

    fun startTerminal() {
        if (busy) return
        scope.launch {
            busy = true
            try {
                val session = runtime.startSession()
                sessions.add(session)
                selectedSessionId = session.id
                runtimeStatus = runtime.status()
                message = "Terminal session " + sessions.size + " started"
            } catch (error: Exception) {
                message = "Could not start terminal: " + (error.message ?: "unknown error")
            } finally {
                busy = false
            }
        }
    }

    fun closeTerminal(session: RuntimeSession) {
        session.close()
        sessions.removeAll { it.id == session.id }
        if (selectedSessionId == session.id) selectedSessionId = sessions.lastOrNull()?.id
        runtimeStatus = runtime.status()
        message = "Terminal session closed"
    }

    DisposableEffect(runtime) {
        onDispose { scope.launch(Dispatchers.IO) { runtime.stop() } }
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    LaunchedEffect(drawerOpen) {
        if (drawerOpen) drawerState.open() else drawerState.close()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxHeight().widthIn(max = 320.dp),
                drawerContainerColor = WorkspacePanel,
            ) {
                Text("GHOST OS", color = WorkspaceGreen, fontSize = 22.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                    letterSpacing = 2.sp, modifier = Modifier.padding(start = 22.dp, top = 28.dp, bottom = 4.dp))
                Text("MOBILE WORKSTATION", color = WorkspaceMuted, fontSize = 10.sp,
                    letterSpacing = 2.sp, modifier = Modifier.padding(start = 22.dp, bottom = 20.dp))
                HorizontalDivider(color = Color(0xFF26312C))
                WorkspacePage.values().forEach { item ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = when (item) {
                                    WorkspacePage.HOME -> Icons.Default.Home
                                    WorkspacePage.TERMINAL -> Icons.Default.Terminal
                                    WorkspacePage.SECURITY -> Icons.Default.Security
                                    WorkspacePage.NETWORK -> Icons.Default.Wifi
                                    WorkspacePage.OSINT -> Icons.Default.Search
                                    WorkspacePage.FILES -> Icons.Default.Folder
                                    WorkspacePage.PACKAGES -> Icons.Default.Inventory2
                                    WorkspacePage.AI -> Icons.Default.Psychology
                                    WorkspacePage.SETTINGS -> Icons.Default.Settings
                                },
                                contentDescription = null,
                            )
                        },
                        label = { Text(item.label, maxLines = 1) },
                        selected = page == item,
                        onClick = { page = item; drawerOpen = false },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFF173426),
                            selectedTextColor = WorkspaceGreen,
                            selectedIconColor = WorkspaceGreen,
                            unselectedTextColor = WorkspaceText,
                            unselectedIconColor = WorkspaceMuted,
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Text("ALPHA · ARM64 · ANDROID 9+", color = WorkspaceMuted, fontSize = 10.sp,
                    modifier = Modifier.padding(22.dp))
            }
        },
    ) {
        Scaffold(
            containerColor = WorkspaceBg,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(page.label, color = WorkspaceGreen,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 18.sp)
                            Text("GHOST OS / WORKSPACE", color = WorkspaceMuted, fontSize = 9.sp, letterSpacing = 1.4.sp)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { drawerOpen = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open navigation", tint = WorkspaceGreen)
                        }
                    },
                    actions = {
                        Surface(color = Color(0xFF173426), shape = RoundedCornerShape(50)) {
                            Text("ALPHA", color = WorkspaceGreen, fontSize = 10.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = WorkspaceBg),
                )
            },
        ) { insets ->
            Column(
                Modifier.fillMaxSize().padding(insets).imePadding().navigationBarsPadding()
                    .padding(horizontal = 14.dp)
            ) {
                when (page) {
                    WorkspacePage.HOME -> HomeWorkspace(
                        status = runtimeStatus, busy = busy, message = message, terminalCount = sessions.size,
                        onInstall = {
                            scope.launch {
                                busy = true
                                message = "Installing Debian stable…"
                                try {
                                    runtime.installDistribution(Distribution.DEBIAN_STABLE) { _, text -> message = text }
                                    runtimeStatus = runtime.status()
                                    message = "Debian stable installed"
                                } catch (error: Exception) {
                                    message = "Installation failed: " + (error.message ?: "unknown error")
                                } finally { busy = false }
                            }
                        },
                        onOpenTerminal = { page = WorkspacePage.TERMINAL; if (sessions.isEmpty()) startTerminal() },
                        onNavigate = { page = it },
                    )
                    WorkspacePage.TERMINAL -> {
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("SEPARATE PTY SESSIONS", color = WorkspaceMuted, fontSize = 10.sp, letterSpacing = 1.sp)
                            Button(
                                onClick = { startTerminal() },
                                enabled = runtimeStatus != RuntimeStatus.NOT_INSTALLED && !busy,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("NEW", fontSize = 11.sp)
                            }
                        }
                        if (sessions.isNotEmpty()) {
                            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                sessions.forEachIndexed { index, session ->
                                    FilterChip(
                                        selected = selectedSessionId == session.id,
                                        onClick = { selectedSessionId = session.id },
                                        label = { Text("Terminal " + (index + 1), maxLines = 1) },
                                        trailingIcon = {
                                            IconButton(onClick = { closeTerminal(session) }, modifier = Modifier.size(22.dp)) {
                                                Icon(Icons.Default.Close, contentDescription = "Close terminal",
                                                    modifier = Modifier.size(14.dp))
                                            }
                                        },
                                    )
                                }
                            }
                        }
                        if (sessions.isEmpty()) {
                            EmptyWorkspace(
                                title = "No terminal sessions",
                                detail = if (runtimeStatus == RuntimeStatus.NOT_INSTALLED)
                                    "Install Debian before opening a terminal."
                                else "Create a terminal to start an independent Debian shell.",
                                action = if (runtimeStatus == RuntimeStatus.NOT_INSTALLED) "INSTALL DEBIAN" else "NEW TERMINAL",
                                enabled = !busy,
                                onAction = {
                                    if (runtimeStatus == RuntimeStatus.NOT_INSTALLED) {
                                        scope.launch {
                                            busy = true
                                            try {
                                                runtime.installDistribution(Distribution.DEBIAN_STABLE) { _, text -> message = text }
                                                runtimeStatus = runtime.status()
                                                message = "Debian stable installed"
                                            } catch (error: Exception) { message = error.message ?: "Install failed" }
                                            finally { busy = false }
                                        }
                                    } else startTerminal()
                                },
                            )
                        } else {
                            Box(Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)) {
                                sessions.forEach { session ->
                                    TerminalPane(
                                        session = session,
                                        visible = selectedSessionId == session.id,
                                        modifier = Modifier.fillMaxSize().zIndex(if (selectedSessionId == session.id) 1f else 0f),
                                    )
                                }
                            }
                        }
                        Text(message, color = WorkspaceMuted, fontSize = 10.sp, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    else -> ToolWorkspacePage(
                        page = page,
                        onOpenTerminal = { page = WorkspacePage.TERMINAL; if (sessions.isEmpty()) startTerminal() },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeWorkspace(
    status: RuntimeStatus,
    busy: Boolean,
    message: String,
    terminalCount: Int,
    onInstall: () -> Unit,
    onOpenTerminal: () -> Unit,
    onNavigate: (WorkspacePage) -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 18.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
            StatusCard("LINUX", status.name, Modifier.weight(1f))
            StatusCard("TERMINALS", terminalCount.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Surface(color = WorkspacePanel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("LINUX WORKSPACE", color = WorkspaceGreen,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, letterSpacing = 1.sp)
                Text(message, color = WorkspaceMuted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onInstall,
                        enabled = !busy && status == RuntimeStatus.NOT_INSTALLED, modifier = Modifier.weight(1f)) {
                        Text("INSTALL DEBIAN", fontSize = 10.sp)
                    }
                    Button(onClick = onOpenTerminal,
                        enabled = !busy && status != RuntimeStatus.NOT_INSTALLED, modifier = Modifier.weight(1f)) {
                        Text("OPEN TERMINAL", fontSize = 10.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("TOOLS & WORKSPACES", color = WorkspaceMuted, fontSize = 11.sp, letterSpacing = 1.4.sp)
        Spacer(Modifier.height(8.dp))
        val tools = listOf(
            WorkspacePage.TERMINAL, WorkspacePage.SECURITY, WorkspacePage.NETWORK,
            WorkspacePage.OSINT, WorkspacePage.FILES, WorkspacePage.PACKAGES,
            WorkspacePage.AI, WorkspacePage.SETTINGS,
        )
        tools.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                pair.forEach { item ->
                    Surface(color = WorkspacePanel, shape = RoundedCornerShape(13.dp),
                        modifier = Modifier.weight(1f), onClick = { onNavigate(item) }) {
                        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(item.label, color = WorkspaceGreen,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 13.sp)
                            Text(item.description, color = WorkspaceMuted, fontSize = 10.sp, lineHeight = 14.sp)
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(9.dp))
        }
    }
}

@Composable
private fun StatusCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(color = WorkspacePanel, shape = RoundedCornerShape(13.dp), modifier = modifier) {
        Column(Modifier.padding(13.dp)) {
            Text(title, color = WorkspaceMuted, fontSize = 9.sp, letterSpacing = 1.sp)
            Text(value, color = WorkspaceGreen, fontSize = 13.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun ToolWorkspacePage(page: WorkspacePage, onOpenTerminal: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(page.description, color = WorkspaceMuted, fontSize = 13.sp)
        Surface(color = WorkspacePanel, shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(page.label.uppercase(), color = WorkspaceGreen,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, letterSpacing = 1.sp)
                Text(
                    when (page) {
                        WorkspacePage.SECURITY -> "Security tools will run as independent tasks with their own output and controls. No tool is launched automatically."
                        WorkspacePage.NETWORK -> "Network diagnostics will have dedicated screens for interface status, DNS, routes and connectivity checks."
                        WorkspacePage.OSINT -> "OSINT modules will be added as independent tools with clear scope, input fields and result views."
                        WorkspacePage.FILES -> "The file browser will be rooted in the app-private Debian filesystem. Android shared storage access will be explicit."
                        WorkspacePage.PACKAGES -> "APT operations will run as managed jobs with progress, cancellation and readable logs."
                        WorkspacePage.AI -> "The assistant workspace will support offline-capable local actions and optional cloud providers."
                        WorkspacePage.SETTINGS -> "Runtime status, storage, permissions, appearance and session controls will live here."
                        else -> page.description
                    },
                    color = WorkspaceText, fontSize = 12.sp, lineHeight = 18.sp,
                )
                OutlinedButton(onClick = onOpenTerminal) { Text("OPEN DEBIAN TERMINAL") }
                Text("MODULE STATUS: PLANNED", color = WorkspaceMuted, fontSize = 10.sp, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun EmptyWorkspace(
    title: String, detail: String, action: String, enabled: Boolean, onAction: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().heightIn(min = 220.dp).padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(title, color = WorkspaceGreen, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Text(detail, color = WorkspaceMuted, fontSize = 12.sp, lineHeight = 18.sp)
        Button(onClick = onAction, enabled = enabled, modifier = Modifier.padding(top = 12.dp)) { Text(action) }
    }
}

@Composable
private fun TerminalPane(session: RuntimeSession, visible: Boolean, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val emulator = remember(session.id) {
        TerminalEmulatorFactory.create(
            initialRows = 24,
            initialCols = 80,
            defaultForeground = WorkspaceText,
            defaultBackground = Color.Black,
            onKeyboardInput = { bytes -> scope.launch(Dispatchers.IO) { session.write(bytes) } },
            onResize = { dimensions ->
                scope.launch(Dispatchers.IO) { session.resize(dimensions.rows, dimensions.columns) }
            },
        )
    }

    LaunchedEffect(session.id, emulator) {
        val buffer = ByteArray(8192)
        while (true) {
            val count = withContext(Dispatchers.IO) { session.read(buffer) }
            if (count < 0) break
            if (count > 0) emulator.writeInput(buffer, 0, count)
        }
    }

    DisposableEffect(emulator) { onDispose { emulator.close() } }

    Surface(modifier = modifier, color = Color.Black, shape = RoundedCornerShape(14.dp)) {
        Terminal(
            terminalEmulator = emulator,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
            backgroundColor = Color.Black,
            foregroundColor = WorkspaceText,
            keyboardEnabled = visible,
            showSoftKeyboard = visible,
            initialFontSize = 12.sp,
        )
    }
}
