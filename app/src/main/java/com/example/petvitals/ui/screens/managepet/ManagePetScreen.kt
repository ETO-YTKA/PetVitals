package com.example.petvitals.ui.screens.managepet

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.petvitals.R
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.CustomMediumButton
import com.example.petvitals.ui.components.CustomTextField
import com.example.petvitals.ui.components.Loading
import com.example.petvitals.ui.components.PopUpHost
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.components.ValueDropDown
import com.example.petvitals.ui.navigation.AddEditPet
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.ui.theme.PetVitalsTheme

@Composable
fun ManagePetScreen(
    addEditPet: AddEditPet,
    onNavigateToPets: () -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: ManagePetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        addEditPet.petId?.let { petId ->
            viewModel.loadPetData(petId)
        }
    }

    ManagePetScreenContent(
        uiState = uiState,
        action = { action -> viewModel.onAction(action) },
        isNewPet = addEditPet.petId == null,
        onNavigateToPets = onNavigateToPets,
        onPopBackStack = onPopBackStack
    )
}

@Composable
private fun ManagePetScreenContent(
    uiState: ManagePetUiState,
    action: (ManagePetAction) -> Unit,
    isNewPet: Boolean,
    onNavigateToPets: () -> Unit,
    onPopBackStack: () -> Unit
) {
    if (uiState.isLoading) {
        Loading()
        return
    }

    PopUpHost(
        popUpState = uiState.popUpState,
        onAction = action,
        onDismiss = { action(ManagePetAction.DismissPopUp) }
    )

    Scaffold(
        topBar = {
            TopBar(
                title = {
                    Text(
                        stringResource(
                            if (isNewPet) R.string.add_pet
                            else R.string.edit_pet
                        )
                    )
                },
                navigationIcon = {
                    CustomIconButton(
                        onClick = onPopBackStack,
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back)
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimen.Screen.horizontalPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val imagePickerLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
                uri?.let { action(ManagePetAction.OnImageUriChange(it)) }
            }
            Spacer(modifier = Modifier.height(0.dp))

            PetImage(
                uiState = uiState,
                onPickPhoto = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                    )
                }
            )

            Text(
                text = stringResource(R.string.manage_pet_intro_helper),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 50.dp)
            )

            // Name
            CustomTextField(
                value = uiState.name,
                onValueChange = { action(ManagePetAction.OnNameChange(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.pets_name)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                isError = uiState.nameErrorMessage != null,
                supportingText = uiState.nameErrorMessage?.let { { Text(it) } },
                maxLines = 1
            )

            // Species
            ValueDropDown(
                value = uiState.selectedSpecies,
                onValueChange = { action(ManagePetAction.OnSpeciesChange(it)) },
                options = uiState.speciesOptions,
                label = stringResource(R.string.species),
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.speciesErrorMessage != null,
                supportingText = uiState.speciesErrorMessage
            )

            // Gender
            ValueDropDown(
                value = uiState.selectedGender,
                onValueChange = { action(ManagePetAction.OnGenderChange(it)) },
                options = uiState.genderOptions,
                label = stringResource(R.string.gender),
                modifier = Modifier.fillMaxWidth()
            )

            // Breed
            CustomTextField(
                value = uiState.breed,
                onValueChange = { action(ManagePetAction.OnBreedChange(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.breed_optional)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                isError = uiState.breedErrorMessage != null,
                supportingText = uiState.breedErrorMessage?.let { { Text(it) } },
                maxLines = 1
            )

            // DOB
            DobPartsPicker(
                onDobMonthChange = { action(ManagePetAction.OnDobMonthChange(it)) },
                onDobDayChange = { action(ManagePetAction.OnDobDayChange(it)) },
                onDobYearChange = { action(ManagePetAction.OnDobYearChange(it)) },
                uiState = uiState
            )

            CustomMediumButton(
                onClick = { action(ManagePetAction.SavePet(uiState.petId, onNavigateToPets)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.save),
                    fontSize = Dimen.FontSize.mediumButton
                )
            }
        }
    }
}

@Composable
private fun PetImage(
    uiState: ManagePetUiState,
    onPickPhoto: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val model = uiState.avatarUri ?: uiState.avatarByteArray

        if (model == null) {
            Surface(
                shape = RoundedCornerShape(100),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(100))
                    .clickable(onClick = onPickPhoto)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_a_photo),
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = stringResource(R.string.tap_to_select_photo),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            AsyncImage(
                model = model,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(100))
                    .clickable(onClick = onPickPhoto)
            )
        }
    }
}

@Composable
private fun DobPartsPicker(
    onDobMonthChange: (Int?) -> Unit,
    onDobDayChange: (String) -> Unit,
    onDobYearChange: (String) -> Unit,
    uiState: ManagePetUiState,
) {
    val hasDobError = uiState.dobErrorMessage != null

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.date_of_birth),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(R.string.date_of_birth_optional_helper),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomTextField(
                value = uiState.dobDay,
                onValueChange = onDobDayChange,
                label = { Text(stringResource(R.string.day)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.weight(0.75f),
                isError = hasDobError,
                maxLines = 1
            )

            ValueDropDown(
                value = uiState.selectedDobMonth,
                onValueChange = onDobMonthChange,
                options = uiState.monthOptions,
                label = stringResource(R.string.month),
                modifier = Modifier.weight(1.15f),
                isError = hasDobError,
            )

            CustomTextField(
                value = uiState.dobYear,
                onValueChange = onDobYearChange,
                label = { Text(stringResource(R.string.year)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.weight(1f),
                isError = hasDobError,
                maxLines = 1
            )
        }

        uiState.dobErrorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 4.dp,
                    end = 16.dp
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ManagePetScreenContentNotApproxDatePreview() {
    PetVitalsTheme {
        ManagePetScreenContent(
            uiState = ManagePetUiState(),
            action = {},
            isNewPet = false,
            onNavigateToPets = {},
            onPopBackStack = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ManagePetScreenContentWithDobPartsPreview() {
    PetVitalsTheme {
        ManagePetScreenContent(
            uiState = ManagePetUiState(
                selectedDobMonth = 5,
                dobDay = "12",
                dobYear = "2020"
            ),
            action = {},
            isNewPet = false,
            onNavigateToPets = {},
            onPopBackStack = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ManagePetScreenContentFieldErrorsPreview() {
    PetVitalsTheme {
        ManagePetScreenContent(
            uiState = ManagePetUiState(
                nameErrorMessage = "Name is required",
                dobErrorMessage = "Date of birth is required",
                breedErrorMessage = "Breed is required",
                speciesErrorMessage = "Species is required"
            ),
            action = {},
            isNewPet = false,
            onNavigateToPets = {},
            onPopBackStack = {},
        )
    }
}
