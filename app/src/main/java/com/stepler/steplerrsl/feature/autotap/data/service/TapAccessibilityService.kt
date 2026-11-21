package com.stepler.steplerrsl.feature.autotap.data.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Accessibility Service used only to dispatch tap gestures on the screen.
 * The user enables it once in system Accessibility settings; while connected it registers
 * itself in [TapServiceHolder] so [com.stepler.steplerrsl.feature.autotap.data.ScreenTapperImpl] can use it.
 */
@AndroidEntryPoint
class TapAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var holder: TapServiceHolder

    override fun onServiceConnected() {
        super.onServiceConnected()
        holder.service = this
    }

    override fun onDestroy() {
        if (holder.service === this) holder.service = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    /** Taps at ([x], [y]); returns true when the system completed the gesture. */
    suspend fun tap(x: Float, y: Float): Boolean = suspendCancellableCoroutine { cont ->
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, TAP_DURATION_MS))
            .build()
        val callback = object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                if (cont.isActive) cont.resume(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                if (cont.isActive) cont.resume(false)
            }
        }
        if (!dispatchGesture(gesture, callback, null) && cont.isActive) cont.resume(false)
    }

    private companion object {
        const val TAP_DURATION_MS = 50L
    }
}
