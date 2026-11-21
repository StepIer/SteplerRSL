package com.stepler.steplerrsl.common.domain.model

/**
 * A captured screen kept in memory only.
 * [pixels] are ARGB_8888 colors, row by row, `width * height` long.
 */
class Screenshot(
    val width: Int,
    val height: Int,
    val pixels: IntArray,
)
