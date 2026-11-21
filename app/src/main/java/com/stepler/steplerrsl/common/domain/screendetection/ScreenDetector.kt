package com.stepler.steplerrsl.common.domain.screendetection

import com.stepler.steplerrsl.common.domain.model.DetectedScreen
import com.stepler.steplerrsl.common.domain.model.DetectedWord
import com.stepler.steplerrsl.common.domain.model.Screenshot

/**
 * Contract of the screen detection feature, shared so other features (e.g. overlay) can use it
 * without importing `feature.screendetection`. Implemented and bound by that feature.
 *
 * The steps are separate so callers can report progress between them.
 */
interface ScreenDetector {
    /** Keeps [screenshot] in memory as the latest capture. */
    fun keepScreenshot(screenshot: Screenshot)

    /** Finds English words on [screenshot] with their positions. */
    suspend fun detectWords(screenshot: Screenshot): Result<List<DetectedWord>>

    /** Returns the known screen matching [words], or null when no screen matches. */
    fun detectScreen(words: List<DetectedWord>): DetectedScreen?
}
