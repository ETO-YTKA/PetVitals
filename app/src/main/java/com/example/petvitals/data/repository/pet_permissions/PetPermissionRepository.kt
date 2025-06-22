package com.example.petvitals.data.repository.pet_permissions

interface PetPermissionRepository {
    suspend fun getCurrentUserPets(): List<PetPermissions>
    suspend fun getCurrentUserPetById(petId: String): PetPermissions?
    suspend fun getUsersByPetId(petId: String): List<PetPermissions>
    suspend fun getCurrentUserPermissionLevel(petId: String): PermissionLevel
    suspend fun savePetPermission(petPermissions: PetPermissions)
    suspend fun deletePetPermissionByUserPetIds(petId: String, userId: String)
}