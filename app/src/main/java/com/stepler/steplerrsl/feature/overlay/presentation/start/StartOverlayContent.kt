package com.stepler.steplerrsl.feature.overlay.presentation.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stepler.steplerrsl.R
import com.stepler.steplerrsl.core.ui.theme.SteplerRSLTheme

@Composable
fun StartOverlayContent(
    state: StartOverlayState,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!state.isPermissionGranted) {
            Text(
                text = stringResource(R.string.overlay_permission_required),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        Button(onClick = onStartClick) {
            Text(text = stringResource(R.string.overlay_start))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StartOverlayContentPreview() {
    SteplerRSLTheme {
        StartOverlayContent(state = StartOverlayState(), onStartClick = {})
    }
}
