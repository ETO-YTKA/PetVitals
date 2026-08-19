package com.example.petvitals.domain.repository

import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.PetInviteError
import com.example.petvitals.domain.models.PetInvite
import com.example.petvitals.domain.models.User

interface PetInviteRepository {
    suspend fun createCode(invite: PetInvite): AppResult<PetInviteError, Unit>
    suspend fun redeemCode(rawCode: String, user: User): AppResult<PetInviteError, Unit>
    suspend fun revokeCode(code: String): AppResult<PetInviteError, Unit>
    suspend fun getCodes(petId: String): AppResult<PetInviteError, List<PetInvite>>
}