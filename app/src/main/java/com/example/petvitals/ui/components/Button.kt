package com.example.petvitals.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petvitals.ui.theme.PetVitalsTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomMediumButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    content: @Composable () -> Unit = {}
) {
    Button(
        onClick = onClick,
        shapes = ButtonShapes(
            shape = CircleShape,
            pressedShape = RoundedCornerShape(12.dp)
        ),
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = ButtonDefaults.MediumContentPadding
    ) {
        content()
    }
}

@PreviewLightDark
@Composable
private fun CustomTextFieldPreview() {
    PetVitalsTheme {
        Surface {
            CustomMediumButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                content = {
                    Text(
                        text = "Button",
                        fontSize = 16.sp
                    )
                }
            )
        }
    }
}
