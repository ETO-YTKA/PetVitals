package com.example.petvitals.ui.screens

import android.content.ContextWrapper
import com.example.petvitals.R
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Food
import com.example.petvitals.domain.models.Medication
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.repository.FoodRepository
import com.example.petvitals.domain.repository.MedicationRepository
import com.example.petvitals.domain.usecase.GetPetPermissionUseCase
import com.example.petvitals.domain.validator.FoodDataValidator
import com.example.petvitals.ui.navigation.AddEditMedication
import com.example.petvitals.ui.screens.managefood.ManageFoodAction
import com.example.petvitals.ui.screens.managefood.ManageFoodViewModel
import com.example.petvitals.ui.screens.managemedication.AddEditMedicationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CareEditorPermissionTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun medicationEditor_asViewer_hidesFormAndRejectsSave() = runTest(dispatcher) {
        val repository = FakeMedicationRepository()
        val viewModel = AddEditMedicationViewModel(
            medicationRepository = repository,
            getPetPermission = FakeGetPetPermissionUseCase(PermissionLevel.VIEWER),
            context = ContextWrapper(null)
        )

        viewModel.loadInitialData(AddEditMedication(petId = PET_ID))
        advanceUntilIdle()
        viewModel.onSaveClick {}
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasEditPermission)
        assertEquals(
            R.string.pet_care_edit_access_denied,
            viewModel.uiState.value.permissionErrorMessageRes
        )
        assertEquals(0, repository.saveCalls)
    }

    @Test
    fun foodEditor_asViewer_rejectsSave() = runTest(dispatcher) {
        val repository = FakeFoodRepository()
        val permissionUseCase = FakeGetPetPermissionUseCase(PermissionLevel.VIEWER)
        val viewModel = ManageFoodViewModel(
            foodRepository = repository,
            getPetPermission = permissionUseCase,
            foodValidator = FoodDataValidator()
        )

        viewModel.loadInitialData(petId = PET_ID, foodId = null)
        advanceUntilIdle()
        assertEquals(0, permissionUseCase.calls)

        viewModel.onAction(ManageFoodAction.OnNameChange("Dry food"))
        viewModel.onAction(ManageFoodAction.OnPortionChange("100 g"))
        viewModel.onAction(ManageFoodAction.OnFrequencyChange("Twice daily"))
        viewModel.onAction(ManageFoodAction.OnSave {})
        advanceUntilIdle()

        assertEquals(1, permissionUseCase.calls)
        assertEquals(0, repository.saveCalls)
    }

    private class FakeGetPetPermissionUseCase(
        private val permissionLevel: PermissionLevel
    ) : GetPetPermissionUseCase {
        var calls = 0

        override suspend fun invoke(petId: String): AppResult<FirestoreError, PermissionLevel> =
            AppResult.Success(permissionLevel).also { calls++ }
    }

    private class FakeMedicationRepository : MedicationRepository {
        var saveCalls = 0

        override suspend fun getMedications(
            petId: String
        ): AppResult<FirestoreError, List<Medication>> = AppResult.Success(emptyList())
        override suspend fun saveMedication(medication: Medication) {
            saveCalls++
        }
        override suspend fun deleteMedication(
            medication: Medication
        ): AppResult<FirestoreError, Unit> = AppResult.Success(Unit)
        override suspend fun getMedicationById(medicationId: String, petId: String): Medication? = null
    }

    private class FakeFoodRepository : FoodRepository {
        var saveCalls = 0

        override suspend fun getAllFood(
            petId: String
        ): AppResult<FirestoreError, List<Food>> = AppResult.Success(emptyList())
        override suspend fun getFoodById(petId: String, foodId: String): Food? = null
        override suspend fun saveFood(food: Food) {
            saveCalls++
        }
        override suspend fun deleteFood(food: Food): AppResult<FirestoreError, Unit> =
            AppResult.Success(Unit)
    }

    private companion object {
        const val PET_ID = "pet-id"
    }
}
