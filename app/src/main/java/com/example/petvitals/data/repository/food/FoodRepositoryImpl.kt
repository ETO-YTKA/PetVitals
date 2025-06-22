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
    override suspend fun getFood(petId: String): List<Food> {

        accountService.currentUserId
        return firestore
            .collection("pets").document(petId)
            .collection("food")
            .get()
            .await()
            .map { it.toObject<Food>() }
    }

    override suspend fun addFood(food: Food) {

        accountService.currentUserId
        firestore
            .collection("pets").document(food.petId)
            .collection("food").document(food.id)
            .set(food)
            .await()
    }

    override suspend fun updateFood(food: Food) {

        accountService.currentUserId
        firestore
            .collection("pets").document(food.petId)
            .collection("food").document(food.id)
            .set(food)
            .await()
    }

    override suspend fun deleteFood(food: Food) {

        accountService.currentUserId
        firestore
            .collection("pets").document(food.petId)
            .collection("food").document(food.id)
            .delete()
            .await()
    }
}