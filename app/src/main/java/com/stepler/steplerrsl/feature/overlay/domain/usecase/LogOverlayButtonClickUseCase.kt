package com.stepler.steplerrsl.feature.overlay.domain.usecase

import com.stepler.steplerrsl.feature.overlay.domain.repository.OverlayLogRepository
import javax.inject.Inject

class LogOverlayButtonClickUseCase @Inject constructor(
    private val repository: OverlayLogRepository,
) {
    operator fun invoke() = repository.log("Overlay button clicked")
}
