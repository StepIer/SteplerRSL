package com.stepler.steplerrsl.feature.overlay.presentation.start

data class StartOverlayState(
    val isPermissionGranted: Boolean = false,
    /** Auto-tap works only while the app's Accessibility Service is enabled. */
    val isAutoTapEnabled: Boolean = false,
)
