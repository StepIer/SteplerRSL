package com.stepler.steplerrsl.feature.overlay.domain.usecase

import com.stepler.steplerrsl.feature.overlay.domain.repository.OverlayPermissionRepository
import javax.inject.Inject

class CheckOverlayPermissionUseCase @Inject constructor(
    private val repository: OverlayPermissionRepository,
) {
    operator fun invoke(): Boolean = repository.isGranted()
}
