package com.stepler.steplerrsl.common.domain.model

/** A word found on a screenshot and its position in screen pixels. */
data class DetectedWord(
    val text: String,
    val bounds: WordBounds,
)

data class WordBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)
