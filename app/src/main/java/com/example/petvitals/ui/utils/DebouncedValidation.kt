package com.example.petvitals.ui.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val DefaultValidationDebounceMillis = 700L

fun debounceValidation(
    scope: CoroutineScope,
    previousJob: Job?,
    delayMillis: Long = DefaultValidationDebounceMillis,
    validate: suspend () -> Unit,
): Job {
    previousJob?.cancel()
    return scope.launch {
        delay(delayMillis)
        validate()
    }
}
