package com.example.petvitals.data.repository.pet_permissions

interface PetPermissionRepository {
    suspend fun getCurrentUserPets(): List<PetPermissions>
    suspend fun getCurrentUserPetById(petId: String): PetPermissions?
    suspend fun getUsersByPetId(petId: String): List<PetPermissions>
    suspend fun savePetPermission(petPermissions: PetPermissions)
}