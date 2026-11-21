package com.stepler.steplerrsl.feature.overlay.domain.repository

interface NotificationPermissionRepository {
    fun isGranted(): Boolean
}
