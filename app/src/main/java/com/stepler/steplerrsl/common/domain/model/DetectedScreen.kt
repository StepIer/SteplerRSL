package com.stepler.steplerrsl.common.domain.model

/** A known game screen recognized on a screenshot: [matchedWords] of its [totalWords] were found. */
data class DetectedScreen(
    val name: String,
    val matchedWords: Int,
    val totalWords: Int,
)
