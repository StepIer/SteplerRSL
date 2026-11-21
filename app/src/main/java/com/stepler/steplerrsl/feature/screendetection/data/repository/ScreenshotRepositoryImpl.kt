package com.stepler.steplerrsl.feature.screendetection.data.repository

import com.stepler.steplerrsl.common.domain.model.Screenshot
import com.stepler.steplerrsl.feature.screendetection.domain.repository.ScreenshotRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Keeps only the latest screenshot, in memory. Nothing is written to storage or the gallery. */
@Singleton
class ScreenshotRepositoryImpl @Inject constructor() : ScreenshotRepository {

    @Volatile
    private var latest: Screenshot? = null

    override fun save(screenshot: Screenshot) {
        latest = screenshot
    }

    override fun latest(): Screenshot? = latest
}
