package com.stepler.steplerrsl.feature.overlay.presentation.overlay

import com.stepler.steplerrsl.common.domain.model.DetectedScreen
import com.stepler.steplerrsl.common.domain.model.DetectedWord
import com.stepler.steplerrsl.common.domain.model.Screenshot
import com.stepler.steplerrsl.common.domain.model.WordBounds
import com.stepler.steplerrsl.common.domain.screendetection.ScreenDetector
import com.stepler.steplerrsl.feature.overlay.domain.repository.OverlayLogRepository
import com.stepler.steplerrsl.feature.overlay.domain.usecase.LogMessageUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.orbitmvi.orbit.test.test

@OptIn(ExperimentalCoroutinesApi::class)
class OverlayViewModelTest {

    private val logcat = mutableListOf<String>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(detector: FakeScreenDetector) = OverlayViewModel(
        logMessage = LogMessageUseCase(object : OverlayLogRepository {
            override fun log(message: String) {
                logcat += message
            }
        }),
        screenDetector = detector,
    )

    @Test
    fun `screenshot click marks busy, logs and asks the service to capture`() = runTest {
        viewModel(FakeScreenDetector()).test(this) {
            containerHost.onScreenshotClick()
            expectState { copy(isBusy = true) }
            assertEquals("Taking screenshot...", awaitState().logs.single().message)
            expectSideEffect(OverlaySideEffect.CaptureScreen)
        }
    }

    @Test
    fun `captured screenshot is kept, words detected and no screen matches`() = runTest {
        val detector = FakeScreenDetector(
            words = listOf(
                DetectedWord("Play", WordBounds(10, 20, 110, 60)),
                DetectedWord("Battle", WordBounds(10, 80, 130, 120)),
            )
        )
        val screenshot = Screenshot(2, 1, IntArray(2))

        viewModel(detector).test(this, initialState = OverlayState(isBusy = true)) {
            containerHost.onScreenshotCaptured(screenshot)
            skipItems(5) // one state per log entry
            val finalState = awaitState()

            assertFalse(finalState.isBusy)
            assertEquals(
                listOf(
                    "Screenshot taken (2x1), kept in memory",
                    "Detecting words...",
                    "Found 2 English words:\nPlay\nBattle",
                    "Detecting screen...",
                    "No screen matches",
                ),
                finalState.logs.map { it.message },
            )
        }
        assertSame(screenshot, detector.kept)
        assertEquals("No screen matches", logcat.last())
    }

    @Test
    fun `matched screen name is logged`() = runTest {
        val detector = FakeScreenDetector(screen = DetectedScreen("Main", matchedWords = 6, totalWords = 7))

        viewModel(detector).test(this, initialState = OverlayState(isBusy = true)) {
            containerHost.onScreenshotCaptured(Screenshot(1, 1, IntArray(1)))
            skipItems(4)
            assertEquals("Screen matches: Main (6/7 words)", awaitState().logs.last().message)
            assertFalse(awaitState().isBusy)
        }
    }

    @Test
    fun `capture failure is logged and clears busy`() = runTest {
        viewModel(FakeScreenDetector()).test(this, initialState = OverlayState(isBusy = true)) {
            containerHost.onCaptureFailed("screen capture permission denied")
            assertEquals(
                "Screenshot failed: screen capture permission denied",
                awaitState().logs.single().message,
            )
            assertFalse(awaitState().isBusy)
        }
    }

    private class FakeScreenDetector(
        private val words: List<DetectedWord> = emptyList(),
        private val screen: DetectedScreen? = null,
    ) : ScreenDetector {
        var kept: Screenshot? = null

        override fun keepScreenshot(screenshot: Screenshot) {
            kept = screenshot
        }

        override suspend fun detectWords(screenshot: Screenshot) = Result.success(words)

        override fun detectScreen(words: List<DetectedWord>) = screen
    }
}
