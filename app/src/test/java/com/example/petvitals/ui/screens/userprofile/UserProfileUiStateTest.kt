package com.example.petvitals.ui.screens.userprofile

import com.example.petvitals.R
import com.example.petvitals.domain.models.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileUiStateTest {

    @Test
    fun defaultState_isLoading() {
        assertTrue(UserProfileUiState().isLoading)
    }

    @Test
    fun failureThenRetryThenSuccess_clearsPreviousError() {
        val user = User(id = "user-id", username = "Taylor", email = "taylor@example.com")

        val failedState = UserProfileUiState().showError(R.string.network_error)
        val loadingState = failedState.startLoading()
        val successState = loadingState.showUser(user)

        assertTrue(loadingState.isLoading)
        assertNull(loadingState.errorMessageRes)
        assertFalse(successState.isLoading)
        assertEquals(user, successState.user)
        assertNull(successState.errorMessageRes)
    }

    @Test
    fun showError_stopsLoadingAndClearsPreviousUser() {
        val user = User(id = "user-id", username = "Taylor", email = "taylor@example.com")

        val errorState = UserProfileUiState(user = user, isLoading = false)
            .showError(R.string.unexpected_error)

        assertFalse(errorState.isLoading)
        assertNull(errorState.user)
        assertEquals(R.string.unexpected_error, errorState.errorMessageRes)
    }
}
