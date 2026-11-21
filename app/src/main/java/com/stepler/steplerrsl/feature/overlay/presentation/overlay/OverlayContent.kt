package com.stepler.steplerrsl.feature.overlay.presentation.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stepler.steplerrsl.R
import com.stepler.steplerrsl.core.ui.theme.OverlayBackground
import com.stepler.steplerrsl.core.ui.theme.SteplerRSLTheme

/**
 * Draggable block with a semi-transparent black background holding the overlay buttons.
 * [onDrag] receives the drag delta in pixels; the host moves the overlay window by it.
 */
@Composable
fun OverlayContent(
    onRawrClick: () -> Unit,
    onScreenshotClick: () -> Unit,
    onDrag: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnDrag by rememberUpdatedState(onDrag)

    Column(
        modifier = modifier
            .background(OverlayBackground, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    currentOnDrag(dragAmount)
                }
            }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = onRawrClick) {
            Text(text = stringResource(R.string.overlay_button))
        }
        Button(onClick = onScreenshotClick) {
            Text(text = stringResource(R.string.overlay_screenshot))
        }
    }
}

@Preview
@Composable
private fun OverlayContentPreview() {
    SteplerRSLTheme {
        OverlayContent(onRawrClick = {}, onScreenshotClick = {}, onDrag = {})
    }
}
