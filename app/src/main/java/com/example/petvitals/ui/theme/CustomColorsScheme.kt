package com.example.petvitals.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CustomColorsScheme (
    val successContainer: Color = Color.Unspecified,
    val onSuccessContainer: Color = Color.Unspecified,
    val infoContainer: Color = Color.Unspecified,
    val onInfoContainer: Color = Color.Unspecified,
    val warningContainer: Color = Color.Unspecified,
    val onWarningContainer: Color = Color.Unspecified
)

val LightCustomColorsScheme = CustomColorsScheme(
    successContainer = successContainerLight,
    onSuccessContainer = onSuccessContainerLight,
    infoContainer = infoContainerLight,
    onInfoContainer = onInfoContainerLight,
    warningContainer = warningContainerLight,
    onWarningContainer = onWarningContainerLight
)

val DarkCustomColorsScheme = CustomColorsScheme(
    successContainer = successContainerDark,
    onSuccessContainer = onSuccessContainerDark,
    infoContainer = infoContainerDark,
    onInfoContainer = onInfoContainerDark,
    warningContainer = warningContainerDark,
    onWarningContainer = onWarningContainerDark
)

val LocalCustomColorsScheme = staticCompositionLocalOf { CustomColorsScheme() }