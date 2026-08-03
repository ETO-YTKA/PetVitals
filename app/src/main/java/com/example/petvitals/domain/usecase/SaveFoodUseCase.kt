package com.example.petvitals.domain.usecase

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Food

interface SaveFoodUseCase {
    suspend operator fun invoke(food: Food): AppResult<FirestoreError, Unit>
}