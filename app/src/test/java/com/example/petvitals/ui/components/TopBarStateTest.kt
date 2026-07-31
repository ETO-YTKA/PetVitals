package com.example.petvitals.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalMaterial3Api::class)
class TopBarStateTest {

    @Test
    fun resetTopBarScroll_clearsPreviousOverlap() {
        val state = TopAppBarState(
            initialHeightOffsetLimit = -64f,
            initialHeightOffset = -20f,
            initialContentOffset = -120f
        )

        state.resetTopBarScroll()

        assertEquals(0f, state.heightOffset, 0f)
        assertEquals(0f, state.contentOffset, 0f)
    }
}
