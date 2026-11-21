package com.stepler.steplerrsl.feature.screendetection.domain.repository

import com.stepler.steplerrsl.common.domain.model.DetectedWord
import com.stepler.steplerrsl.common.domain.model.Screenshot

interface TextRecognitionRepository {
    /** Returns every word-level text element found on [screenshot], unfiltered. */
    suspend fun recognizeWords(screenshot: Screenshot): Result<List<DetectedWord>>
}
