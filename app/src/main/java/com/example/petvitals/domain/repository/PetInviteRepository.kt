package com.example.petvitals.domain.repository

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.PetInvite
import com.example.petvitals.domain.models.User

interface PetInviteRepository {
    suspend fun createCode(invite: PetInvite): AppResult<FirestoreError, Unit>
    suspend fun redeemCode(rawCode: String, user: User): AppResult<FirestoreError, Unit>
    suspend fun revokeCode(code: String): AppResult<FirestoreError, Unit>
    suspend fun getCodes(petId: String): AppResult<FirestoreError, List<PetInvite>>
}