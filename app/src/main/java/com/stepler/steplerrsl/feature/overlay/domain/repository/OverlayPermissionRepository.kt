package com.stepler.steplerrsl.feature.overlay.domain.repository

interface OverlayPermissionRepository {
    fun isGranted(): Boolean
}
