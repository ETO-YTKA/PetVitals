package com.example.petvitals.ui.screens.pet_profile

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.petvitals.PetProfile
import com.example.petvitals.R
import com.example.petvitals.data.repository.pet.Gender
import com.example.petvitals.data.repository.pet.PetSpecies
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.utils.decodeBase64ToImage

@Composable
fun PetProfileScreen(
    petProfile: PetProfile,
    onPopBackStack: () -> Unit,
    onNavigateToEditPet: (String) -> Unit,
    viewModel: PetProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getPetData(petProfile.petId)
    }

    ScreenLayout(
        modifier = Modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(Dimen.spaceMedium),
        topBar = {
            TopBar(
                title = { Text(stringResource(R.string.pet_profile)) },
                navigationIcon = {
                    CustomIconButton(
                        onClick = onPopBackStack,
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back)
                    )
                },
                actions = {
                    CustomIconButton(
                        onClick = { onNavigateToEditPet(petProfile.petId) },
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = stringResource(R.string.edit_pet)
                    )
                    CustomIconButton(
                        onClick = {
                            viewModel.deletePet(petProfile.petId)
                            onPopBackStack()
                        },
                        painter = painterResource(R.drawable.ic_delete_forever),
                        contentDescription = stringResource(R.string.delete_pet)
                    )
                }
            )
        }
    ) {
        Spacer(modifier = Modifier.height(0.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimen.spaceSmall)
        ) {
            val painterRes = if (uiState.pet.species == PetSpecies.CAT) R.drawable.ic_cat
                else R.drawable.ic_dog
            val image = uiState.pet.imageString?.let { remember { decodeBase64ToImage(it) } }
            val imageModifier = Modifier
                .size(Dimen.petImageProfile)
                .then(if (image != null) Modifier.clip(CircleShape) else Modifier)

            AsyncImage(
                model = image,
                contentDescription = stringResource(R.string.pet_image),
                contentScale = ContentScale.Crop,
                fallback = painterResource(painterRes),
                modifier = imageModifier
            )

            Row {
                Text(
                    text = uiState.pet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                uiState.pet.gender.let { gender ->
                    @DrawableRes val painterRes = when (gender) {
                        Gender.MALE -> R.drawable.ic_male
                        Gender.FEMALE -> R.drawable.ic_female
                    }
                    @StringRes val contentDescription = when (gender) {
                        Gender.MALE -> R.string.male
                        Gender.FEMALE -> R.string.female
                    }
                    Icon(
                        painter = painterResource(painterRes),
                        contentDescription = stringResource(contentDescription),
                    )
                }
            }

            Text(
                text = uiState.pet.breed,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(0.7f)
            )

            Text(
                text = uiState.age,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.alpha(0.7f)
            )
        }

        SectionCard(
            title = "Health",
            icon = painterResource(R.drawable.ic_health_and_safety),
        ) {
            CardItem(
                title = stringResource(R.string.date_of_birth),
                information = uiState.dob
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Dimen.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimen.spaceSmall)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimen.spaceSmall)
            ) {
                icon?.let {
                    Icon(
                        painter = it,
                        contentDescription = null,
                    )
                }
                Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }

            content()
        }
    }
}

@Composable
private fun CardItem(
    title: String,
    information: String,
    modifier: Modifier = Modifier,
    infoIcon: Painter? = null,
) {
    Column(modifier.padding(Dimen.spaceSmall)) {

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.alpha(0.7f)
        )

        Spacer(modifier = Modifier.width(Dimen.spaceSmall))

        Row {
            infoIcon?.let {
                Icon(
                    painter = it,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(Dimen.spaceSmall))
            }
            Text(text = information)
        }
    }
}