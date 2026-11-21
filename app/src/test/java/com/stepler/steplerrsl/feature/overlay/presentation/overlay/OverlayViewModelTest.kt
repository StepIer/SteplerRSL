package com.stepler.steplerrsl.feature.overlay.presentation.overlay

import com.stepler.steplerrsl.common.domain.autotap.ScreenTapper
import com.stepler.steplerrsl.common.domain.model.DetectedScreen
import com.stepler.steplerrsl.common.domain.model.DetectedWord
import com.stepler.steplerrsl.common.domain.model.Screenshot
import com.stepler.steplerrsl.common.domain.model.WordBounds
import com.stepler.steplerrsl.common.domain.screendetection.ScreenDetector
import com.stepler.steplerrsl.feature.overlay.domain.repository.OverlayLogRepository
import com.stepler.steplerrsl.feature.overlay.domain.usecase.GetScreenActionUseCase
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
import org.junit.Assert.assertTrue
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

    private val main = DetectedScreen("Main", matchedWords = 6, totalWords = 8)
    private val battle = DetectedWord("Battle", WordBounds(2000, 900, 2200, 960))

    private fun viewModel(
        detector: FakeScreenDetector,
        tapper: FakeScreenTapper = FakeScreenTapper(),
    ) = OverlayViewModel(
        logMessage = LogMessageUseCase(object : OverlayLogRepository {
            override fun log(message: String) {
                logcat += message
            }
        }),
        screenDetector = detector,
        getScreenAction = GetScreenActionUseCase(),
        screenTapper = tapper,
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
    fun `main screen with Battle asks the service to tap its center and stays busy`() = runTest {
        val detector = FakeScreenDetector(words = listOf(battle), screen = main)

        viewModel(detector).test(this, initialState = OverlayState(isBusy = true)) {
            containerHost.onScreenshotCaptured(Screenshot(1, 1, IntArray(1)))
            skipItems(4)
            assertEquals("Screen matches: Main (6/8 words)", awaitState().logs.last().message)
            val tapping = awaitState()
            assertEquals("Tapping Battle at (2100, 930)...", tapping.logs.last().message)
            assertTrue(tapping.isBusy)
            expectSideEffect(OverlaySideEffect.Tap(2100, 930))
        }
    }

    @Test
    fun `main screen without Battle word logs it can't tap`() = runTest {
        viewModel(FakeScreenDetector(screen = main)).test(this, initialState = OverlayState(isBusy = true)) {
            containerHost.onScreenshotCaptured(Screenshot(1, 1, IntArray(1)))
            skipItems(5)
            assertEquals("Can't tap Battle: word not found on screen", awaitState().logs.last().message)
            assertFalse(awaitState().isBusy)
        }
    }

    @Test
    fun `tap is skipped with a hint when the accessibility service is off`() = runTest {
        val detector = FakeScreenDetector(words = listOf(battle), screen = main)

        viewModel(detector, FakeScreenTapper(enabled = false))
            .test(this, initialState = OverlayState(isBusy = true)) {
                containerHost.onScreenshotCaptured(Screenshot(1, 1, IntArray(1)))
                skipItems(5)
                assertEquals(
                    "Can't tap Battle: Accessibility Service is off. Enable SteplerRSL in Settings > Accessibility",
                    awaitState().logs.last().message,
                )
                assertFalse(awaitState().isBusy)
            }
    }

    @Test
    fun `performTap taps, logs the result and clears busy`() = runTest {
        val tapper = FakeScreenTapper()

        viewModel(FakeScreenDetector(), tapper).test(this, initialState = OverlayState(isBusy = true)) {
            containerHost.performTap(2100, 930)
            assertEquals("Tapped at (2100, 930)", awaitState().logs.single().message)
            assertFalse(awaitState().isBusy)
        }
        assertEquals(listOf(2100 to 930), tapper.taps)
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

    private class FakeScreenTapper(private val enabled: Boolean = true) : ScreenTapper {
        val taps = mutableListOf<Pair<Int, Int>>()

        override fun isEnabled() = enabled

        override suspend fun tap(x: Int, y: Int): Result<Unit> {
            taps += x to y
            return Result.success(Unit)
        }
    }
}
