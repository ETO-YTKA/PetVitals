package com.example.petvitals.ui.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.petvitals.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarBackButton(
    onPopBackStack: () -> Unit,
    title: String = ""
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onPopBackStack
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = null
                )
            }
        }
    )
}