package com.googlecode.tcime.unofficial.aftermarket

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.view.inputmethod.InputConnection
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class ZhuyinIMELocalTest {
    private var sharedPrefs: SharedPreferences = mock(SharedPreferences::class.java)
    private var context: Context = mock(Context::class.java)
    private var inputConnection: InputConnection = mock(InputConnection::class.java)
    private var resources: Resources = mock(Resources::class.java)

    @Before
    fun before() {
        this.sharedPrefs = mock(SharedPreferences::class.java)
        this.context = mock(Context::class.java)
        this.resources = mock(Resources::class.java)
        Mockito.`when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs)
        Mockito.`when`(context.resources).thenReturn(resources)
    }

    @Test
    fun testCreateKeyboardSwitch() {
        val classUnderTest = ZhuyinIME()
        val keyboardSwitch: KeyboardSwitch = classUnderTest.createKeyboardSwitch(context)
        assertNotNull(keyboardSwitch)
        keyboardSwitch.initializeKeyboard(960)
        val currentKeyboard = keyboardSwitch.currentKeyboard as SoftKeyboard
        assertNotNull(currentKeyboard)
        assert(currentKeyboard.isEnglish)
    }

    @Test
    fun testCreateEditor() {
        val classUnderTest = ZhuyinIME()
        val editor: ZhuyinEditor = classUnderTest.createEditor() as ZhuyinEditor
        assertNotNull(editor)
        editor.composingText = StringBuilder("綠茶")
        Assert.assertTrue(editor.hasComposingText())
        val composingText = editor.composingText().toString()
        Assert.assertEquals("綠茶", composingText)

        editor.clearComposingText(inputConnection)
        Assert.assertFalse(editor.hasComposingText())
    }
}