package com.example.petvitals.data.repository.pet_permissions

import com.example.petvitals.data.service.account.AccountService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class PetPermissionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val accountService: AccountService
): PetPermissionRepository {
    override suspend fun getCurrentUserPets(): List<PetPermissions> {

        val userId = accountService.currentUserId
        return firestore
            .collection("petPermission")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .map { it.toObject<PetPermissions>() }
    }

    override suspend fun getCurrentUserPetById(petId: String): PetPermissions? {

        val userId = accountService.currentUserId
        return firestore
            .collection("petPermission")
            .whereEqualTo("userId", userId)
            .whereEqualTo("petId", petId)
            .get()
            .await()
            .toObjects<PetPermissions>()
            .firstOrNull()
    }

    override suspend fun getUsersByPetId(petId: String): List<PetPermissions> {

        return firestore
            .collection("petPermission")
            .whereEqualTo("petId", petId)
            .get()
            .await()
            .map { it.toObject<PetPermissions>() }
    }

    override suspend fun getCurrentUserPermissionLevel(petId: String): PermissionLevel {

        val userId = accountService.currentUserId
        return firestore
                .collection("petPermission")
                .whereEqualTo("petId", petId)
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .toObjects<PetPermissions>()
                .first().permissionLevel
    }

    override suspend fun savePetPermission(petPermissions: PetPermissions) {

        firestore
            .collection("petPermission").document(petPermissions.id)
            .set(petPermissions)
            .await()
    }

    override suspend fun deletePetPermissionByUserPetIds(petId: String, userId: String) {

        firestore
            .collection("petPermission")
            .whereEqualTo("petId", petId)
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .forEach { it.reference.delete() }
    }
}