package com.example.petvitals.data.di

import com.example.petvitals.data.repository.FoodRepositoryImpl
import com.example.petvitals.data.repository.MedicationRepositoryImpl
import com.example.petvitals.data.repository.PetPermissionRepositoryImpl
import com.example.petvitals.data.repository.PetRepositoryImpl
import com.example.petvitals.data.repository.RecordRepositoryImpl
import com.example.petvitals.data.repository.UserRepositoryImpl
import com.example.petvitals.domain.repository.FoodRepository
import com.example.petvitals.domain.repository.MedicationRepository
import com.example.petvitals.domain.repository.PetPermissionRepository
import com.example.petvitals.domain.repository.PetRepository
import com.example.petvitals.domain.repository.RecordRepository
import com.example.petvitals.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun provideUserRepository(impl: UserRepositoryImpl): UserRepository
    @Binds
    abstract fun providePetRepository(impl: PetRepositoryImpl): PetRepository
    @Binds
    abstract fun provideRecordRepository(impl: RecordRepositoryImpl): RecordRepository
    @Binds
    abstract fun provideMedicationRepository(impl: MedicationRepositoryImpl): MedicationRepository
    @Binds
    abstract fun provideFoodRepository(impl: FoodRepositoryImpl): FoodRepository
    @Binds
    abstract fun providePetPermissionRepository(impl: PetPermissionRepositoryImpl): PetPermissionRepository
}