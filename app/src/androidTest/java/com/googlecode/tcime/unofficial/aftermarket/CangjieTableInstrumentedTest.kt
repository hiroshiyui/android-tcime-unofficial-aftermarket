package com.googlecode.tcime.unofficial.aftermarket

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CangjieTableInstrumentedTest {
    @Test
    fun testIsLetter() {
        assert(CangjieTable.isLetter('日'))
        assert(CangjieTable.isLetter('月'))
        assert(CangjieTable.isLetter('卜'))
        assertFalse(CangjieTable.isLetter('虫'))
    }
}