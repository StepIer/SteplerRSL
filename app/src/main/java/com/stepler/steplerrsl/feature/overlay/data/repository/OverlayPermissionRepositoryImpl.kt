package com.stepler.steplerrsl.feature.overlay.data.repository

import android.content.Context
import android.provider.Settings
import com.stepler.steplerrsl.feature.overlay.domain.repository.OverlayPermissionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class OverlayPermissionRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : OverlayPermissionRepository {
    override fun isGranted(): Boolean = Settings.canDrawOverlays(context)
}
