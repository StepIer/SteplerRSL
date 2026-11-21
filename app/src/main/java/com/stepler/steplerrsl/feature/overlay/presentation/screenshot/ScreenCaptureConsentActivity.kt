package com.stepler.steplerrsl.feature.overlay.presentation.screenshot

import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.stepler.steplerrsl.feature.overlay.presentation.overlay.OverlayService

/**
 * Invisible activity that shows the system "start recording / casting" consent dialog
 * (an Activity is required for it) and hands the result to [OverlayService].
 */
class ScreenCaptureConsentActivity : ComponentActivity() {

    private val consentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            OverlayService.deliverCaptureConsent(this, result.resultCode, result.data)
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val manager = getSystemService(MediaProjectionManager::class.java)
            consentLauncher.launch(manager.createScreenCaptureIntent())
        }
    }
}
