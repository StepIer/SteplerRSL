package com.stepler.steplerrsl.feature.overlay.presentation.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stepler.steplerrsl.R
import com.stepler.steplerrsl.core.ui.theme.OverlayBackground
import com.stepler.steplerrsl.core.ui.theme.OverlayOnBackground
import com.stepler.steplerrsl.core.ui.theme.SteplerRSLTheme

/**
 * Draggable block with a semi-transparent black background: action buttons and a log panel.
 * [onDrag] receives the drag delta in pixels; the host moves the overlay window by it.
 * Dragging inside the log panel scrolls the log instead of moving the window.
 */
@Composable
fun OverlayContent(
    state: OverlayState,
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
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRawrClick) {
                Text(text = stringResource(R.string.overlay_button))
            }
            Button(onClick = onScreenshotClick, enabled = !state.isBusy) {
                Text(text = stringResource(R.string.overlay_screenshot))
            }
        }
        LogPanel(logs = state.logs)
    }
}

@Composable
private fun LogPanel(logs: List<LogEntry>) {
    val listState = rememberLazyListState()
    LaunchedEffect(logs.lastOrNull()?.id) {
        if (logs.isNotEmpty()) listState.scrollToItem(logs.lastIndex)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .width(280.dp)
            .height(200.dp),
    ) {
        if (logs.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.overlay_log_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = OverlayOnBackground,
                )
            }
        }
        items(logs, key = { it.id }) { entry ->
            // Time on the left, message on the right so multi-line messages stay aligned.
            val style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = entry.time, style = style, color = OverlayOnBackground)
                Text(text = entry.message, style = style, color = OverlayOnBackground)
            }
        }
    }
}

@Preview
@Composable
private fun OverlayContentPreview() {
    SteplerRSLTheme {
        OverlayContent(
            state = OverlayState(
                logs = listOf(
                    LogEntry(0, "15:40:01", "Taking screenshot..."),
                    LogEntry(1, "15:40:01", "Screenshot taken (1080x2400), kept in memory"),
                    LogEntry(2, "15:40:01", "Detecting words..."),
                    LogEntry(3, "15:40:02", "Found 1 English words"),
                    LogEntry(4, "15:40:02", "\"Play\" [420, 1800, 660, 1880]"),
                    LogEntry(5, "15:40:02", "Detecting screen..."),
                    LogEntry(6, "15:40:02", "No screen matches"),
                ),
            ),
            onRawrClick = {},
            onScreenshotClick = {},
            onDrag = {},
        )
    }
}
