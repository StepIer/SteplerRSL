package com.stepler.steplerrsl.feature.overlay.presentation.start

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.stepler.steplerrsl.feature.overlay.presentation.overlay.OverlayService
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun StartOverlayScreen(
    modifier: Modifier = Modifier,
    viewModel: StartOverlayViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        viewModel::onNotificationPermissionResult,
    )

    LifecycleResumeEffect(Unit) {
        viewModel.onResume()
        onPauseOrDispose { }
    }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            StartOverlaySideEffect.StartOverlay -> OverlayService.start(context)
            StartOverlaySideEffect.OpenPermissionSettings -> context.openOverlayPermissionSettings()
            StartOverlaySideEffect.OpenAccessibilitySettings ->
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            StartOverlaySideEffect.RequestNotificationPermission ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.onNotificationPermissionResult(true)
                }
        }
    }

    StartOverlayContent(
        state = state,
        onStartClick = viewModel::onStartClick,
        onEnableAutoTapClick = viewModel::onEnableAutoTapClick,
        modifier = modifier,
    )
}

private fun Context.openOverlayPermissionSettings() {
    startActivity(
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:$packageName".toUri())
    )
}
