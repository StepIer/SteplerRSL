package com.stepler.steplerrsl.feature.overlay.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.stepler.steplerrsl.feature.overlay.domain.repository.NotificationPermissionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationPermissionRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : NotificationPermissionRepository {

    /** Before Android 13 there is no runtime notification permission, so it's always granted. */
    override fun isGranted(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
}
