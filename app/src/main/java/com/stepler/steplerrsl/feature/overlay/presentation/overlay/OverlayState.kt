package com.stepler.steplerrsl.feature.overlay.presentation.overlay

data class OverlayState(
    val isBusy: Boolean = false,
    val logs: List<LogEntry> = emptyList(),
)

data class LogEntry(
    val id: Long,
    val time: String,
    val message: String,
)
