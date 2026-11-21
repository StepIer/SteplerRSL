package com.stepler.steplerrsl.feature.overlay.presentation.overlay

import com.stepler.steplerrsl.common.domain.model.Screenshot
import com.stepler.steplerrsl.common.domain.screendetection.ScreenDetector
import com.stepler.steplerrsl.feature.overlay.domain.usecase.LogMessageUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.Syntax
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Orbit "ViewModel" for the overlay window. Services can't host androidx ViewModels, so this
 * owns its own scope: [OverlayService] creates it via Hilt and calls [clear] in onDestroy.
 *
 * Pipeline on Screenshot: capture (done by the service) → keep in memory → detect English words
 * → detect screen. The detection steps come from the screen detection feature via the shared
 * [ScreenDetector] contract. Every step is written to the on-screen log and to logcat (tag `rawr`).
 */
class OverlayViewModel @Inject constructor(
    private val logMessage: LogMessageUseCase,
    private val screenDetector: ScreenDetector,
) : ContainerHost<OverlayState, OverlaySideEffect> {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
    private var nextLogId = 0L

    override val container = scope.container<OverlayState, OverlaySideEffect>(OverlayState())

    fun onRawrClick() = intent {
        logMessage("Overlay button clicked")
    }

    fun onScreenshotClick() = intent {
        if (state.isBusy) return@intent
        reduce { state.copy(isBusy = true) }
        addLog("Taking screenshot...")
        postSideEffect(OverlaySideEffect.CaptureScreen)
    }

    fun onScreenshotCaptured(screenshot: Screenshot) = intent {
        screenDetector.keepScreenshot(screenshot)
        addLog("Screenshot taken (${screenshot.width}x${screenshot.height}), kept in memory")

        addLog("Detecting words...")
        screenDetector.detectWords(screenshot)
            .onSuccess { words ->
                // One log entry: header, then the words as a column.
                addLog(
                    buildString {
                        append("Found ${words.size} English words:")
                        words.forEach { append('\n').append(it.text) }
                    }
                )

                addLog("Detecting screen...")
                val screen = screenDetector.detectScreen(words)
                addLog(
                    screen?.let { "Screen matches: ${it.name} (${it.matchedWords}/${it.totalWords} words)" }
                        ?: "No screen matches"
                )
            }
            .onFailure { addLog("Word detection failed: ${it.message}") }

        reduce { state.copy(isBusy = false) }
    }

    fun onCaptureFailed(reason: String) = intent {
        addLog("Screenshot failed: $reason")
        reduce { state.copy(isBusy = false) }
    }

    fun clear() = scope.cancel()

    private suspend fun Syntax<OverlayState, OverlaySideEffect>.addLog(message: String) {
        logMessage(message)
        val entry = LogEntry(id = nextLogId++, time = timeFormat.format(Date()), message = message)
        reduce { state.copy(logs = (state.logs + entry).takeLast(MAX_LOG_ENTRIES)) }
    }

    private companion object {
        const val MAX_LOG_ENTRIES = 300
    }
}
