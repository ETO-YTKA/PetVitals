package com.example.petvitals.domain.repository

import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.PetPermission

interface PetPermissionRepository {
    suspend fun getCurrentUserPets(): List<PetPermission>
    suspend fun getCurrentUserPetById(petId: String): PetPermission?
    suspend fun getUsersByPetId(petId: String): List<PetPermission>
    suspend fun getCurrentUserPermissionLevel(petId: String): PermissionLevel?
    suspend fun savePetPermission(petPermission: PetPermission)
    suspend fun deletePetPermissionByUserPetIds(petId: String, userId: String)
}