package com.example.petvitals.data.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Food
import com.example.petvitals.domain.models.canManagePetCare
import com.example.petvitals.domain.repository.FoodRepository
import com.example.petvitals.domain.usecase.GetPetPermissionUseCase
import com.example.petvitals.domain.usecase.SaveFoodUseCase
import jakarta.inject.Inject

class SaveFoodUseCaseImpl @Inject constructor(
    private val foodRepository: FoodRepository,
    private val getPetPermissionUseCase: GetPetPermissionUseCase
) : SaveFoodUseCase {

    override suspend fun invoke(food: Food): AppResult<FirestoreError, Unit> {

        return when (val permission = getPetPermissionUseCase.invoke(food.petId)) {
            is AppResult.Failure -> permission

            is AppResult.Success -> {
                if (!permission.data.canManagePetCare) {
                    AppResult.Failure(FirestoreError.PermissionDenied)
                } else {
                    foodRepository.saveFood(food)
                }
            }
        }
    }
}