package com.stepler.steplerrsl.feature.overlay.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.stepler.steplerrsl.feature.overlay.domain.repository.ScreenshotRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ScreenshotRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ScreenshotRepository {

    override suspend fun saveToGallery(pngBytes: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val fileName = "Screenshot_${SimpleDateFormat(DATE_PATTERN, Locale.US).format(Date())}.png"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$ALBUM")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val uri = resolver.insert(collection, values) ?: error("MediaStore insert failed")
            try {
                resolver.openOutputStream(uri)?.use { it.write(pngBytes) }
                    ?: error("Cannot open output stream for $uri")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
                }
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
            Unit
        }
    }

    private companion object {
        const val ALBUM = "SteplerRSL"
        const val DATE_PATTERN = "yyyyMMdd_HHmmss"
    }
}
