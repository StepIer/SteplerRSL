package com.stepler.steplerrsl.feature.autotap.data.service

import javax.inject.Inject
import javax.inject.Singleton

/** Holds the connected [TapAccessibilityService], or null when it's disabled. */
@Singleton
class TapServiceHolder @Inject constructor() {
    @Volatile
    var service: TapAccessibilityService? = null
}
