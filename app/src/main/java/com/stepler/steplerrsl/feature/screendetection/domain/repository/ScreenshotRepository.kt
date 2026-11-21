package com.stepler.steplerrsl.feature.screendetection.domain.repository

import com.stepler.steplerrsl.common.domain.model.Screenshot

/** Holds the latest screenshot in memory only (nothing is written to storage). */
interface ScreenshotRepository {
    fun save(screenshot: Screenshot)
    fun latest(): Screenshot?
}
