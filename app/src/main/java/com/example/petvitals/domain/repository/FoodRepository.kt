package com.example.petvitals.domain.repository

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Food

interface FoodRepository {
    suspend fun getAllFood(petId: String): AppResult<FirestoreError, List<Food>>
    suspend fun getFoodById(petId: String, foodId: String): Food?
    suspend fun saveFood(food: Food)
    suspend fun deleteFood(food: Food): AppResult<FirestoreError, Unit>
}
