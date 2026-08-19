package com.example.petvitals.di

import com.example.petvitals.data.usecase.CreateInviteCodeUseCaseImpl
import com.example.petvitals.data.usecase.CreatePetUseCaseImpl
import com.example.petvitals.data.usecase.DeleteRecordUseCaseImpl
import com.example.petvitals.data.usecase.GetCurrentUserRecordsImpl
import com.example.petvitals.data.usecase.GetPetPermissionUseCaseImpl
import com.example.petvitals.data.usecase.RedeemCodeUseCaseImpl
import com.example.petvitals.data.usecase.SaveFoodUseCaseImpl
import com.example.petvitals.data.usecase.SaveMedicationUseCaseImpl
import com.example.petvitals.data.usecase.SaveRecordUseCaseImpl
import com.example.petvitals.data.usecase.SignUpUseCaseImpl
import com.example.petvitals.domain.usecase.CreateInviteCodeUseCase
import com.example.petvitals.domain.usecase.CreatePetUseCase
import com.example.petvitals.domain.usecase.DeleteRecordUseCase
import com.example.petvitals.domain.usecase.GetCurrentUserRecords
import com.example.petvitals.domain.usecase.GetPetPermissionUseCase
import com.example.petvitals.domain.usecase.RedeemCodeUseCase
import com.example.petvitals.domain.usecase.SaveFoodUseCase
import com.example.petvitals.domain.usecase.SaveMedicationUseCase
import com.example.petvitals.domain.usecase.SaveRecordUseCase
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

    @Binds
    abstract fun bindCreatePetUseCase(createPetUseCase: CreatePetUseCaseImpl): CreatePetUseCase

    @Binds
    abstract fun bindGetPetPermissionUseCase(
        getPetPermissionUseCase: GetPetPermissionUseCaseImpl
    ): GetPetPermissionUseCase

    @Binds
    abstract fun bindSaveFoodUseCase(saveFood: SaveFoodUseCaseImpl): SaveFoodUseCase

    @Binds
    abstract fun bindSaveMedicationUseCase(
        saveMedication: SaveMedicationUseCaseImpl
    ): SaveMedicationUseCase

    @Binds
    abstract fun bindSaveRecordUseCase(
        saveRecordUseCase: SaveRecordUseCaseImpl
    ): SaveRecordUseCase

    @Binds
    abstract fun bindDeleteRecordUseCase(
        deleteRecordUseCase: DeleteRecordUseCaseImpl
    ): DeleteRecordUseCase

    @Binds
    abstract fun bindGetCurrentUserRecords(
        getCurrentUserRecords: GetCurrentUserRecordsImpl
    ): GetCurrentUserRecords

    @Binds
    abstract fun bindCreateInviteCodeUseCase(
        createInviteCode: CreateInviteCodeUseCaseImpl
    ): CreateInviteCodeUseCase

    @Binds
    abstract fun bindRedeemCodeUseCase(
        redeemCode: RedeemCodeUseCaseImpl
    ): RedeemCodeUseCase
}
