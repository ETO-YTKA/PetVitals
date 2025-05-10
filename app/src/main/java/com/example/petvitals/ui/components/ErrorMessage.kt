package com.example.petvitals.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.petvitals.ui.theme.Dimen

@Composable
fun ErrorMessage(message: String) {
    Spacer(modifier = Modifier.height(Dimen.spaceSmall))
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error
    )
}