package com.example.petvitals.data.service.account

import com.example.petvitals.domain.error.AccountError
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountErrorMapperTest {

    @Test
    fun toAccountError_mapsUnknownErrorsToUnknown() {
        assertEquals(AccountError.Unknown, Exception("Unknown").toAccountError())
    }

    @Test
    fun toAccountError_mapsIllegalArgumentExceptionToEmptyFields() {
        assertEquals(AccountError.EmptyFields, IllegalArgumentException().toAccountError())
    }
}
