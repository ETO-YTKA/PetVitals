package com.example.petvitals.di.module

import com.example.petvitals.data.repository.user.UserRepository
import com.example.petvitals.data.repository.user.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun provideUserRepository(impl: UserRepositoryImpl): UserRepository
}