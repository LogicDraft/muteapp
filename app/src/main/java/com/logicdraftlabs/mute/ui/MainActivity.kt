package com.logicdraftlabs.mute.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.logicdraftlabs.mute.R
import com.logicdraftlabs.mute.core.MuteController
import com.logicdraftlabs.mute.ui.navigation.AppNavGraph
import com.logicdraftlabs.mute.ui.navigation.Screen
import com.logicdraftlabs.mute.ui.theme.MuteTheme
import com.logicdraftlabs.mute.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )

        handleIntent(intent)

        setContent {
            val themePref by viewModel.themePreference.collectAsState()
            val dynamicColors by viewModel.dynamicColorsEnabled.collectAsState()

            val navController = rememberNavController()
            val context = LocalContext.current

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { /* no-op */ }

            LaunchedEffect(Unit) {
                viewModel.load(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val granted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!granted) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            MuteTheme(
                themePref = themePref,
                dynamicColorsEnabled = dynamicColors
            ) {
                AppNavGraph(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.load(this)
    }

    /**
     * Complete Voice Assistant & Gemini Intent Handler.
     * Parses voice commands, search queries, deep links, and shortcuts intents.
     */
    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        var targetAction: String? = intent.getStringExtra("voice_action")
            ?: intent.getStringExtra("action")
            ?: intent.getStringExtra("command")

        val action = intent.action
        if (targetAction == null && action != null) {
            when (action) {
                "actions.intent.TURN_ON_SILENT_MODE",
                "custom.actions.intent.MUTE",
                "com.logicdraftlabs.mute.action.MUTE" -> targetAction = "mute"

                "actions.intent.TURN_OFF_SILENT_MODE",
                "custom.actions.intent.UNMUTE",
                "com.logicdraftlabs.mute.action.UNMUTE" -> targetAction = "unmute"

                "actions.intent.OPEN_APP_FEATURE",
                "custom.actions.intent.TOGGLE",
                "com.logicdraftlabs.mute.action.TOGGLE_MUTE" -> targetAction = "toggle"
            }
        }

        // Check search query extras sent by Google Assistant / Gemini / Bixby
        val query = intent.getStringExtra(android.app.SearchManager.QUERY)
            ?: intent.getStringExtra("query")
            ?: intent.getStringExtra(Intent.EXTRA_TEXT)
            ?: intent.getStringExtra("com.google.android.gms.actions.EXTRA_QUERY")
            ?: intent.getStringExtra("feature")
            ?: intent.dataString

        if (targetAction == null && query != null) {
            val lowerQuery = query.lowercase()
            when {
                lowerQuery.contains("unmute") ||
                lowerQuery.contains("turn off") ||
                lowerQuery.contains("stop muto") ||
                lowerQuery.contains("disable muto") ||
                lowerQuery.contains("sound on") ||
                lowerQuery.contains("restore sound") ||
                lowerQuery.endsWith("/unmute") ||
                lowerQuery.endsWith("off") -> {
                    targetAction = "unmute"
                }

                lowerQuery.contains("mute") ||
                lowerQuery.contains("turn on") ||
                lowerQuery.contains("silent") ||
                lowerQuery.contains("be quiet") ||
                lowerQuery.contains("quiet") ||
                lowerQuery.contains("silence") ||
                lowerQuery.endsWith("/mute") ||
                lowerQuery.endsWith("on") -> {
                    targetAction = "mute"
                }

                lowerQuery.contains("toggle") ||
                lowerQuery.contains("switch") ||
                lowerQuery.endsWith("/toggle") -> {
                    targetAction = "toggle"
                }
            }
        }

        // Check Uri data scheme & path
        val uriData = intent.data
        if (targetAction == null && uriData != null) {
            val path = uriData.path ?: uriData.host ?: ""
            when {
                path.contains("unmute") || path.contains("off") -> targetAction = "unmute"
                path.contains("mute") || path.contains("on") -> targetAction = "mute"
                path.contains("toggle") -> targetAction = "toggle"
            }
        }

        // Perform instant mute/unmute action and display feedback
        if (targetAction != null) {
            val isCurrentlyMuted = MuteController.isMuted(this)
            when (targetAction) {
                "mute", "on" -> {
                    if (!isCurrentlyMuted) {
                        MuteController.mute(this)
                    }
                    Toast.makeText(this, "Muted by Voice Assistant", Toast.LENGTH_SHORT).show()
                }
                "unmute", "off" -> {
                    if (isCurrentlyMuted) {
                        MuteController.unmute(this)
                    }
                    Toast.makeText(this, "Unmuted by Voice Assistant", Toast.LENGTH_SHORT).show()
                }
                "toggle" -> {
                    val newState = MuteController.toggle(this)
                    val label = if (newState) "Muted by Voice Assistant" else "Unmuted by Voice Assistant"
                    Toast.makeText(this, label, Toast.LENGTH_SHORT).show()
                }
            }
            viewModel.load(this)
        }
    }

    companion object {
        const val EXTRA_REQUEST_PERMISSION = "extra_request_permission"
    }
}
