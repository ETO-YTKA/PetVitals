package com.example.petvitals.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.petvitals.ui.theme.Dimen

@Composable
fun ScreenLayout(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        modifier = modifier
            .fillMaxSize()
            .padding(Dimen.spaceLarge)
    ) {
        content()
    }
}