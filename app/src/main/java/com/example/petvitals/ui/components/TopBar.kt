package com.example.petvitals.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.petvitals.R
import com.example.petvitals.ui.theme.PetVitalsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberTopBarScrollBehavior(): TopAppBarScrollBehavior =
    TopAppBarDefaults.pinnedScrollBehavior()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetTopBarWhenNotScrollable(
    scrollBehavior: TopAppBarScrollBehavior,
    canScrollBackward: Boolean,
    canScrollForward: Boolean,
    contentVisible: Boolean = true
) {
    LaunchedEffect(contentVisible, canScrollBackward, canScrollForward) {
        if (!contentVisible || (!canScrollBackward && !canScrollForward)) {
            scrollBehavior.state.resetTopBarScroll()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
internal fun TopAppBarState.resetTopBarScroll() {
    heightOffset = 0f
    contentOffset = 0f
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenterAlignedTopBar(
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit),
    navigationIcon: @Composable (() -> Unit) = {},
    actions: @Composable (RowScope.() -> Unit) = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    CenterAlignedTopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        ),
        scrollBehavior = scrollBehavior
    )
}

@PreviewLightDark
@Composable
private fun CenterAlignedTopBarPreview() {
    PetVitalsTheme {
        Surface {
            CenterAlignedTopBar(
                title = { Text("Pets") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.ic_person),
                            contentDescription = "Profile"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add_circle),
                            contentDescription = "Add pet"
                        )
                    }
                }
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun CenterAlignedTopBarTitleOnlyPreview() {
    PetVitalsTheme {
        Surface {
            CenterAlignedTopBar(title = { Text("Profile") })
        }
    }
}
