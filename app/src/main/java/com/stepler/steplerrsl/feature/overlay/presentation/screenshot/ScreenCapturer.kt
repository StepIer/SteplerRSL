package com.stepler.steplerrsl.feature.overlay.presentation.screenshot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Display
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

/**
 * Captures the whole screen through an already-granted [MediaProjection].
 *
 * One virtual display is kept for the projection's lifetime (Android 14+ allows only one per
 * projection). It has no surface while idle, so nothing is rendered between captures.
 */
class ScreenCapturer(
    context: Context,
    private val projection: MediaProjection,
    onStopped: () -> Unit,
) {
    private val handler = Handler(Looper.getMainLooper())
    private val metrics = DisplayMetrics().also {
        @Suppress("DEPRECATION")
        context.getSystemService(DisplayManager::class.java)
            .getDisplay(Display.DEFAULT_DISPLAY)
            .getRealMetrics(it)
    }
    private val width = metrics.widthPixels
    private val height = metrics.heightPixels
    private val virtualDisplay: VirtualDisplay

    init {
        // Must be registered before createVirtualDisplay on Android 14+.
        projection.registerCallback(
            object : MediaProjection.Callback() {
                override fun onStop() {
                    release()
                    onStopped()
                }
            },
            handler,
        )
        virtualDisplay = projection.createVirtualDisplay(
            "screenshot",
            width,
            height,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            null,
            null,
            handler,
        ) ?: error("Cannot create virtual display for screen capture")
    }

    suspend fun capture(): Bitmap {
        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        try {
            virtualDisplay.surface = reader.surface
            return withTimeout(CAPTURE_TIMEOUT_MS) { awaitImage(reader) }.use { it.toBitmap() }
        } finally {
            virtualDisplay.surface = null
            reader.close()
        }
    }

    fun release() {
        virtualDisplay.release()
        projection.stop()
    }

    private suspend fun awaitImage(reader: ImageReader): Image = suspendCancellableCoroutine { cont ->
        reader.setOnImageAvailableListener({ r ->
            val image = r.acquireLatestImage() ?: return@setOnImageAvailableListener
            r.setOnImageAvailableListener(null, null)
            if (cont.isActive) cont.resume(image) else image.close()
        }, handler)
        cont.invokeOnCancellation { reader.setOnImageAvailableListener(null, null) }
    }

    private fun Image.toBitmap(): Bitmap {
        val plane = planes[0]
        val rowPaddingPx = (plane.rowStride - plane.pixelStride * width) / plane.pixelStride
        val padded = Bitmap.createBitmap(width + rowPaddingPx, height, Bitmap.Config.ARGB_8888)
        padded.copyPixelsFromBuffer(plane.buffer)
        if (rowPaddingPx == 0) return padded
        return Bitmap.createBitmap(padded, 0, 0, width, height).also { padded.recycle() }
    }

    private companion object {
        const val CAPTURE_TIMEOUT_MS = 2_000L
    }
}
