package com.stepler.steplerrsl.feature.overlay.domain.usecase

import com.stepler.steplerrsl.common.domain.model.DetectedScreen
import com.stepler.steplerrsl.common.domain.model.DetectedWord
import com.stepler.steplerrsl.common.domain.model.WordBounds
import com.stepler.steplerrsl.feature.overlay.domain.model.ScreenAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetScreenActionUseCaseTest {

    private val useCase = GetScreenActionUseCase()
    private val main = DetectedScreen("Main", matchedWords = 7, totalWords = 8)

    @Test
    fun `main screen taps center of Battle`() {
        val words = listOf(
            DetectedWord("Shop", WordBounds(0, 0, 10, 10)),
            DetectedWord("BATTLE", WordBounds(2000, 900, 2200, 960)),
        )
        assertEquals(ScreenAction.TapWord("BATTLE", 2100, 930), useCase(main, words))
    }

    @Test
    fun `main screen without Battle word reports word not found`() {
        val words = listOf(DetectedWord("Shop", WordBounds(0, 0, 10, 10)))
        assertEquals(ScreenAction.WordNotFound("Battle"), useCase(main, words))
    }

    @Test
    fun `screen without a rule has no action`() {
        assertNull(useCase(DetectedScreen("Other", 3, 3), emptyList()))
    }
}
