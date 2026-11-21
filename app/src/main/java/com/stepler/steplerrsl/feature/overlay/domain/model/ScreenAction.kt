package com.stepler.steplerrsl.feature.overlay.domain.model

/** What to do automatically on a detected screen. */
sealed interface ScreenAction {
    /** Tap the center of the word [word] found at ([x], [y]) in screen pixels. */
    data class TapWord(val word: String, val x: Int, val y: Int) : ScreenAction

    /** The screen has a tap rule but its word wasn't found on the screenshot. */
    data class WordNotFound(val word: String) : ScreenAction
}
