package com.stepler.steplerrsl.feature.overlay.presentation.overlay

sealed interface OverlaySideEffect {
    /** The service should capture the screen and report back via onScreenshotCaptured / onCaptureFailed. */
    data object CaptureScreen : OverlaySideEffect
}
