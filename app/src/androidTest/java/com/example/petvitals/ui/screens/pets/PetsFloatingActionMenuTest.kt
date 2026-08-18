package com.example.petvitals.ui.screens.pets

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.petvitals.R
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.AccountError
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.models.User
import com.example.petvitals.domain.repository.PetRepository
import com.example.petvitals.ui.theme.PetVitalsTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PetsFloatingActionMenuTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun menu_expandsAndDispatchesJoinAndAddDestinations() {
        var joinCalls = 0
        var addCalls = 0
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val viewModel = PetsViewModel(
            accountService = FakeAccountService(),
            petRepository = FakePetRepository(),
            context = context
        )

        composeRule.setContent {
            PetVitalsTheme {
                PetsScreen(
                    viewModel = viewModel,
                    onNavigateToPetProfile = {},
                    onNavigateToAddPet = { addCalls += 1 },
                    onNavigateToJoinPet = { joinCalls += 1 },
                    onNavigateToUserProfile = {},
                    onNavigateToSplash = {}
                )
            }
        }

        val menuToggle = composeRule.onNode(isToggleable() and hasClickAction())
        menuToggle.assertIsOff().performClick().assertIsOn()
        composeRule.onNodeWithText(string(R.string.join_pet)).assertIsDisplayed().performClick()
        composeRule.onNodeWithText(string(R.string.add_pet)).assertIsDisplayed().performClick()

        composeRule.runOnIdle {
            assertEquals(1, joinCalls)
            assertEquals(1, addCalls)
        }

        menuToggle.performClick().assertIsOff()
        composeRule.onNodeWithText(string(R.string.join_pet)).assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.add_pet)).assertDoesNotExist()
    }

    private fun string(@StringRes id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private class FakeAccountService : AccountService {
        override val currentUser: Flow<User?> = flowOf(
            User(id = "owner-id", username = "Owner", email = "owner@example.com")
        )
        override val currentUserId: String = "owner-id"
        override val isEmailVerified: Boolean = true
        override val currentUserEmail: String = "owner@example.com"

        override fun hasUser(): Boolean = true

        override suspend fun signIn(
            email: String,
            password: String
        ): AppResult<AccountError, Unit> = AppResult.Success(Unit)

        override suspend fun signUp(
            email: String,
            password: String
        ): AppResult<AccountError, String> = AppResult.Success("owner-id")

        override fun logout() = Unit

        override suspend fun deleteAccount(): AppResult<AccountError, Unit> =
            AppResult.Success(Unit)

        override suspend fun sendVerificationEmail(): AppResult<AccountError, Unit> =
            AppResult.Success(Unit)

        override suspend fun sendPasswordResetEmail(
            email: String
        ): AppResult<AccountError, Unit> = AppResult.Success(Unit)
    }

    private class FakePetRepository : PetRepository {
        override suspend fun savePet(pet: Pet): AppResult<FirestoreError, Unit> =
            AppResult.Success(Unit)

        override suspend fun getPetById(petId: String): AppResult<FirestoreError, Pet?> =
            AppResult.Success(null)

        override suspend fun getCurrentUserPets(): AppResult<FirestoreError, List<Pet>> =
            AppResult.Success(listOf(Pet(id = "pet-id", name = "Milo")))

        override suspend fun deletePet(petId: String): AppResult<FirestoreError, Unit> =
            AppResult.Success(Unit)

        override suspend fun createPetWithOwner(
            pet: Pet,
            member: Member
        ): AppResult<FirestoreError, Unit> = AppResult.Success(Unit)
    }
}
