package com.stepler.steplerrsl.feature.screendetection.data.repository

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.stepler.steplerrsl.common.domain.model.DetectedWord
import com.stepler.steplerrsl.common.domain.model.Screenshot
import com.stepler.steplerrsl.common.domain.model.WordBounds
import com.stepler.steplerrsl.feature.screendetection.domain.repository.TextRecognitionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** On-device OCR with ML Kit's bundled Latin-script text recognizer. */
@Singleton
class TextRecognitionRepositoryImpl @Inject constructor() : TextRecognitionRepository {

    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    override suspend fun recognizeWords(screenshot: Screenshot): Result<List<DetectedWord>> =
        withContext(Dispatchers.Default) {
            runCatching {
                val bitmap = Bitmap.createBitmap(
                    screenshot.pixels,
                    screenshot.width,
                    screenshot.height,
                    Bitmap.Config.ARGB_8888,
                )
                try {
                    val text = recognizer.process(InputImage.fromBitmap(bitmap, 0)).await()
                    text.textBlocks
                        .flatMap { it.lines }
                        .flatMap { it.elements }
                        .mapNotNull { element ->
                            val box = element.boundingBox ?: return@mapNotNull null
                            DetectedWord(
                                text = element.text,
                                bounds = WordBounds(box.left, box.top, box.right, box.bottom),
                            )
                        }
                } finally {
                    bitmap.recycle()
                }
            }
        }
}
