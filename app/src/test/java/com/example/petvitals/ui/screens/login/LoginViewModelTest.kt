package com.example.petvitals.ui.screens.login

import android.content.ContextWrapper
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginViewModelTest {

    @Test
    fun onAction_withEmailChanged_updatesEmail() {
        val viewModel = LoginViewModel(
            accountService = FakeAccountService(),
            context = ContextWrapper(null)
        )

        viewModel.onAction(LoginAction.OnEmailChanged("owner@example.com"))

        assertEquals("owner@example.com", viewModel.uiState.value.email)
    }

    @Test
    fun onAction_withPasswordChanged_updatesPassword() {
        val viewModel = LoginViewModel(
            accountService = FakeAccountService(),
            context = ContextWrapper(null)
        )

        viewModel.onAction(LoginAction.OnPasswordChanged("secret-password"))

        assertEquals("secret-password", viewModel.uiState.value.password)
    }

    private class FakeAccountService : AccountService {
        override val currentUser: Flow<User?> = emptyFlow()
        override val currentUserId: String = ""
        override val isEmailVerified: Boolean = true
        override val currentUserEmail: String? = null

        override fun hasUser(): Boolean = false

        override suspend fun signIn(email: String, password: String) = Unit

        override suspend fun signUp(email: String, password: String): String = ""

        override suspend fun logout() = Unit

        override suspend fun deleteAccount() = Unit

        override suspend fun sendVerificationEmail() = Unit

        override suspend fun sendPasswordResetEmail(email: String) = Unit
    }
}
