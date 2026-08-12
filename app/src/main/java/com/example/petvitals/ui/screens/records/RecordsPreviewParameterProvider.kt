package com.example.petvitals.ui.screens.records

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.petvitals.R
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.models.PetSpecies
import com.example.petvitals.domain.models.Record
import com.example.petvitals.domain.models.RecordOverview
import com.example.petvitals.domain.models.RecordType
import java.util.Date

class RecordsPreviewParameterProvider : PreviewParameterProvider<RecordsUiState> {
    override val values: Sequence<RecordsUiState> = sequenceOf(
        populatedPreviewState(),
        populatedPreviewState(expanded = true),
        RecordsUiState(isInitialLoading = false),
        RecordsUiState(isInitialLoading = true),
        RecordsUiState(
            isInitialLoading = false,
            errorMessageRes = R.string.something_went_wrong_error
        ),
        populatedPreviewState().copy(searchQuery = "dental")
    )
}

private val previewPet = Pet(
    id = "pet-1",
    name = "Mochi",
    species = PetSpecies.CAT
)

private val previewDog = Pet(
    id = "pet-2",
    name = "Pepper",
    species = PetSpecies.DOG
)

private val previewRecord = Record(
    id = "record-1",
    title = "Annual vaccination",
    type = RecordType.VACCINATION,
    eventDate = Date(1_775_565_600_000L),
    description = "Routine booster completed. No adverse reaction observed.",
    petIds = listOf(previewPet.id)
)

private val previewRecordItems = listOf(
    RecordOverview(
        record = previewRecord,
        pets = listOf(previewPet),
        canManage = true
    ),
    RecordOverview(
        record = Record(
            id = "record-2",
            title = "Reduced appetite",
            type = RecordType.SYMPTOM,
            eventDate = Date(1_775_558_400_000L),
            description = "Ate about half of the usual breakfast portion.",
            petIds = listOf(previewDog.id)
        ),
        pets = listOf(previewDog),
        canManage = true
    ),
    RecordOverview(
        record = Record(
            id = "record-3",
            title = "Grooming appointment",
            type = RecordType.GROOMING,
            eventDate = Date(1_775_479_400_000L),
            description = "Coat trim and nail care completed.",
            petIds = listOf(previewPet.id, previewDog.id)
        ),
        pets = listOf(previewPet, previewDog),
        canManage = false
    )
)

private fun populatedPreviewState(
    expanded: Boolean = false
) = RecordsUiState(
    isInitialLoading = false,
    expandedRecordIds = if (expanded) setOf(previewRecord.id) else emptySet(),
    records = previewRecordItems
)
