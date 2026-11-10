package com.ghostos.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WorkspaceBg = Color(0xFF080B0D)
private val WorkspacePanel = Color(0xFF11171A)
private val WorkspaceGreen = Color(0xFF77E6A5)
private val WorkspaceMuted = Color(0xFF8B9A92)

private enum class Workspace(val title: String, val symbol: String, val subtitle: String) {
    HOME("Overview", "⌂", "System overview"),
    TERMINAL("Terminal", ">_", "Debian terminal"),
    SECURITY("Security", "◇", "Security tools"),
    NETWORK("Network", "⌁", "Network diagnostics"),
    OSINT("OSINT", "◎", "Investigation workspace"),
    FILES("Files", "▤", "Debian filesystem"),
    PACKAGES("Packages", "▦", "APT package management"),
    LOGS("Logs", "≡", "Runtime diagnostics")
}

/**
 * Responsive application shell. Compact screens use a modal side drawer;
 * tablets and landscape layouts use a persistent navigation rail.
 */
@Composable
fun GhostWorkspaceShell() {
    var selected by remember { mutableStateOf(Workspace.HOME) }
    var drawerOpen by remember { mutableStateOf(false) }

    BoxWithConstraints(Modifier.fillMaxSize().background(WorkspaceBg)) {
        val wide = maxWidth >= 680.dp

        if (wide) {
            Row(Modifier.fillMaxSize()) {
                NavigationPanel(selected = selected, onSelect = { selected = it }, compact = false)
                VerticalDivider(color = Color(0xFF26332D))
                WorkspaceContent(selected, Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            ModalNavigationDrawer(
                drawerState = rememberDrawerState(if (drawerOpen) DrawerValue.Open else DrawerValue.Closed),
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = WorkspacePanel,
                        drawerContentColor = Color.White,
                        modifier = Modifier.fillMaxHeight().widthIn(min = 250.dp, max = 310.dp)
                    ) {
                        NavigationPanel(
                            selected = selected,
                            onSelect = { selected = it; drawerOpen = false },
                            compact = true
                        )
                    }
                },
                gesturesEnabled = true
            ) {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        Modifier.fillMaxWidth().background(WorkspacePanel).padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { drawerOpen = true }) {
                            Text("☰", color = WorkspaceGreen, fontSize = 23.sp)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("GHOST OS", color = WorkspaceGreen, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                            Text(selected.title.uppercase(), color = WorkspaceMuted, fontSize = 10.sp)
                        }
                        Text("ALPHA", color = WorkspaceGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    WorkspaceContent(selected, Modifier.weight(1f).fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun NavigationPanel(
    selected: Workspace,
    onSelect: (Workspace) -> Unit,
    compact: Boolean
) {
    Column(
        Modifier
            .width(if (compact) 280.dp else 208.dp)
            .fillMaxHeight()
            .background(WorkspacePanel)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 18.dp)
    ) {
        Text("GHOST OS", color = WorkspaceGreen, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Text("MOBILE WORKSTATION", color = WorkspaceMuted, fontSize = 9.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(24.dp))
        Workspace.entries.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.title, maxLines = 1) },
                selected = selected == item,
                onClick = { onSelect(item) },
                icon = { Text(item.symbol, color = if (selected == item) WorkspaceGreen else WorkspaceMuted) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = Color(0xFF173426),
                    selectedTextColor = WorkspaceGreen,
                    unselectedTextColor = Color(0xFFE1EAE4),
                    selectedIconColor = WorkspaceGreen,
                    unselectedIconColor = WorkspaceMuted
                ),
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text("ROOTLESS · ANDROID", color = WorkspaceMuted, fontSize = 9.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun WorkspaceContent(selected: Workspace, modifier: Modifier = Modifier) {
    when (selected) {
        Workspace.HOME, Workspace.TERMINAL -> GhostHomeScreen()
        else -> WorkspaceModuleScreen(selected, modifier)
    }
}

@Composable
private fun WorkspaceModuleScreen(workspace: Workspace, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(WorkspaceBg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(workspace.title.uppercase(), color = WorkspaceGreen, fontSize = 23.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Text(workspace.subtitle, color = WorkspaceMuted, fontSize = 13.sp)
        Surface(color = WorkspacePanel, shape = MaterialTheme.shapes.large) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("WORKSPACE READY FOR MODULE INTEGRATION", color = WorkspaceGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(
                    when (workspace) {
                        Workspace.SECURITY -> "Security tools will run as isolated jobs with their own output and cancellation controls. No tools are launched from this screen yet."
                        Workspace.NETWORK -> "Network diagnostics will have a dedicated results panel and independent execution state."
                        Workspace.OSINT -> "OSINT modules will be added as independent tools with separate workspaces and saved results."
                        Workspace.FILES -> "The Debian filesystem browser will be connected to the app-private Linux root."
                        Workspace.PACKAGES -> "APT package operations will run as managed jobs with progress and logs."
                        Workspace.LOGS -> "Runtime and installation diagnostics are available here when connected to the diagnostics store."
                        else -> "Select a workspace from the navigation."
                    },
                    color = Color(0xFFE1EAE4),
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
                HorizontalDivider(color = Color(0xFF26332D))
                Text("This module is a navigation foundation, not an installed tool or active terminal session.", color = WorkspaceMuted, fontSize = 11.sp)
            }
        }
    }
}
