package com.example.petvitals.ui.screens.joinpet

import com.example.petvitals.R
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.PetInviteError
import com.example.petvitals.domain.usecase.RedeemCodeUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JoinPetViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun codeChange_clearsPreviousError() = runTest(dispatcher) {
        val viewModel = JoinPetViewModel(
            FakeRedeemCodeUseCase(AppResult.Failure(PetInviteError.Network))
        )
        viewModel.onAction(JoinPetAction.OnCodeChange(CODE))
        viewModel.onAction(JoinPetAction.OnJoinPetClick)
        advanceUntilIdle()
        assertEquals(R.string.network_error, viewModel.uiState.value.errorMessageRes)

        viewModel.onAction(JoinPetAction.OnCodeChange("ABCD"))

        assertNull(viewModel.uiState.value.errorMessageRes)
    }

    @Test
    fun submit_clearsPreviousErrorBeforeRetry() = runTest(dispatcher) {
        val redeem = QueueRedeemCodeUseCase(
            AppResult.Failure(PetInviteError.Network),
            AppResult.Success(Unit)
        )
        val viewModel = JoinPetViewModel(redeem)
        viewModel.onAction(JoinPetAction.OnCodeChange(CODE))
        viewModel.onAction(JoinPetAction.OnJoinPetClick)
        advanceUntilIdle()

        viewModel.onAction(JoinPetAction.OnJoinPetClick)

        assertTrue(viewModel.uiState.value.isSubmitting)
        assertNull(viewModel.uiState.value.errorMessageRes)
    }

    @Test
    fun successfulRedemption_emitsJoinedEventAndStopsSubmitting() = runTest(dispatcher) {
        val viewModel = JoinPetViewModel(
            FakeRedeemCodeUseCase(AppResult.Success(Unit))
        )
        val event = async { viewModel.events.first() }
        viewModel.onAction(JoinPetAction.OnCodeChange(CODE))

        viewModel.onAction(JoinPetAction.OnJoinPetClick)
        advanceUntilIdle()

        assertEquals(JoinPetEvent.Joined, event.await())
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun repeatedSubmit_whileSubmitting_invokesRedemptionOnce() = runTest(dispatcher) {
        val redeem = CountingRedeemCodeUseCase()
        val viewModel = JoinPetViewModel(redeem)
        viewModel.onAction(JoinPetAction.OnCodeChange(CODE))

        viewModel.onAction(JoinPetAction.OnJoinPetClick)
        viewModel.onAction(JoinPetAction.OnJoinPetClick)
        advanceUntilIdle()

        assertEquals(1, redeem.calls)
    }

    @Test
    fun codeChange_whileSubmitting_isIgnoredAndSubmittedCodeIsStable() = runTest(dispatcher) {
        val redeem = SuspendedRedeemCodeUseCase()
        val viewModel = JoinPetViewModel(redeem)
        viewModel.onAction(JoinPetAction.OnCodeChange(CODE))

        viewModel.onAction(JoinPetAction.OnJoinPetClick)
        runCurrent()
        viewModel.onAction(JoinPetAction.OnCodeChange("ZZZZ"))

        assertEquals(CODE, viewModel.uiState.value.code)
        assertEquals(listOf(CODE), redeem.codes)
        redeem.release.complete(Unit)
        advanceUntilIdle()
    }

    private class FakeRedeemCodeUseCase(
        private val result: AppResult<PetInviteError, Unit>
    ) : RedeemCodeUseCase {
        override suspend fun invoke(code: String): AppResult<PetInviteError, Unit> = result
    }

    private class QueueRedeemCodeUseCase(
        vararg results: AppResult<PetInviteError, Unit>
    ) : RedeemCodeUseCase {
        private val results = ArrayDeque(results.toList())

        override suspend fun invoke(code: String): AppResult<PetInviteError, Unit> =
            results.removeFirst()
    }

    private class CountingRedeemCodeUseCase : RedeemCodeUseCase {
        var calls = 0

        override suspend fun invoke(code: String): AppResult<PetInviteError, Unit> {
            calls++
            return AppResult.Success(Unit)
        }
    }

    private class SuspendedRedeemCodeUseCase : RedeemCodeUseCase {
        val codes = mutableListOf<String>()
        val release = CompletableDeferred<Unit>()

        override suspend fun invoke(code: String): AppResult<PetInviteError, Unit> {
            codes += code
            release.await()
            return AppResult.Success(Unit)
        }
    }

    private companion object {
        const val CODE = "ABCD1234EFGH5678"
    }
}
