package com.example.petvitals.ui.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageProcessorTest {

    @Test
    fun calculateTargetSize_landscapeImage_fitsWithinBalancedBounds() {
        val result = calculateTargetSize(sourceWidth = 4000, sourceHeight = 3000)

        assertEquals(720 to 540, result)
    }

    @Test
    fun calculateTargetSize_portraitImage_fitsWithinBalancedBounds() {
        val result = calculateTargetSize(sourceWidth = 3000, sourceHeight = 4000)

        assertEquals(540 to 720, result)
    }

    @Test
    fun calculateTargetSize_smallImage_doesNotUpscale() {
        val result = calculateTargetSize(sourceWidth = 320, sourceHeight = 240)

        assertEquals(320 to 240, result)
    }

    @Test
    fun calculateInSampleSize_largeImage_usesLargestSafePowerOfTwo() {
        val result = calculateInSampleSize(sourceWidth = 4000, sourceHeight = 3000)

        assertEquals(4, result)
    }

    @Test
    fun calculateInSampleSize_wideImage_accountsForScaledTargetHeight() {
        val result = calculateInSampleSize(sourceWidth = 4000, sourceHeight = 1000)

        assertEquals(4, result)
    }

    @Test
    fun calculateInSampleSize_smallImage_doesNotSample() {
        val result = calculateInSampleSize(sourceWidth = 320, sourceHeight = 240)

        assertEquals(1, result)
    }
}
