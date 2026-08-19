package com.example.petvitals.data.utils

import com.example.petvitals.domain.models.Gender
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.models.PetSpecies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PetMapperTest {

    @Test
    fun toUpdateMap_containsOnlyProfileFields() {
        val pet = Pet(
            id = "pet-id",
            name = "Milo",
            species = PetSpecies.DOG,
            breed = "Beagle",
            gender = Gender.MALE,
            dobYear = 2020,
            dobMonth = 4,
            dobDay = 12,
            avatar = "avatar",
            healthNote = "Keep this note",
            foodNote = "Keep this note too"
        )

        val updates = pet.toUpdateMap()

        assertEquals(
            setOf(
                "name",
                "species",
                "breed",
                "gender",
                "dobYear",
                "dobMonth",
                "dobDay",
                "avatar"
            ),
            updates.keys
        )
        assertFalse(updates.containsKey("id"))
        assertFalse(updates.containsKey("healthNote"))
        assertFalse(updates.containsKey("foodNote"))
        assertEquals("Milo", updates["name"])
        assertEquals(PetSpecies.DOG, updates["species"])
    }
}
