package com.googlecode.tcime.unofficial.aftermarket

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ZhuyinIMEInstrumentedTest {
    @Test
    fun testCreateKeyboardSwitch() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val zhuyinIME = ZhuyinIME()
        val keyboardSwitch = zhuyinIME.createKeyboardSwitch(context)
        keyboardSwitch.initializeKeyboard(960)
        val currentKeyboard = keyboardSwitch.currentKeyboard as SoftKeyboard
        assertNotNull(currentKeyboard)
        assertTrue(currentKeyboard.isEnglish)
    }
}