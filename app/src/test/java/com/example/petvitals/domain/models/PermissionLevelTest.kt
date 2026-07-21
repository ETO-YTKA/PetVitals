package com.example.petvitals.domain.models

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionLevelTest {

    @Test
    fun owner_canManageCareAndDeletePet() {
        assertTrue(PermissionLevel.OWNER.canManagePetCare)
        assertTrue(PermissionLevel.OWNER.canDeletePet)
    }

    @Test
    fun editor_canManageCareButCannotDeletePet() {
        assertTrue(PermissionLevel.EDITOR.canManagePetCare)
        assertFalse(PermissionLevel.EDITOR.canDeletePet)
    }

    @Test
    fun viewer_cannotManageCareOrDeletePet() {
        assertFalse(PermissionLevel.VIEWER.canManagePetCare)
        assertFalse(PermissionLevel.VIEWER.canDeletePet)
    }
}
