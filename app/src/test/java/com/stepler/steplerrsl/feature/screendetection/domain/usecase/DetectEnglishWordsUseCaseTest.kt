package com.stepler.steplerrsl.feature.screendetection.domain.usecase

import com.stepler.steplerrsl.common.domain.model.DetectedWord
import com.stepler.steplerrsl.common.domain.model.Screenshot
import com.stepler.steplerrsl.common.domain.model.WordBounds
import com.stepler.steplerrsl.feature.screendetection.domain.repository.TextRecognitionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DetectEnglishWordsUseCaseTest {

    private val bounds = WordBounds(1, 2, 3, 4)

    private fun useCase(vararg texts: String) = DetectEnglishWordsUseCase(
        object : TextRecognitionRepository {
            override suspend fun recognizeWords(screenshot: Screenshot) =
                Result.success(texts.map { DetectedWord(it, bounds) })
        }
    )

    @Test
    fun `keeps english words, trims punctuation, drops numbers and symbols`() = runTest {
        val words = useCase("Play", "Level:", "123", "x2", "don't", "co-op", "!!", "€5", "Привет")
            .invoke(Screenshot(1, 1, IntArray(1)))
            .getOrThrow()

        assertEquals(listOf("Play", "Level", "don't", "co-op"), words.map { it.text })
        assertEquals(bounds, words.first().bounds)
    }
}
