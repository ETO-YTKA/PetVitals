package com.example.petvitals.data.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Food
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.repository.FoodRepository
import com.example.petvitals.domain.usecase.GetPetPermissionUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveFoodUseCaseImplTest {

    @Test
    fun invoke_asEditor_savesFoodAndReturnsSuccess() = runTest {
        val repository = FakeFoodRepository(AppResult.Success(Unit))
        val useCase = createUseCase(
            permissionResult = AppResult.Success(PermissionLevel.EDITOR),
            repository = repository
        )

        val result = useCase(FOOD)

        assertTrue(result is AppResult.Success)
        assertEquals(1, repository.saveCalls)
        assertEquals(FOOD, repository.savedFood)
    }

    @Test
    fun invoke_asViewer_returnsPermissionDeniedWithoutSaving() = runTest {
        val repository = FakeFoodRepository(AppResult.Success(Unit))
        val useCase = createUseCase(
            permissionResult = AppResult.Success(PermissionLevel.VIEWER),
            repository = repository
        )

        val result = useCase(FOOD)

        assertEquals(FirestoreError.PermissionDenied, (result as AppResult.Failure).error)
        assertEquals(0, repository.saveCalls)
    }

    @Test
    fun invoke_whenPermissionLookupFails_forwardsFailureWithoutSaving() = runTest {
        val repository = FakeFoodRepository(AppResult.Success(Unit))
        val useCase = createUseCase(
            permissionResult = AppResult.Failure(FirestoreError.Network),
            repository = repository
        )

        val result = useCase(FOOD)

        assertEquals(FirestoreError.Network, (result as AppResult.Failure).error)
        assertEquals(0, repository.saveCalls)
    }

    @Test
    fun invoke_whenRepositoryFails_forwardsFailure() = runTest {
        val repository = FakeFoodRepository(
            AppResult.Failure(FirestoreError.Unknown)
        )
        val useCase = createUseCase(
            permissionResult = AppResult.Success(PermissionLevel.OWNER),
            repository = repository
        )

        val result = useCase(FOOD)

        assertEquals(FirestoreError.Unknown, (result as AppResult.Failure).error)
        assertEquals(1, repository.saveCalls)
    }

    private fun createUseCase(
        permissionResult: AppResult<FirestoreError, PermissionLevel>,
        repository: FoodRepository
    ) = SaveFoodUseCaseImpl(
        foodRepository = repository,
        getPetPermissionUseCase = FakeGetPetPermissionUseCase(permissionResult)
    )

    private class FakeGetPetPermissionUseCase(
        private val result: AppResult<FirestoreError, PermissionLevel>
    ) : GetPetPermissionUseCase {
        override suspend fun invoke(
            petId: String
        ): AppResult<FirestoreError, PermissionLevel> = result
    }

    private class FakeFoodRepository(
        private val saveResult: AppResult<FirestoreError, Unit>
    ) : FoodRepository {
        var saveCalls = 0
        var savedFood: Food? = null

        override suspend fun getAllFood(
            petId: String
        ): AppResult<FirestoreError, List<Food>> = AppResult.Success(emptyList())

        override suspend fun getFoodById(petId: String, foodId: String): Food? = null

        override suspend fun saveFood(food: Food): AppResult<FirestoreError, Unit> {
            saveCalls++
            savedFood = food
            return saveResult
        }

        override suspend fun deleteFood(
            food: Food
        ): AppResult<FirestoreError, Unit> = AppResult.Success(Unit)
    }

    private companion object {
        val FOOD = Food(
            id = "food-id",
            petId = "pet-id",
            name = "Dry food",
            portion = "100 g",
            frequency = "Twice daily"
        )
    }
}
