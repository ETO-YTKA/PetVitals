package com.example.petvitals.ui.screens.managefood

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Food
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.repository.FoodRepository
import com.example.petvitals.domain.usecase.GetPetPermissionUseCase
import com.example.petvitals.domain.validator.FoodDataValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ManageFoodViewModelTest {

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
    fun saveInAddMode_usesRoutePetId() = runTest(dispatcher) {
        val repository = FakeFoodRepository()
        val viewModel = createViewModel(repository)

        viewModel.loadInitialData(PET_ID, foodId = null)
        enterValidFood(viewModel)
        viewModel.onAction(ManageFoodAction.OnSave {})
        advanceUntilIdle()

        assertEquals(PET_ID, repository.savedFood?.petId)
        assertTrue(repository.savedFood?.id?.isNotBlank() == true)
    }

    @Test
    fun saveInEditMode_preservesFoodId() = runTest(dispatcher) {
        val repository = FakeFoodRepository(
            loadedFood = Food(
                id = FOOD_ID,
                petId = PET_ID,
                name = "Dry food",
                portion = "100 g",
                frequency = "Twice daily"
            )
        )
        val viewModel = createViewModel(repository)

        viewModel.loadInitialData(PET_ID, FOOD_ID)
        advanceUntilIdle()
        viewModel.onAction(ManageFoodAction.OnNameChange("Updated food"))
        viewModel.onAction(ManageFoodAction.OnSave {})
        advanceUntilIdle()

        assertEquals(FOOD_ID, repository.savedFood?.id)
        assertEquals("Updated food", repository.savedFood?.name)
    }

    private fun createViewModel(repository: FoodRepository) = ManageFoodViewModel(
        foodRepository = repository,
        getPetPermission = FakeGetPetPermissionUseCase(PermissionLevel.EDITOR),
        foodValidator = FoodDataValidator()
    )

    private fun enterValidFood(viewModel: ManageFoodViewModel) {
        viewModel.onAction(ManageFoodAction.OnNameChange("Dry food"))
        viewModel.onAction(ManageFoodAction.OnPortionChange("100 g"))
        viewModel.onAction(ManageFoodAction.OnFrequencyChange("Twice daily"))
    }

    private class FakeGetPetPermissionUseCase(
        private val permissionLevel: PermissionLevel
    ) : GetPetPermissionUseCase {
        override suspend fun invoke(
            petId: String
        ): AppResult<FirestoreError, PermissionLevel> = AppResult.Success(permissionLevel)
    }

    private class FakeFoodRepository(
        private val loadedFood: Food? = null
    ) : FoodRepository {
        var savedFood: Food? = null

        override suspend fun getAllFood(
            petId: String
        ): AppResult<FirestoreError, List<Food>> = AppResult.Success(emptyList())

        override suspend fun getFoodById(petId: String, foodId: String): Food? = loadedFood

        override suspend fun saveFood(food: Food) {
            savedFood = food
        }

        override suspend fun deleteFood(
            food: Food
        ): AppResult<FirestoreError, Unit> = AppResult.Success(Unit)
    }

    private companion object {
        const val PET_ID = "pet-id"
        const val FOOD_ID = "food-id"
    }
}
