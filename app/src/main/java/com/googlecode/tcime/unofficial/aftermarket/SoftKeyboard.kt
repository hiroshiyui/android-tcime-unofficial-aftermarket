/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.tcime.unofficial.aftermarket

import android.content.Context
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import android.inputmethodservice.Keyboard

/**
 * A soft keyboard definition.
 */
class SoftKeyboard(context: Context?, @JvmField var id: Int) : Keyboard(context, id) {
    private lateinit var symbolKey: Key
    private lateinit var enterKey: Key
    private lateinit var enterIcon: Drawable
    private lateinit var enterPreviewIcon: Drawable

    // Escape-key is the enter-key set 'Esc'.
    var escapeKeyIndex = 0
        private set
    private var escaped = false
    val isEnglish: Boolean
        get() = id == R.xml.qwerty || id == R.xml.qwerty_5row
    val isZhuyin: Boolean
        get() = id == R.xml.zhuyin
    val isCangjie: Boolean
        get() = id == R.xml.cangjie
    val isChinese: Boolean
        get() = isZhuyin || isCangjie
    val isNumberSymbol: Boolean
        get() = id == R.xml.symbols
    val isShiftSymbol: Boolean
        get() = id == R.xml.symbols_shift
    val isSymbols: Boolean
        /**
         * Returns `true` if the current keyboard is the symbol (number-symbol
         * or shift-symbol) keyboard; otherwise returns `false`.
         */
        get() = isNumberSymbol || isShiftSymbol

    /**
     * Updates the on/off status of sticky keys (symbol-key and shift-key).
     */
    fun updateStickyKeys() {
        if (isSymbols) {
            // Updates the shift-key status for symbol keyboards: shifted-off for
            // number-symbol keyboard and shifted-on for shift-symbol keyboard.
            isShifted = isShiftSymbol
        }
        symbolKey.on = isSymbols
    }

    fun hasEscape(): Boolean {
        return escaped
    }

    /**
     * Sets enter-key as the escape-key.
     *
     * @return `true` if the key is changed.
     */
    fun setEscape(escapeState: Boolean): Boolean {
        if (escaped != escapeState) {
            if (SoftKeyboardView.canRedrawKey()) {
                if (escapeState) {
                    enterKey.icon = null
                    enterKey.iconPreview = null
                    enterKey.label = ESCAPE_LABEL
                } else {
                    enterKey.icon = enterIcon
                    enterKey.iconPreview = enterPreviewIcon
                    enterKey.label = null
                }
            }
            escaped = escapeState
            return true
        }
        return false
    }

    override fun createKeyFromXml(
        res: Resources, parent: Row, x: Int, y: Int,
        parser: XmlResourceParser
    ): Key {
        val key: Key = SoftKey(res, parent, x, y, parser)
        if (key.codes[0] == KEYCODE_MODE_CHANGE) {
            symbolKey = key
        } else if (key.codes[0] == KEYCODE_ENTER) {
            enterKey = key
            enterIcon = key.icon
            enterPreviewIcon = key.iconPreview
            escapeKeyIndex = keys.size
            escaped = false
        }
        return key
    }

    /**
     * A soft key definition.
     */
    internal class SoftKey(
        res: Resources?, parent: Row?, x: Int, y: Int,
        parser: XmlResourceParser?
    ) : Key(res, parent, x, y, parser) {
        override fun onReleased(inside: Boolean) {
            // Override the default implementation to make the sticky status unchanged
            // since it has been handled by SoftKeyboard and InputView.
            pressed = !pressed
        }
    }

    companion object {
        const val KEYCODE_MODE_CHANGE_LETTER = -200
        const val KEYCODE_OPTIONS = -100
        const val KEYCODE_ENTER = 10
        const val KEYCODE_SPACE = 32
        const val KEYCODE_DELETE = 46
        private const val ESCAPE_LABEL = "Esc"
    }
}