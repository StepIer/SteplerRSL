package com.stepler.steplerrsl.feature.overlay.presentation.start

sealed interface StartOverlaySideEffect {
    data object StartOverlay : StartOverlaySideEffect
    data object OpenPermissionSettings : StartOverlaySideEffect
}
