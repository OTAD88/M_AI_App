package com.paperink.pdf.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset

fun Modifier.graphicsLayerCompat(scale: Float, pan: Offset): Modifier = graphicsLayer {
    scaleX = scale
    scaleY = scale
    translationX = pan.x
    translationY = pan.y
}
