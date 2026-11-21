package com.stepler.steplerrsl.feature.screendetection.di

import com.stepler.steplerrsl.common.domain.screendetection.ScreenDetector
import com.stepler.steplerrsl.feature.screendetection.data.repository.ScreenDefinitionRepositoryImpl
import com.stepler.steplerrsl.feature.screendetection.data.repository.ScreenshotRepositoryImpl
import com.stepler.steplerrsl.feature.screendetection.data.repository.TextRecognitionRepositoryImpl
import com.stepler.steplerrsl.feature.screendetection.domain.ScreenDetectorImpl
import com.stepler.steplerrsl.feature.screendetection.domain.repository.ScreenDefinitionRepository
import com.stepler.steplerrsl.feature.screendetection.domain.repository.ScreenshotRepository
import com.stepler.steplerrsl.feature.screendetection.domain.repository.TextRecognitionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface ScreenDetectionModule {

    @Binds
    fun bindScreenDetector(impl: ScreenDetectorImpl): ScreenDetector

    @Binds
    fun bindScreenshotRepository(impl: ScreenshotRepositoryImpl): ScreenshotRepository

    @Binds
    fun bindTextRecognitionRepository(impl: TextRecognitionRepositoryImpl): TextRecognitionRepository

    @Binds
    fun bindScreenDefinitionRepository(impl: ScreenDefinitionRepositoryImpl): ScreenDefinitionRepository
}
