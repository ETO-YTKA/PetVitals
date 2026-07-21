package com.example.petvitals.ui.screens.petprofile

import com.example.petvitals.domain.models.Food
import com.example.petvitals.domain.models.Medication
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.Pet

data class PetProfileUiState(
    val isLoading: Boolean = false,
    val loadErrorMessageRes: Int? = null,

    val pet: Pet = Pet(),
    val dob: String? = null,
    val age: String? = null,
    val medications: List<Medication> = emptyList(),
    val food: List<Food> = emptyList(),
    val noteEditor: NoteEditorState = NoteEditorState(),
    val permissionLevel: PermissionLevel = PermissionLevel.VIEWER,

    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,
    val showOnDeleteModal: Boolean = false,
    val showShareModal: Boolean = false
)

enum class PetNoteType {
    HEALTH,
    FOOD
}

data class NoteEditorState(
    val noteType: PetNoteType? = null,
    val draft: String = "",
    val original: String = "",
    val isSaving: Boolean = false,
    val errorMessageRes: Int? = null
) {
    val normalizedValue: String?
        get() = draft.trim().takeIf(String::isNotEmpty)

    val canSave: Boolean
        get() = noteType != null &&
                draft.trim() != original.trim() &&
                !isSaving

    fun open(noteType: PetNoteType, content: String?): NoteEditorState {
        if (this.noteType != null) return this

        val value = content.orEmpty()
        return NoteEditorState(
            noteType = noteType,
            draft = value,
            original = value
        )
    }

    fun updateDraft(value: String): NoteEditorState {
        return copy(
            draft = value,
            errorMessageRes = null
        )
    }

    fun beginSaving(): NoteEditorState {
        return copy(
            isSaving = true,
            errorMessageRes = null
        )
    }

    fun saveFailed(errorMessageRes: Int): NoteEditorState {
        return copy(
            isSaving = false,
            errorMessageRes = errorMessageRes
        )
    }

    fun cancel(): NoteEditorState = NoteEditorState()
}
