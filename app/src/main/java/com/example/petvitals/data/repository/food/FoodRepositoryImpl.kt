package com.example.petvitals.data.repository.food

import com.example.petvitals.data.service.account.AccountService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class FoodRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val accountService: AccountService
): FoodRepository {
    override suspend fun getAllFood(petId: String): List<Food> {

        return firestore
            .collection("pets").document(petId)
            .collection("food")
            .get()
            .await()
            .map { it.toObject<Food>() }
    }

    override suspend fun getFoodById(
        petId: String,
        foodId: String
    ): Food? {

        return firestore
            .collection("pets").document(petId)
            .collection("food").document(foodId)
            .get()
            .await()
            .toObject<Food>()
    }

    override suspend fun saveFood(food: Food) {

        firestore
            .collection("pets").document(food.petId)
            .collection("food").document(food.id)
            .set(food)
            .await()
    }

    override suspend fun deleteFood(food: Food) {

        firestore
            .collection("pets").document(food.petId)
            .collection("food").document(food.id)
            .delete()
            .await()
    }
}