package com.example.petvitals.ui.screens.pets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.theme.Dimen

@Composable
fun PetsScreen(
    modifier: Modifier = Modifier,
    restartApp: (Any) -> Unit,
    viewModel: PetsViewModel = hiltViewModel(),
    topAppBarTitle: (String) -> Unit,

) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.displayName) {
        topAppBarTitle("Hi, ${uiState.displayName}!")
    }
    LaunchedEffect(Unit) { viewModel.initialize(restartApp) }

    ScreenLayout(modifier = modifier) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = Dimen.petIconSize),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                PetProfile(
                    icon = painterResource(id = R.drawable.add_circle_24dp),
                    name = stringResource(R.string.add_pet)
                )
            }
            item {
                PetProfile(
                    icon = painterResource(id = R.drawable.add_circle_24dp),
                    name = stringResource(R.string.add_pet)
                )
            }
            item {
                PetProfile(
                    icon = painterResource(id = R.drawable.add_circle_24dp),
                    name = stringResource(R.string.add_pet)
                )
            }
            item {
                PetProfile(
                    icon = painterResource(id = R.drawable.add_circle_24dp),
                    name = stringResource(R.string.add_pet)
                )
            }
        }
    }
}

@Composable
fun PetProfile(icon: Painter, name: String) {
    Card(
        modifier = Modifier
            .padding(Dimen.spaceMedium)
            .clip(RoundedCornerShape(Dimen.petIconCornerRadiusPercent))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimen.spaceMedium),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(Dimen.petIconSize)
            )
            Text(text = name)
        }
    }
}