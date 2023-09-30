package com.googlecode.tcime.unofficial.aftermarket

import android.content.Context
import android.content.SharedPreferences
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class ZhuyinIMETest {
    private var sharedPrefs: SharedPreferences = mock(SharedPreferences::class.java)
    private var context: Context = mock(Context::class.java)

    @Before
    fun before() {
        this.sharedPrefs = mock(SharedPreferences::class.java)
        this.context = mock(Context::class.java)
        Mockito.`when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs)
    }

    @Test
    fun testZhuyinIME() {
        val classUnderTest = ZhuyinIME()
        val keyboardSwitch: KeyboardSwitch = classUnderTest.createKeyboardSwitch(context)
        val currentKeyboard = keyboardSwitch.currentKeyboard as SoftKeyboard
        assert(currentKeyboard.isZhuyin) // currently not passed :-P
    }
}