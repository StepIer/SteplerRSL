package com.stepler.steplerrsl.feature.screendetection.domain.usecase

import com.stepler.steplerrsl.common.domain.model.DetectedWord
import com.stepler.steplerrsl.common.domain.model.Screenshot
import com.stepler.steplerrsl.feature.screendetection.domain.repository.TextRecognitionRepository
import javax.inject.Inject

/**
 * Finds English words on a screenshot: recognized text elements made of Latin letters
 * (apostrophes/hyphens inside allowed), with surrounding punctuation trimmed.
 * Numbers and symbols are dropped.
 */
class DetectEnglishWordsUseCase @Inject constructor(
    private val repository: TextRecognitionRepository,
) {
    suspend operator fun invoke(screenshot: Screenshot): Result<List<DetectedWord>> =
        repository.recognizeWords(screenshot).map { words ->
            words.mapNotNull { word ->
                val text = word.text.trim { !it.isLetterOrDigit() }
                if (ENGLISH_WORD.matches(text)) word.copy(text = text) else null
            }
        }

    private companion object {
        val ENGLISH_WORD = Regex("^[A-Za-z]+(?:['’-][A-Za-z]+)*$")
    }
}
