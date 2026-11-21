package com.stepler.steplerrsl.feature.overlay.di

import com.stepler.steplerrsl.feature.overlay.data.repository.NotificationPermissionRepositoryImpl
import com.stepler.steplerrsl.feature.overlay.data.repository.OverlayLogRepositoryImpl
import com.stepler.steplerrsl.feature.overlay.data.repository.OverlayPermissionRepositoryImpl
import com.stepler.steplerrsl.feature.overlay.data.repository.ScreenshotRepositoryImpl
import com.stepler.steplerrsl.feature.overlay.domain.repository.NotificationPermissionRepository
import com.stepler.steplerrsl.feature.overlay.domain.repository.OverlayLogRepository
import com.stepler.steplerrsl.feature.overlay.domain.repository.OverlayPermissionRepository
import com.stepler.steplerrsl.feature.overlay.domain.repository.ScreenshotRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface OverlayModule {

    @Binds
    fun bindOverlayPermissionRepository(impl: OverlayPermissionRepositoryImpl): OverlayPermissionRepository

    @Binds
    fun bindOverlayLogRepository(impl: OverlayLogRepositoryImpl): OverlayLogRepository

    @Binds
    fun bindScreenshotRepository(impl: ScreenshotRepositoryImpl): ScreenshotRepository

    @Binds
    fun bindNotificationPermissionRepository(impl: NotificationPermissionRepositoryImpl): NotificationPermissionRepository
}
