package com.stepler.steplerrsl.feature.screendetection.data.repository

import com.stepler.steplerrsl.feature.screendetection.domain.model.ScreenDefinition
import com.stepler.steplerrsl.feature.screendetection.domain.repository.ScreenDefinitionRepository
import javax.inject.Inject

/** Known game screens and the words that identify them (matched case-insensitively). */
class ScreenDefinitionRepositoryImpl @Inject constructor() : ScreenDefinitionRepository {

    override fun getAll(): List<ScreenDefinition> = SCREENS

    private companion object {
        val SCREENS = listOf(
            ScreenDefinition(
                name = "Main",
                requiredWords = setOf(
                    "Missions",
                    "Quests",
                    "Challenges",
                    "Clans",
                    "Index",
                    "Champions",
                    "Battle",
                ),
            ),
        )
    }
}
