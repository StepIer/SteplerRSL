package com.stepler.steplerrsl.feature.screendetection.domain.usecase

import com.stepler.steplerrsl.common.domain.model.DetectedWord
import com.stepler.steplerrsl.common.domain.model.WordBounds
import com.stepler.steplerrsl.feature.screendetection.data.repository.ScreenDefinitionRepositoryImpl
import com.stepler.steplerrsl.feature.screendetection.domain.model.ScreenDefinition
import com.stepler.steplerrsl.feature.screendetection.domain.repository.ScreenDefinitionRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DetectScreenUseCaseTest {

    private fun words(vararg texts: String) = texts.map { DetectedWord(it, WordBounds(0, 0, 0, 0)) }

    private fun useCase(vararg screens: ScreenDefinition) = DetectScreenUseCase(
        object : ScreenDefinitionRepository {
            override fun getAll() = screens.toList()
        }
    )

    private val mainScreen = DetectScreenUseCase(ScreenDefinitionRepositoryImpl())

    @Test
    fun `no screen definitions means no match`() {
        assertNull(useCase().invoke(words("Play", "Settings")))
    }

    @Test
    fun `main screen matches with all seven words in any case`() {
        val match = mainScreen(
            words("MISSIONS", "Quests", "challenges", "Clans", "Index", "Champions", "Battle", "Shop")
        )
        assertEquals("Main", match?.screen?.name)
        assertEquals(7, match?.matchedWords?.size)
    }

    @Test
    fun `main screen still matches when two words are missed (5 of 7 is over 70 percent)`() {
        val match = mainScreen(words("Missions", "Quests", "Clans", "Champions", "Battle"))
        assertEquals("Main", match?.screen?.name)
        assertEquals(5, match?.matchedWords?.size)
    }

    @Test
    fun `main screen does not match with only 4 of 7 words`() {
        assertNull(mainScreen(words("Missions", "Quests", "Clans", "Battle", "Shop", "Settings")))
    }

    @Test
    fun `best matching screen wins`() {
        val a = ScreenDefinition("A", setOf("one", "two", "three", "four"), minMatchRatio = 0.5)
        val b = ScreenDefinition("B", setOf("one", "two", "five"), minMatchRatio = 0.5)
        assertEquals("B", useCase(a, b).invoke(words("one", "two", "five"))?.screen?.name)
    }
}
