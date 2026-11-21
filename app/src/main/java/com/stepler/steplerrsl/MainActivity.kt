package com.stepler.steplerrsl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.stepler.steplerrsl.core.ui.theme.SteplerRSLTheme
import com.stepler.steplerrsl.feature.overlay.presentation.overlay.OverlayService
import com.stepler.steplerrsl.feature.overlay.presentation.start.StartOverlayScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SteplerRSLTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StartOverlayScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onDestroy() {
        // The app is being closed (not just rotated): close the overlay together with it.
        if (isFinishing) OverlayService.stop(this)
        super.onDestroy()
    }
}
