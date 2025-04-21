package com.example.petvitals.model.module

import com.example.petvitals.domain.SignUpDataValidator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ValidationModule {
    @Provides
    fun provideSignUpDataValidator(): SignUpDataValidator = SignUpDataValidator()
}

