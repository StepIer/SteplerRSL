package com.stepler.steplerrsl.feature.overlay.presentation.overlay

import com.stepler.steplerrsl.common.domain.autotap.ScreenTapper
import com.stepler.steplerrsl.common.domain.model.Screenshot
import com.stepler.steplerrsl.common.domain.screendetection.ScreenDetector
import com.stepler.steplerrsl.feature.overlay.domain.model.ScreenAction
import com.stepler.steplerrsl.feature.overlay.domain.usecase.GetScreenActionUseCase
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
 * → detect screen → automatic action (e.g. Main → tap Battle). Detection and tapping come from
 * other features via the shared [ScreenDetector] and [ScreenTapper] contracts. Every step is
 * written to the on-screen log and to logcat (tag `rawr`).
 */
class OverlayViewModel @Inject constructor(
    private val logMessage: LogMessageUseCase,
    private val screenDetector: ScreenDetector,
    private val getScreenAction: GetScreenActionUseCase,
    private val screenTapper: ScreenTapper,
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
        val words = screenDetector.detectWords(screenshot).getOrElse {
            addLog("Word detection failed: ${it.message}")
            reduce { state.copy(isBusy = false) }
            return@intent
        }
        // One log entry: header, then the words as a column.
        addLog(
            buildString {
                append("Found ${words.size} English words:")
                words.forEach { append('\n').append(it.text) }
            }
        )

        addLog("Detecting screen...")
        val screen = screenDetector.detectScreen(words)
        if (screen == null) {
            addLog("No screen matches")
            reduce { state.copy(isBusy = false) }
            return@intent
        }
        addLog("Screen matches: ${screen.name} (${screen.matchedWords}/${screen.totalWords} words)")

        when (val action = getScreenAction(screen, words)) {
            is ScreenAction.TapWord -> {
                if (!screenTapper.isEnabled()) {
                    addLog("Can't tap ${action.word}: Accessibility Service is off. Enable SteplerRSL in Settings > Accessibility")
                    reduce { state.copy(isBusy = false) }
                } else {
                    addLog("Tapping ${action.word} at (${action.x}, ${action.y})...")
                    // Stay busy: the service makes the overlay pass touches through, then calls performTap.
                    postSideEffect(OverlaySideEffect.Tap(action.x, action.y))
                }
            }
            is ScreenAction.WordNotFound -> {
                addLog("Can't tap ${action.word}: word not found on screen")
                reduce { state.copy(isBusy = false) }
            }
            null -> reduce { state.copy(isBusy = false) }
        }
    }

    fun onCaptureFailed(reason: String) = intent {
        addLog("Screenshot failed: $reason")
        reduce { state.copy(isBusy = false) }
    }

    /** Called by the service once the overlay lets touches through. */
    fun performTap(x: Int, y: Int) = intent {
        screenTapper.tap(x, y)
            .onSuccess { addLog("Tapped at ($x, $y)") }
            .onFailure { addLog("Tap failed: ${it.message}") }
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
