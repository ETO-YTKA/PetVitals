package com.example.petvitals.data.repository.food

interface FoodRepository {
    suspend fun getFood(petId: String): List<Food>
    suspend fun addFood(food: Food)
    suspend fun updateFood(food: Food)
    suspend fun deleteFood(food: Food)
}