package com.example.petvitals.di

import com.example.petvitals.data.usecase.SignUpUseCaseImpl
import com.example.petvitals.domain.usecase.SignUpUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    abstract fun bindSignUpUseCase(signUpUseCase: SignUpUseCaseImpl): SignUpUseCase
}