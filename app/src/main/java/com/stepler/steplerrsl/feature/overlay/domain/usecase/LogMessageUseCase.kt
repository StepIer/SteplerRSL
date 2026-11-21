package com.stepler.steplerrsl.feature.overlay.domain.usecase

import com.stepler.steplerrsl.feature.overlay.domain.repository.OverlayLogRepository
import javax.inject.Inject

/** Writes a message to the app log (logcat, tag `rawr`). */
class LogMessageUseCase @Inject constructor(
    private val repository: OverlayLogRepository,
) {
    operator fun invoke(message: String) = repository.log(message)
}
