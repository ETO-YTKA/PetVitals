package com.example.petvitals.data.repository

import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.data.utils.safeFirestoreCall
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.repository.PetRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val accountService: AccountService
) : PetRepository {

    override suspend fun savePet(pet: Pet): AppResult<FirestoreError, Unit> {

        return safeFirestoreCall {
            firestore
                .collection(FirestoreCollections.PETS)
                .document(pet.id)
                .set(pet)
                .await()
        }
    }

    override suspend fun getPetById(petId: String): AppResult<FirestoreError, Pet?> {

        return safeFirestoreCall {
            firestore
                .collection(FirestoreCollections.PETS)
                .document(petId)
                .get()
                .await()
                .toObject<Pet>()
        }
    }

    override suspend fun getCurrentUserPets(): AppResult<FirestoreError, List<Pet>> {

        val userId = accountService.currentUserId ?: return AppResult.Failure(FirestoreError.Unauthenticated)

        return safeFirestoreCall {

            val memberDocs = firestore
                .collectionGroup(FirestoreCollections.PET_MEMBERS)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            memberDocs.mapNotNull { memberDoc ->

                val member = memberDoc.toObject<Member>()
                val petDoc = memberDoc.reference.parent.parent ?: return@mapNotNull null

                petDoc
                    .get()
                    .await()
                    .toObject<Pet>()
                    ?.copy(currentUserPermission = member.permissionLevel)
            }
        }
    }

    override suspend fun deletePet(petId: String): AppResult<FirestoreError, Unit> {

        return safeFirestoreCall {
            firestore
                .collection(FirestoreCollections.PETS)
                .document(petId)
                .delete()
                .await()
        }
    }

    override suspend fun createPetWithOwner(
        pet: Pet,
        member: Member
    ): AppResult<FirestoreError, Unit> {

        return safeFirestoreCall {
            val petRef = firestore
                .collection(FirestoreCollections.PETS)
                .document(pet.id)

            val memberRef = petRef
                .collection(FirestoreCollections.PET_MEMBERS)
                .document(member.userId)

            firestore.runBatch { batch ->
                batch.set(petRef, pet)
                batch.set(memberRef, member)
            }.await()
        }
    }
}