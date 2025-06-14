package com.example.petvitals.di.module

import com.example.petvitals.data.repository.food.FoodRepository
import com.example.petvitals.data.repository.food.FoodRepositoryImpl
import com.example.petvitals.data.repository.medication.MedicationRepository
import com.example.petvitals.data.repository.medication.MedicationRepositoryImpl
import com.example.petvitals.data.repository.pet.PetRepository
import com.example.petvitals.data.repository.pet.PetRepositoryImpl
import com.example.petvitals.data.repository.record.RecordRepository
import com.example.petvitals.data.repository.record.RecordRepositoryImpl
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
    @Binds abstract fun providePetRepository(impl: PetRepositoryImpl): PetRepository
    @Binds abstract fun provideRecordRepository(impl: RecordRepositoryImpl): RecordRepository
    @Binds abstract fun provideMedicationRepository(impl: MedicationRepositoryImpl): MedicationRepository
    @Binds abstract fun provideFoodRepository(impl: FoodRepositoryImpl): FoodRepository
}