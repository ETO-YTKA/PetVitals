package com.example.petvitals.data.repository.pet_permission

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
    override suspend fun getCurrentUserPets(): List<PetPermission> {

        val userId = accountService.currentUserId
        return firestore
            .collection("petPermissions")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .map { it.toObject<PetPermission>() }
    }

    override suspend fun getCurrentUserPetById(petId: String): PetPermission? {

        val userId = accountService.currentUserId
        return firestore
            .collection("petPermissions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("petId", petId)
            .get()
            .await()
            .toObjects<PetPermission>()
            .firstOrNull()
    }

    override suspend fun getUsersByPetId(petId: String): List<PetPermission> {

        return firestore
            .collection("petPermissions")
            .whereEqualTo("petId", petId)
            .get()
            .await()
            .map { it.toObject<PetPermission>() }
    }

    override suspend fun getCurrentUserPermissionLevel(petId: String): PermissionLevel? {

        val userId = accountService.currentUserId
        return firestore
                .collection("petPermissions")
                .whereEqualTo("petId", petId)
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .toObjects<PetPermission>()
                .firstOrNull()?.permissionLevel
    }

    override suspend fun savePetPermission(petPermission: PetPermission) {

        firestore
            .collection("petPermissions").document(petPermission.id)
            .set(petPermission)
            .await()
    }

    override suspend fun deletePetPermissionByUserPetIds(petId: String, userId: String) {

        firestore
            .collection("petPermissions")
            .whereEqualTo("petId", petId)
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .forEach { it.reference.delete() }
    }
}