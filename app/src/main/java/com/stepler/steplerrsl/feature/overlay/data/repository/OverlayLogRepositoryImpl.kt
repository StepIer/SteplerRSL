package com.stepler.steplerrsl.feature.overlay.data.repository

import android.util.Log
import com.stepler.steplerrsl.feature.overlay.domain.repository.OverlayLogRepository
import javax.inject.Inject

class OverlayLogRepositoryImpl @Inject constructor() : OverlayLogRepository {
    override fun log(message: String) {
        Log.d(TAG, message)
    }

    private companion object {
        const val TAG = "rawr"
    }
}
