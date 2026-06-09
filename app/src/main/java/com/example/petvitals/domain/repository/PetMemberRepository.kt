package com.example.petvitals.domain.repository

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PermissionLevel

interface PetMemberRepository {
    suspend fun getPetMembers(petId: String): AppResult<FirestoreError, List<Member>>
    suspend fun getPetRole(petId: String, userId: String): AppResult<FirestoreError, PermissionLevel?>
    suspend fun savePetMember(petId: String, member: Member): AppResult<FirestoreError, Unit>
    suspend fun deletePetMember(petId: String, userId: String): AppResult<FirestoreError, Unit>
}