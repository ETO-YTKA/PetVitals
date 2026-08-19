package com.example.petvitals.ui.screens.managepet

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.models.PetSpecies
import com.example.petvitals.domain.repository.PetRepository
import com.example.petvitals.domain.usecase.CreatePetUseCase
import com.example.petvitals.domain.validator.PetDataValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ManagePetViewModelEditTest {

    @Test
    fun saveExistingPet_preservesIdAndUsesProfileUpdate() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repository = FakePetRepository()
            val createPet = FakeCreatePetUseCase()
            val viewModel = ManagePetViewModel(
                petRepository = repository,
                createPetUseCase = createPet,
                petDataValidator = PetDataValidator(),
                context = InstrumentationRegistry.getInstrumentation().targetContext
            )
            viewModel.loadPetData(PET_ID)
            advanceUntilIdle()
            var succeeded = false

            viewModel.onAction(
                ManagePetAction.SavePet(PET_ID) {
                    succeeded = true
                }
            )
            advanceUntilIdle()

            assertEquals(PET_ID, repository.updatedPet?.id)
            assertEquals(0, createPet.calls)
            assertTrue(succeeded)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class FakeCreatePetUseCase : CreatePetUseCase {
        var calls = 0

        override suspend fun invoke(pet: Pet): AppResult<FirestoreError, Unit> {
            calls++
            return AppResult.Success(Unit)
        }
    }

    private class FakePetRepository : PetRepository {
        var updatedPet: Pet? = null

        override suspend fun savePet(pet: Pet) = AppResult.Success(Unit)

        override suspend fun updatePet(pet: Pet): AppResult<FirestoreError, Unit> {
            updatedPet = pet
            return AppResult.Success(Unit)
        }

        override suspend fun getPetById(petId: String) = AppResult.Success(
            Pet(
                id = petId,
                name = "Milo",
                species = PetSpecies.CAT
            )
        )

        override suspend fun getCurrentUserPets() = AppResult.Success(emptyList<Pet>())

        override suspend fun deletePet(petId: String) = AppResult.Success(Unit)

        override suspend fun createPetWithOwner(pet: Pet, member: Member) =
            AppResult.Success(Unit)
    }

    private companion object {
        const val PET_ID = "pet-id"
    }
}
