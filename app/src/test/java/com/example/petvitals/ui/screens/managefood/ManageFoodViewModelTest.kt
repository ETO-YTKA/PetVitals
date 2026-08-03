package com.example.petvitals.ui.screens.managefood

import android.content.ContextWrapper
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Food
import com.example.petvitals.domain.repository.FoodRepository
import com.example.petvitals.domain.usecase.SaveFoodUseCase
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
        val saveFoodUseCase = FakeSaveFoodUseCase()
        val viewModel = createViewModel(repository, saveFoodUseCase)

        viewModel.loadInitialData(PET_ID, foodId = null)
        enterValidFood(viewModel)
        viewModel.onAction(ManageFoodAction.OnSave {})
        advanceUntilIdle()

        assertEquals(PET_ID, saveFoodUseCase.savedFood?.petId)
        assertTrue(saveFoodUseCase.savedFood?.id?.isNotBlank() == true)
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
        val saveFoodUseCase = FakeSaveFoodUseCase()
        val viewModel = createViewModel(repository, saveFoodUseCase)

        viewModel.loadInitialData(PET_ID, FOOD_ID)
        advanceUntilIdle()
        viewModel.onAction(ManageFoodAction.OnNameChange("Updated food"))
        viewModel.onAction(ManageFoodAction.OnSave {})
        advanceUntilIdle()

        assertEquals(FOOD_ID, saveFoodUseCase.savedFood?.id)
        assertEquals("Updated food", saveFoodUseCase.savedFood?.name)
    }

    private fun createViewModel(
        repository: FoodRepository,
        saveFoodUseCase: SaveFoodUseCase
    ) = ManageFoodViewModel(
        foodRepository = repository,
        saveFoodUseCase = saveFoodUseCase,
        foodValidator = FoodDataValidator(),
        context = ContextWrapper(null)
    )

    private fun enterValidFood(viewModel: ManageFoodViewModel) {
        viewModel.onAction(ManageFoodAction.OnNameChange("Dry food"))
        viewModel.onAction(ManageFoodAction.OnPortionChange("100 g"))
        viewModel.onAction(ManageFoodAction.OnFrequencyChange("Twice daily"))
    }

    private class FakeFoodRepository(
        private val loadedFood: Food? = null
    ) : FoodRepository {
        override suspend fun getAllFood(
            petId: String
        ): AppResult<FirestoreError, List<Food>> = AppResult.Success(emptyList())

        override suspend fun getFoodById(petId: String, foodId: String): Food? = loadedFood

        override suspend fun saveFood(
            food: Food
        ): AppResult<FirestoreError, Unit> = AppResult.Success(Unit)

        override suspend fun deleteFood(
            food: Food
        ): AppResult<FirestoreError, Unit> = AppResult.Success(Unit)
    }

    private class FakeSaveFoodUseCase : SaveFoodUseCase {
        var savedFood: Food? = null

        override suspend fun invoke(food: Food): AppResult<FirestoreError, Unit> {
            savedFood = food
            return AppResult.Success(Unit)
        }
    }

    private companion object {
        const val PET_ID = "pet-id"
        const val FOOD_ID = "food-id"
    }
}
