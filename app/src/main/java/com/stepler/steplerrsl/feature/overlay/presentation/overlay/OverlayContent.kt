package com.stepler.steplerrsl.feature.overlay.presentation.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
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
 * Draggable block with a semi-transparent black background holding the overlay button.
 * [onDrag] receives the drag delta in pixels; the host moves the overlay window by it.
 */
@Composable
fun OverlayContent(
    onButtonClick: () -> Unit,
    onDrag: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnDrag by rememberUpdatedState(onDrag)

    Box(
        modifier = modifier
            .background(OverlayBackground, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    currentOnDrag(dragAmount)
                }
            }
            .padding(16.dp),
    ) {
        Button(onClick = onButtonClick) {
            Text(text = stringResource(R.string.overlay_button))
        }
    }
}

@Preview
@Composable
private fun OverlayContentPreview() {
    SteplerRSLTheme {
        OverlayContent(onButtonClick = {}, onDrag = {})
    }
}
