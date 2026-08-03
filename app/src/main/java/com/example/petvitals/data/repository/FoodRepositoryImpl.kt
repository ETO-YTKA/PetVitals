package com.example.petvitals.data.repository

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
    private val firestore: FirebaseFirestore
): FoodRepository {

    override suspend fun getAllFood(petId: String): AppResult<FirestoreError, List<Food>> {

        return safeFirestoreCall {
            firestore
                .collection(FirestoreCollections.PETS)
                .document(petId)
                .collection(FirestoreCollections.FOOD)
                .get()
                .await()
                .map { it.toObject<Food>() }
        }
    }

    override suspend fun getFoodById(
        petId: String,
        foodId: String
    ): Food? {

        return firestore
            .collection(FirestoreCollections.PETS)
            .document(petId)
            .collection(FirestoreCollections.FOOD)
            .document(foodId)
            .get()
            .await()
            .toObject<Food>()
    }

    override suspend fun saveFood(food: Food): AppResult<FirestoreError, Unit> {

        return safeFirestoreCall {
            firestore
                .collection(FirestoreCollections.PETS)
                .document(food.petId)
                .collection(FirestoreCollections.FOOD)
                .document(food.id)
                .set(food)
                .await()
        }
    }

    override suspend fun deleteFood(food: Food): AppResult<FirestoreError, Unit> {

        return safeFirestoreCall {
            firestore
                .collection(FirestoreCollections.PETS)
                .document(food.petId)
                .collection(FirestoreCollections.FOOD)
                .document(food.id)
                .delete()
                .await()
        }
    }
}