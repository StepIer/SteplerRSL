package com.stepler.steplerrsl.feature.overlay.presentation.overlay

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.stepler.steplerrsl.R
import com.stepler.steplerrsl.core.ui.theme.SteplerRSLTheme
import com.stepler.steplerrsl.feature.overlay.domain.usecase.LogOverlayButtonClickUseCase
import com.stepler.steplerrsl.feature.overlay.domain.usecase.SaveScreenshotUseCase
import com.stepler.steplerrsl.feature.overlay.presentation.screenshot.ScreenCaptureConsentActivity
import com.stepler.steplerrsl.feature.overlay.presentation.screenshot.ScreenCapturer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Foreground service that draws a Compose overlay on top of other apps.
 * Requires the SYSTEM_ALERT_WINDOW ("Display over other apps") permission.
 *
 * Screenshots use MediaProjection: the first tap asks for consent via
 * [ScreenCaptureConsentActivity]; the projection is then kept while the overlay lives.
 */
@AndroidEntryPoint
class OverlayService : LifecycleService(), SavedStateRegistryOwner {

    @Inject
    lateinit var logOverlayButtonClick: LogOverlayButtonClickUseCase

    @Inject
    lateinit var saveScreenshot: SaveScreenshotUseCase

    private val savedStateController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    private var overlayView: ComposeView? = null
    private var screenCapturer: ScreenCapturer? = null
    private var isCapturing = false

    override fun onCreate() {
        super.onCreate()
        savedStateController.performAttach()
        savedStateController.performRestore(null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_STOP -> stopSelf()
            ACTION_CAPTURE_CONSENT -> onCaptureConsent(intent)
            else -> {
                startInForeground(withMediaProjection = screenCapturer != null)
                showOverlay()
            }
        }
        return START_NOT_STICKY
    }

    /** App was swiped away from recents: the overlay must go with it. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        screenCapturer?.release()
        screenCapturer = null
        overlayView?.let(windowManager::removeView)
        overlayView = null
        super.onDestroy()
    }

    private fun showOverlay() {
        if (overlayView != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }

        val view = ComposeView(this)
        view.setViewTreeLifecycleOwner(this)
        view.setViewTreeSavedStateRegistryOwner(this)
        view.setContent {
            SteplerRSLTheme {
                OverlayContent(
                    onRawrClick = { logOverlayButtonClick() },
                    onScreenshotClick = ::onScreenshotClick,
                    onDrag = { delta ->
                        params.x += delta.x.roundToInt()
                        params.y += delta.y.roundToInt()
                        windowManager.updateViewLayout(view, params)
                    },
                )
            }
        }

        windowManager.addView(view, params)
        overlayView = view
    }

    private fun onScreenshotClick() {
        if (screenCapturer == null) {
            startActivity(
                Intent(this, ScreenCaptureConsentActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } else {
            takeScreenshot()
        }
    }

    private fun onCaptureConsent(intent: Intent) {
        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
        val data = IntentCompat.getParcelableExtra(intent, EXTRA_RESULT_DATA, Intent::class.java)
        if (resultCode != Activity.RESULT_OK || data == null) {
            toast(R.string.screenshot_permission_denied)
            return
        }

        // Android 14+: the service must be a mediaProjection FGS before getMediaProjection().
        startInForeground(withMediaProjection = true)
        val projection = getSystemService(MediaProjectionManager::class.java)
            .getMediaProjection(resultCode, data)
        if (projection == null) {
            startInForeground(withMediaProjection = false)
            toast(R.string.screenshot_failed)
            return
        }
        screenCapturer = runCatching {
            ScreenCapturer(this, projection) {
                // User stopped sharing (e.g. from the status bar): next tap asks for consent again.
                screenCapturer = null
                startInForeground(withMediaProjection = false)
            }
        }.getOrElse {
            projection.stop()
            startInForeground(withMediaProjection = false)
            toast(R.string.screenshot_failed)
            return
        }
        takeScreenshot()
    }

    private fun takeScreenshot() {
        val capturer = screenCapturer ?: return
        if (isCapturing) return
        isCapturing = true

        lifecycleScope.launch {
            // Hide the overlay so it isn't part of the screenshot, and let dialogs animate away.
            overlayView?.visibility = View.INVISIBLE
            delay(HIDE_BEFORE_CAPTURE_MS)
            val bitmap = runCatching { capturer.capture() }
            overlayView?.visibility = View.VISIBLE

            val saved = bitmap.mapCatching { saveScreenshot(it.toPng()).getOrThrow() }
            toast(if (saved.isSuccess) R.string.screenshot_saved else R.string.screenshot_failed)
            isCapturing = false
        }
    }

    private suspend fun Bitmap.toPng(): ByteArray = withContext(Dispatchers.Default) {
        ByteArrayOutputStream().use { out ->
            compress(Bitmap.CompressFormat.PNG, 100, out)
            recycle()
            out.toByteArray()
        }
    }

    private fun toast(@StringRes message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @Suppress("DEPRECATION")
    private fun overlayWindowType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

    private fun startInForeground(withMediaProjection: Boolean) {
        createNotificationChannel()

        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, OverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.overlay_notification_title))
            .setOngoing(true)
            .addAction(0, getString(R.string.overlay_stop), stopIntent)
            .build()

        val mediaProjectionType =
            if (withMediaProjection) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION else 0
        val type = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE or mediaProjectionType
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && withMediaProjection -> mediaProjectionType
            else -> null
        }
        if (type != null) {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.overlay_notification_channel),
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "overlay"
        private const val NOTIFICATION_ID = 1
        private const val ACTION_STOP = "com.stepler.steplerrsl.overlay.STOP"
        private const val ACTION_CAPTURE_CONSENT = "com.stepler.steplerrsl.overlay.CAPTURE_CONSENT"
        private const val EXTRA_RESULT_CODE = "result_code"
        private const val EXTRA_RESULT_DATA = "result_data"
        private const val HIDE_BEFORE_CAPTURE_MS = 300L

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, OverlayService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, OverlayService::class.java))
        }

        fun deliverCaptureConsent(context: Context, resultCode: Int, data: Intent?) {
            context.startService(
                Intent(context, OverlayService::class.java)
                    .setAction(ACTION_CAPTURE_CONSENT)
                    .putExtra(EXTRA_RESULT_CODE, resultCode)
                    .putExtra(EXTRA_RESULT_DATA, data)
            )
        }
    }
}
