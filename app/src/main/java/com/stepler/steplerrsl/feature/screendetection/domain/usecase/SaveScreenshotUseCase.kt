package com.stepler.steplerrsl.feature.screendetection.domain.usecase

import com.stepler.steplerrsl.common.domain.model.Screenshot
import com.stepler.steplerrsl.feature.screendetection.domain.repository.ScreenshotRepository
import javax.inject.Inject

/** Keeps [Screenshot] in memory as the latest capture. */
class SaveScreenshotUseCase @Inject constructor(
    private val repository: ScreenshotRepository,
) {
    operator fun invoke(screenshot: Screenshot) = repository.save(screenshot)
}
