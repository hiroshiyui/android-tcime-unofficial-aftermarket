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
class ZhuyinIMEInstrumentedTest {
    private lateinit var context: Context
    private lateinit var zhuyinIME: ZhuyinIME

    @Before
    fun before() {
        context = ApplicationProvider.getApplicationContext()
        zhuyinIME = ZhuyinIME()
    }

    @Test
    fun testCreateKeyboardSwitch() {
        val keyboardSwitch = zhuyinIME.createKeyboardSwitch(context)
        keyboardSwitch.initializeKeyboard(960)
        val currentKeyboard = keyboardSwitch.currentKeyboard as SoftKeyboard
        assertNotNull(currentKeyboard)
        assertTrue(currentKeyboard.isEnglish)

        // switch to Bopomofo keyboard
        currentKeyboard.id = R.xml.zhuyin
        assertTrue(currentKeyboard.isZhuyin)
        assertTrue(currentKeyboard.isChinese)

        // switch to QWERTY English keyboard
        currentKeyboard.id = R.xml.qwerty
        assertTrue(currentKeyboard.isEnglish)
    }

    @Test
    fun testCreateEditor() {
        val editor: ZhuyinEditor = zhuyinIME.createEditor() as ZhuyinEditor
        assertNotNull(editor)
        editor.composingText = StringBuilder("綠茶")
        assertTrue(editor.hasComposingText())
        val composingText = editor.composingText().toString()
        assertEquals("綠茶", composingText)

        editor.clearComposingText(zhuyinIME.currentInputConnection)
        assertFalse(editor.hasComposingText())
    }

    @Test
    fun testCreateWordDictionary() {
        val dictionary: ZhuyinDictionary =
            zhuyinIME.createWordDictionary(context) as ZhuyinDictionary
        var words: String
        assertNotNull(dictionary)
        words = dictionary.getWords("ㄇㄠ")
        assertEquals("貓", words)
        words = dictionary.getWords("ㄉㄠ")
        assert(words.contains("刀"))
        assert(words.contains("叨"))
    }
}