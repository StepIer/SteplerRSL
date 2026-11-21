package com.stepler.steplerrsl.common.domain.autotap

/**
 * Contract of the auto-tap feature: taps on the real screen, on top of any app (e.g. the game).
 * Implemented and bound by `feature.autotap`; other features inject only this interface.
 */
interface ScreenTapper {
    /** True when the system lets the app tap (its Accessibility Service is enabled). */
    fun isEnabled(): Boolean

    /** Taps at ([x], [y]) in screen pixels. Fails if tapping is not enabled or was cancelled. */
    suspend fun tap(x: Int, y: Int): Result<Unit>
}
