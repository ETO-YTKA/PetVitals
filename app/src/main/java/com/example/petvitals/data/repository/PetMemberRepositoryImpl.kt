package com.example.petvitals.data.repository

import com.example.petvitals.data.utils.safeFirestoreCall
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.repository.PetMemberRepository
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class PetMemberRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PetMemberRepository {

    override suspend fun getPetMembers(petId: String): AppResult<FirestoreError, List<Member>> {
        TODO("Not yet implemented")
    }

    override suspend fun getPetRole(
        petId: String,
        userId: String
    ): AppResult<FirestoreError, PermissionLevel?> {
        TODO("Not yet implemented")
    }

    override suspend fun savePetMember(
        petId: String,
        member: Member
    ): AppResult<FirestoreError, Unit> {

        return safeFirestoreCall {
            firestore
                .collection(FirestoreCollections.PETS)
                .document(petId)
                .collection(FirestoreCollections.PET_MEMBERS)
                .document(member.userId)
                .set(member)
                .await()
        }
    }

    override suspend fun deletePetMember(
        petId: String,
        userId: String
    ): AppResult<FirestoreError, Unit> {
        TODO("Not yet implemented")
    }
}