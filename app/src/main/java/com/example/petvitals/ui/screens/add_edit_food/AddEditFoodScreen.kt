package com.example.petvitals.ui.screens.add_edit_food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.AddEditFood
import com.example.petvitals.R
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.theme.Dimen

@Composable
fun AddEditFoodScreen(
    addEditFood: AddEditFood,
    onPopBackStack: () -> Unit,
    viewModel: AddEditFoodViewModel = hiltViewModel()
) {

    ScreenLayout(
        verticalArrangement = Arrangement.spacedBy(Dimen.spaceLarge),
        topBar = {
            val title = when (addEditFood.foodId) {
                null -> stringResource(R.string.add_food)
                else -> stringResource(R.string.edit_food)
            }

            TopBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(
                        onClick = onPopBackStack
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) {

    }
}