package com.googlecode.tcime.unofficial.aftermarket

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class CangjieIMEInstrumentedTest {
    private lateinit var context: Context
    private lateinit var cangjieIME: CangjieIME

    @Before
    fun before() {
        context = ApplicationProvider.getApplicationContext()
        cangjieIME = CangjieIME()
    }

    @Test
    fun testCreateKeyboardSwitch() {
        val keyboardSwitch = cangjieIME.createKeyboardSwitch(context)
        keyboardSwitch.initializeKeyboard(960)
        val currentKeyboard = keyboardSwitch.currentKeyboard as SoftKeyboard
        assertNotNull(currentKeyboard)
        assertTrue(currentKeyboard.isEnglish)

        // switch to Bopomofo keyboard
        currentKeyboard.id = R.xml.cangjie
        assertTrue(currentKeyboard.isCangjie)
        assertTrue(currentKeyboard.isChinese)

        // switch to QWERTY English keyboard
        currentKeyboard.id = R.xml.qwerty
        assertTrue(currentKeyboard.isEnglish)
    }

    @Test
    fun testCreateEditor() {
        val editor: CangjieEditor = cangjieIME.createEditor() as CangjieEditor
        assertNotNull(editor)
        editor.composingText = StringBuilder("綠茶")
        assertTrue(editor.hasComposingText())
        val composingText = editor.composingText().toString()
        assertEquals("綠茶", composingText)

        editor.clearComposingText(cangjieIME.currentInputConnection)
        assertFalse(editor.hasComposingText())
    }

    @Test
    fun testCreateWordDictionary() {
        val dictionary: CangjieDictionary =
            cangjieIME.createWordDictionary(context) as CangjieDictionary
        assertNotNull(dictionary)
        var words: String = dictionary.getWords("卜口")
        assertEquals("占", words)
        words = dictionary.getWords("人大口")
        assert(words.contains("知"))
        assert(words.contains("佑"))
    }
}