package com.example.petvitals.di.module

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

