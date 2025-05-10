package com.example.petvitals.ui.screens.pet_profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.PetProfile
import com.example.petvitals.R
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.theme.Dimen

@Composable
fun PetProfileScreen(
    petProfile: PetProfile,
    viewModel: PetProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val birthDate = when {
        uiState.birthDay.isNotEmpty() && uiState.birthMonth.isNotEmpty() -> {
            "${uiState.birthDay}.${uiState.birthMonth}.${uiState.birthYear}"
        }
        uiState.birthMonth.isNotEmpty() -> {
            "${uiState.birthMonth}.${uiState.birthYear}"
        }
        else -> {
            uiState.birthYear
        }
    }
    Log.d("PetProfileScreen", "BirthDate: $birthDate")

    LaunchedEffect(Unit) {
        viewModel.getPetData(petProfile.petId)
    }

    ScreenLayout(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.splash),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(Dimen.petImageProfile)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(Dimen.spaceMedium))
            Text(text = uiState.name, style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(Dimen.spaceHuge))
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(Dimen.spaceMedium),
                verticalArrangement = Arrangement.spacedBy(Dimen.spaceMedium)
            ) {
                Text(text = uiState.species)
                Text(text = birthDate)
            }
        }
    }
}