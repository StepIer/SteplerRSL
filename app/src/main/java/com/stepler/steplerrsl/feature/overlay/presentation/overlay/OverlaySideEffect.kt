package com.stepler.steplerrsl.feature.overlay.presentation.overlay

sealed interface OverlaySideEffect {
    /** The service should capture the screen and report back via onScreenshotCaptured / onCaptureFailed. */
    data object CaptureScreen : OverlaySideEffect

    /**
     * The service should make the overlay pass touches through, then call [OverlayViewModel.performTap]
     * and restore the overlay, so the tap reaches the app below even if the overlay covers the point.
     */
    data class Tap(val x: Int, val y: Int) : OverlaySideEffect
}
