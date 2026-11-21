package com.stepler.steplerrsl.feature.autotap.data

import com.stepler.steplerrsl.common.domain.autotap.ScreenTapper
import com.stepler.steplerrsl.feature.autotap.data.service.TapServiceHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ScreenTapperImpl @Inject constructor(
    private val holder: TapServiceHolder,
) : ScreenTapper {

    override fun isEnabled(): Boolean = holder.service != null

    override suspend fun tap(x: Int, y: Int): Result<Unit> = withContext(Dispatchers.Main) {
        val service = holder.service
            ?: return@withContext Result.failure(IllegalStateException(SERVICE_OFF_MESSAGE))
        if (service.tap(x.toFloat(), y.toFloat())) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("tap was cancelled by the system"))
        }
    }

    private companion object {
        const val SERVICE_OFF_MESSAGE =
            "Accessibility Service is off. Enable SteplerRSL in Settings > Accessibility"
    }
}
