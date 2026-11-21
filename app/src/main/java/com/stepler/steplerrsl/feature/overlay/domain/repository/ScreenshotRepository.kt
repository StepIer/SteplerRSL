package com.stepler.steplerrsl.feature.overlay.domain.repository

interface ScreenshotRepository {
    /** Saves a PNG-encoded screenshot to the device gallery. */
    suspend fun saveToGallery(pngBytes: ByteArray): Result<Unit>
}
