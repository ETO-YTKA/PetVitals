package com.example.petvitals.data.repository

import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.data.utils.safeFirestoreCall
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Food
import com.example.petvitals.domain.repository.FoodRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class FoodRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val accountService: AccountService
): FoodRepository {
    override suspend fun getAllFood(
        petId: String
    ): AppResult<FirestoreError, List<Food>> = safeFirestoreCall {
        firestore
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

    override suspend fun deleteFood(
        food: Food
    ): AppResult<FirestoreError, Unit> = safeFirestoreCall<Unit> {
        firestore
            .collection("pets").document(food.petId)
            .collection("food").document(food.id)
            .delete()
            .await()
    }
}
