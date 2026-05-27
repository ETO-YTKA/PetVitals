package com.example.petvitals.data.usecase

import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.AccountError
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.error.SignUpError
import com.example.petvitals.domain.models.User
import com.example.petvitals.domain.repository.UserRepository
import com.example.petvitals.domain.usecase.SignUpUseCase
import jakarta.inject.Inject

class SignUpUseCaseImpl @Inject constructor(
    private val accountService: AccountService,
    private val userRepository: UserRepository
) : SignUpUseCase {

    override suspend fun invoke(
        username: String,
        email: String,
        password: String
    ): AppResult<SignUpError, Unit> {

        return when (val serviceResult = accountService.signUp(email, password)) {

            is AppResult.Success -> {
                val user = User(
                    id = serviceResult.data,
                    username = username,
                    email = email
                )

                when (val repositoryResult = userRepository.saveUser(user)) {

                    is AppResult.Success -> AppResult.Success(Unit)

                    is AppResult.Failure -> {
                        val signUpError = when (repositoryResult.error) {
                            is FirestoreError.Network -> SignUpError.Network
                            is FirestoreError.Unauthenticated -> SignUpError.Unauthenticated
                            else -> SignUpError.Unknown
                        }
                        return AppResult.Failure(signUpError)
                    }
                }

                AppResult.Success(Unit)
            }

            is AppResult.Failure -> {
                val error = when (serviceResult.error) {
                    is AccountError.Network -> SignUpError.Network
                    is AccountError.EmailAlreadyInUse -> SignUpError.EmailAlreadyInUse
                    else -> SignUpError.Unknown
                }

                AppResult.Failure(error)
            }
        }
    }
}
