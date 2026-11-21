package com.stepler.steplerrsl.feature.autotap.di

import com.stepler.steplerrsl.common.domain.autotap.ScreenTapper
import com.stepler.steplerrsl.feature.autotap.data.ScreenTapperImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AutoTapModule {

    @Binds
    fun bindScreenTapper(impl: ScreenTapperImpl): ScreenTapper
}
