package com.example.petvitals.ui.screens.pet_profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.PetProfile
import com.example.petvitals.ui.components.ScreenLayout

@Composable
fun PetProfileScreen(
    petProfile: PetProfile,
    viewModel: PetProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    viewModel.getPetData(petProfile.petId)

    ScreenLayout(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = uiState.name)
    }
}