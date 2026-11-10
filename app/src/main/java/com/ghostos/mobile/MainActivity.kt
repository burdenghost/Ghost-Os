package com.ghostos.mobile

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import com.ghostos.mobile.ui.GhostWorkspaceShell

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFF77E6A5),
                background = Color(0xFF080B0D),
                surface = Color(0xFF11171A),
                onPrimary = Color(0xFF07110B),
                onBackground = Color(0xFFE6F0EA),
                onSurface = Color(0xFFE6F0EA)
            )) {
                GhostWorkspaceShell()
            }
        }
    }
}
