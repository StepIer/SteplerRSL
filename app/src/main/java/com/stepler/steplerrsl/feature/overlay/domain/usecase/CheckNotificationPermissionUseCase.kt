package com.stepler.steplerrsl.feature.overlay.domain.usecase

import com.stepler.steplerrsl.feature.overlay.domain.repository.NotificationPermissionRepository
import javax.inject.Inject

class CheckNotificationPermissionUseCase @Inject constructor(
    private val repository: NotificationPermissionRepository,
) {
    operator fun invoke(): Boolean = repository.isGranted()
}
