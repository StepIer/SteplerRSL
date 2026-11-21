package com.stepler.steplerrsl.feature.screendetection.domain

import com.stepler.steplerrsl.common.domain.model.DetectedScreen
import com.stepler.steplerrsl.common.domain.model.DetectedWord
import com.stepler.steplerrsl.common.domain.model.Screenshot
import com.stepler.steplerrsl.common.domain.screendetection.ScreenDetector
import com.stepler.steplerrsl.feature.screendetection.domain.usecase.DetectEnglishWordsUseCase
import com.stepler.steplerrsl.feature.screendetection.domain.usecase.DetectScreenUseCase
import com.stepler.steplerrsl.feature.screendetection.domain.usecase.SaveScreenshotUseCase
import javax.inject.Inject

/** Exposes this feature's use cases through the shared [ScreenDetector] contract. */
class ScreenDetectorImpl @Inject constructor(
    private val saveScreenshot: SaveScreenshotUseCase,
    private val detectEnglishWords: DetectEnglishWordsUseCase,
    private val detectScreenUseCase: DetectScreenUseCase,
) : ScreenDetector {

    override fun keepScreenshot(screenshot: Screenshot) = saveScreenshot(screenshot)

    override suspend fun detectWords(screenshot: Screenshot): Result<List<DetectedWord>> =
        detectEnglishWords(screenshot)

    override fun detectScreen(words: List<DetectedWord>): DetectedScreen? =
        detectScreenUseCase(words)?.let { match ->
            DetectedScreen(
                name = match.screen.name,
                matchedWords = match.matchedWords.size,
                totalWords = match.screen.requiredWords.size,
            )
        }
}
