package com.stepler.steplerrsl.feature.screendetection.domain.usecase

import com.stepler.steplerrsl.common.domain.model.DetectedWord
import com.stepler.steplerrsl.feature.screendetection.domain.model.ScreenMatch
import com.stepler.steplerrsl.feature.screendetection.domain.repository.ScreenDefinitionRepository
import javax.inject.Inject

/**
 * Finds the known screen best matching [words] (case-insensitive).
 * A screen qualifies when the share of its required words found reaches its `minMatchRatio`;
 * among qualifying screens the highest share wins. Returns null when no screen matches.
 */
class DetectScreenUseCase @Inject constructor(
    private val repository: ScreenDefinitionRepository,
) {
    operator fun invoke(words: List<DetectedWord>): ScreenMatch? {
        val found = words.mapTo(HashSet()) { it.text.lowercase() }
        return repository.getAll()
            .filter { it.requiredWords.isNotEmpty() }
            .map { screen ->
                ScreenMatch(screen, screen.requiredWords.filterTo(LinkedHashSet()) { it.lowercase() in found })
            }
            .filter { it.ratio >= it.screen.minMatchRatio }
            .maxByOrNull { it.ratio }
    }
}
