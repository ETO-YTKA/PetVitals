package com.example.petvitals.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun CustomIconButton(
    onClick: () -> Unit,
    painter: Painter,
    contentDescription: String? = null
) {
    IconButton(
        onClick = onClick
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription
        )
    }
}