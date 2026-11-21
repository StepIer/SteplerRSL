package com.stepler.steplerrsl.feature.overlay.presentation.start

import com.stepler.steplerrsl.feature.overlay.domain.repository.OverlayPermissionRepository
import com.stepler.steplerrsl.feature.overlay.domain.usecase.CheckOverlayPermissionUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.orbitmvi.orbit.test.test

class StartOverlayViewModelTest {

    private fun viewModel(granted: Boolean) = StartOverlayViewModel(
        CheckOverlayPermissionUseCase(FakePermissionRepository(granted))
    )

    @Test
    fun `start click with permission starts overlay`() = runTest {
        viewModel(granted = true).test(this) {
            containerHost.onStartClick()
            expectState { copy(isPermissionGranted = true) }
            expectSideEffect(StartOverlaySideEffect.StartOverlay)
        }
    }

    @Test
    fun `start click without permission opens settings`() = runTest {
        viewModel(granted = false).test(this) {
            containerHost.onStartClick()
            expectSideEffect(StartOverlaySideEffect.OpenPermissionSettings)
        }
    }

    private class FakePermissionRepository(private val granted: Boolean) : OverlayPermissionRepository {
        override fun isGranted() = granted
    }
}
