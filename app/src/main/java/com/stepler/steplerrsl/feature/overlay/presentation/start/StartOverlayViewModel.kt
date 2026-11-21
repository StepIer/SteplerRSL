package com.stepler.steplerrsl.feature.overlay.presentation.start

import androidx.lifecycle.ViewModel
import com.stepler.steplerrsl.feature.overlay.domain.usecase.CheckOverlayPermissionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class StartOverlayViewModel @Inject constructor(
    private val checkOverlayPermissionUseCase: CheckOverlayPermissionUseCase,
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
            if (granted) StartOverlaySideEffect.StartOverlay
            else StartOverlaySideEffect.OpenPermissionSettings
        )
    }
}
