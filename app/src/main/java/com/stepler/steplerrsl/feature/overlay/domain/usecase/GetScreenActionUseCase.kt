package com.stepler.steplerrsl.feature.overlay.domain.usecase

import com.stepler.steplerrsl.common.domain.model.DetectedScreen
import com.stepler.steplerrsl.common.domain.model.DetectedWord
import com.stepler.steplerrsl.feature.overlay.domain.model.ScreenAction
import javax.inject.Inject

/**
 * Decides the automatic action for a detected screen.
 * Rules: screen name → word to tap (case-insensitive). Returns null when the screen has no rule.
 */
class GetScreenActionUseCase @Inject constructor() {

    operator fun invoke(screen: DetectedScreen, words: List<DetectedWord>): ScreenAction? {
        val wordToTap = TAP_RULES[screen.name] ?: return null
        val word = words.firstOrNull { it.text.equals(wordToTap, ignoreCase = true) }
            ?: return ScreenAction.WordNotFound(wordToTap)
        val b = word.bounds
        return ScreenAction.TapWord(
            word = word.text,
            x = (b.left + b.right) / 2,
            y = (b.top + b.bottom) / 2,
        )
    }

    private companion object {
        val TAP_RULES = mapOf(
            "Main" to "Battle",
        )
    }
}
