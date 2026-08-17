package com.example.petvitals.domain.repository

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PetInvite

interface PetInviteRepository {
    suspend fun createCode(invite: PetInvite): AppResult<FirestoreError, Unit>
    suspend fun redeemCode(inviteId: String, member: Member): AppResult<FirestoreError, Unit>
    suspend fun revokeCode(inviteId: String): AppResult<FirestoreError, Unit>
    suspend fun getCodes(petId: String): AppResult<FirestoreError, List<PetInvite>>
}