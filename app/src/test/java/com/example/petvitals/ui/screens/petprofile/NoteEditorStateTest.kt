package com.example.petvitals.ui.screens.petprofile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteEditorStateTest {

    @Test
    fun open_withActiveEditor_keepsExistingDraft() {
        val state = NoteEditorState(
            noteType = PetNoteType.HEALTH,
            draft = "Old draft",
            original = "Old note",
            errorMessageRes = 42
        )

        val result = state.open(PetNoteType.FOOD, "Fresh food note")

        assertEquals(state, result)
    }

    @Test
    fun open_withoutActiveEditor_loadsPersistedContent() {
        val result = NoteEditorState().open(PetNoteType.FOOD, "Fresh food note")

        assertEquals(PetNoteType.FOOD, result.noteType)
        assertEquals("Fresh food note", result.draft)
        assertEquals("Fresh food note", result.original)
        assertFalse(result.isSaving)
        assertNull(result.errorMessageRes)
    }

    @Test
    fun cancel_clearsEditorAndDiscardsDraft() {
        val state = NoteEditorState(
            noteType = PetNoteType.HEALTH,
            draft = "Unsaved note",
            original = "Saved note"
        )

        assertEquals(NoteEditorState(), state.cancel())
    }

    @Test
    fun canSave_isTrueOnlyForChangedDraftWhileNotSaving() {
        val unchanged = NoteEditorState().open(PetNoteType.HEALTH, "Saved note")
        val changed = unchanged.updateDraft("Changed note")

        assertFalse(unchanged.canSave)
        assertTrue(changed.canSave)
        assertFalse(changed.beginSaving().canSave)
    }

    @Test
    fun normalizedValue_trimsContentAndReturnsNullForBlankDraft() {
        val content = NoteEditorState().open(PetNoteType.FOOD, null)
            .updateDraft("  Fresh water only  ")
        val blank = content.updateDraft("   ")

        assertEquals("Fresh water only", content.normalizedValue)
        assertNull(blank.normalizedValue)
    }

    @Test
    fun saveFailure_stopsSavingAndKeepsChangedDraft() {
        val state = NoteEditorState().open(PetNoteType.HEALTH, "Saved note")
            .updateDraft("Changed note")
            .beginSaving()

        val result = state.saveFailed(errorMessageRes = 42)

        assertFalse(result.isSaving)
        assertEquals("Changed note", result.draft)
        assertEquals(42, result.errorMessageRes)
        assertTrue(result.canSave)
    }
}
