package com.stepler.steplerrsl.feature.screendetection.domain.model

/**
 * A known game screen, recognized by the words that appear on it.
 * It matches when at least [minMatchRatio] of [requiredWords] are found, so OCR missing a word
 * or two doesn't block detection.
 */
data class ScreenDefinition(
    val name: String,
    val requiredWords: Set<String>,
    val minMatchRatio: Double = DEFAULT_MIN_MATCH_RATIO,
) {
    companion object {
        const val DEFAULT_MIN_MATCH_RATIO = 0.7
    }
}

/** A screen whose words were found on a screenshot: [matchedWords] out of its required words. */
data class ScreenMatch(
    val screen: ScreenDefinition,
    val matchedWords: Set<String>,
) {
    val ratio: Double
        get() = matchedWords.size.toDouble() / screen.requiredWords.size
}
