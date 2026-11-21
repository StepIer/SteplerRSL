package com.stepler.steplerrsl.feature.overlay.presentation.start

import com.stepler.steplerrsl.feature.overlay.domain.repository.NotificationPermissionRepository
import com.stepler.steplerrsl.feature.overlay.domain.repository.OverlayPermissionRepository
import com.stepler.steplerrsl.feature.overlay.domain.usecase.CheckNotificationPermissionUseCase
import com.stepler.steplerrsl.feature.overlay.domain.usecase.CheckOverlayPermissionUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.orbitmvi.orbit.test.test

class StartOverlayViewModelTest {

    private fun viewModel(overlayGranted: Boolean, notificationsGranted: Boolean = true) =
        StartOverlayViewModel(
            CheckOverlayPermissionUseCase(FakeOverlayPermissionRepository(overlayGranted)),
            CheckNotificationPermissionUseCase(FakeNotificationPermissionRepository(notificationsGranted)),
        )

    @Test
    fun `start click with all permissions starts overlay`() = runTest {
        viewModel(overlayGranted = true).test(this) {
            containerHost.onStartClick()
            expectState { copy(isPermissionGranted = true) }
            expectSideEffect(StartOverlaySideEffect.StartOverlay)
        }
    }

    @Test
    fun `start click without overlay permission opens settings`() = runTest {
        viewModel(overlayGranted = false).test(this) {
            containerHost.onStartClick()
            expectSideEffect(StartOverlaySideEffect.OpenPermissionSettings)
        }
    }

    @Test
    fun `start click without notification permission requests it`() = runTest {
        viewModel(overlayGranted = true, notificationsGranted = false).test(this) {
            containerHost.onStartClick()
            expectState { copy(isPermissionGranted = true) }
            expectSideEffect(StartOverlaySideEffect.RequestNotificationPermission)
        }
    }

    @Test
    fun `overlay starts after notification permission result even if denied`() = runTest {
        viewModel(overlayGranted = true, notificationsGranted = false).test(this) {
            containerHost.onNotificationPermissionResult(granted = false)
            expectSideEffect(StartOverlaySideEffect.StartOverlay)
        }
    }

    private class FakeOverlayPermissionRepository(private val granted: Boolean) : OverlayPermissionRepository {
        override fun isGranted() = granted
    }

    private class FakeNotificationPermissionRepository(private val granted: Boolean) :
        NotificationPermissionRepository {
        override fun isGranted() = granted
    }
}
