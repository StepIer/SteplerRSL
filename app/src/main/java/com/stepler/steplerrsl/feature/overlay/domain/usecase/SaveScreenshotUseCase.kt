package com.stepler.steplerrsl.feature.overlay.domain.usecase

import com.stepler.steplerrsl.feature.overlay.domain.repository.ScreenshotRepository
import javax.inject.Inject

class SaveScreenshotUseCase @Inject constructor(
    private val repository: ScreenshotRepository,
) {
    suspend operator fun invoke(pngBytes: ByteArray): Result<Unit> = repository.saveToGallery(pngBytes)
}
