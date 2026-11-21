package com.stepler.steplerrsl.feature.screendetection.domain.repository

import com.stepler.steplerrsl.feature.screendetection.domain.model.ScreenDefinition

interface ScreenDefinitionRepository {
    fun getAll(): List<ScreenDefinition>
}
