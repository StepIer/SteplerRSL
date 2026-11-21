package com.stepler.steplerrsl.feature.overlay.presentation.start

import androidx.lifecycle.ViewModel
import com.stepler.steplerrsl.feature.overlay.domain.usecase.CheckNotificationPermissionUseCase
import com.stepler.steplerrsl.feature.overlay.domain.usecase.CheckOverlayPermissionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class StartOverlayViewModel @Inject constructor(
    private val checkOverlayPermissionUseCase: CheckOverlayPermissionUseCase,
    private val checkNotificationPermissionUseCase: CheckNotificationPermissionUseCase,
) : ViewModel(), ContainerHost<StartOverlayState, StartOverlaySideEffect> {

    override val container = container<StartOverlayState, StartOverlaySideEffect>(StartOverlayState())

    /** Called when the screen is resumed, e.g. after returning from the permission settings. */
    fun onResume() = intent {
        reduce { state.copy(isPermissionGranted = checkOverlayPermissionUseCase()) }
    }

    fun onStartClick() = intent {
        val granted = checkOverlayPermissionUseCase()
        reduce { state.copy(isPermissionGranted = granted) }
        postSideEffect(
            when {
                !granted -> StartOverlaySideEffect.OpenPermissionSettings
                !checkNotificationPermissionUseCase() -> StartOverlaySideEffect.RequestNotificationPermission
                else -> StartOverlaySideEffect.StartOverlay
            }
        )
    }

    /** Notifications are optional (overlay status + toasts), so start the overlay either way. */
    fun onNotificationPermissionResult(@Suppress("UNUSED_PARAMETER") granted: Boolean) = intent {
        postSideEffect(StartOverlaySideEffect.StartOverlay)
    }
}
