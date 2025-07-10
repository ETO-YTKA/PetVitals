package com.example.petvitals.data.repository.food

interface FoodRepository {
    suspend fun getAllFood(petId: String): List<Food>
    suspend fun getFoodById(petId: String, foodId: String): Food?
    suspend fun saveFood(food: Food)
    suspend fun deleteFood(food: Food)
}