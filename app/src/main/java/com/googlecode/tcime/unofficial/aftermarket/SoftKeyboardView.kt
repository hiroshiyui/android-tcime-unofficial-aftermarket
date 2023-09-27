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
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet
import android.util.Log
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Shows a soft keyboard, rendering keys and detecting key presses.
 */
class SoftKeyboardView : KeyboardView {
    private var currentKeyboard: SoftKeyboard? = null
    private var capsLock = false
    var isCangjieSimplified = false
        private set

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    private fun canCapsLock(): Boolean {
        // Caps-lock can only be toggled on English keyboard.
        return currentKeyboard != null && currentKeyboard!!.isEnglish
    }

    fun toggleCapsLock(): Boolean {
        if (canCapsLock()) {
            capsLock = !isShifted
            isShifted = capsLock
            return true
        }
        return false
    }

    fun updateCursorCaps(caps: Int) {
        if (canCapsLock()) {
            isShifted = capsLock || caps != 0
        }
    }

    private fun canCangjieSimplified(): Boolean {
        // Simplified-cangjie can only be toggled on Cangjie keyboard.
        return currentKeyboard != null && currentKeyboard!!.isCangjie
    }

    fun toggleCangjieSimplified(): Boolean {
        if (canCangjieSimplified()) {
            isCangjieSimplified = !isShifted
            isShifted = isCangjieSimplified
            return true
        }
        return false
    }

    fun hasEscape(): Boolean {
        return currentKeyboard != null && currentKeyboard!!.hasEscape()
    }

    fun setEscape(escape: Boolean) {
        if (currentKeyboard != null && currentKeyboard!!.setEscape(escape)) {
            invalidateEscapeKey()
        }
    }

    private fun invalidateEscapeKey() {
        // invalidateKey method is only supported since 1.6.
        if (invalidateKeyMethod != null) {
            try {
                invalidateKeyMethod!!.invoke(this, currentKeyboard!!.escapeKeyIndex)
            } catch (e: IllegalArgumentException) {
                Log.e("SoftKeyboardView", "exception: ", e)
            } catch (e: IllegalAccessException) {
                Log.e("SoftKeyboardView", "exception: ", e)
            } catch (e: InvocationTargetException) {
                Log.e("SoftKeyboardView", "exception: ", e)
            }
        }
    }

    override fun setKeyboard(keyboard: Keyboard) {
        if (keyboard is SoftKeyboard) {
            val escape = hasEscape()
            currentKeyboard = keyboard
            currentKeyboard!!.updateStickyKeys()
            currentKeyboard!!.setEscape(escape)
        }
        super.setKeyboard(keyboard)
    }

    override fun onLongPress(key: Keyboard.Key): Boolean {
        // 0xFF01~0xFF5E map to the full-width forms of the characters from
        // 0x21~0x7E. Make the long press as producing corresponding full-width
        // forms for these characters by adding the offset (0xff01 - 0x21).
        if (currentKeyboard != null) {
            // Symbol Full-width
            if (currentKeyboard!!.isSymbols && key.popupResId == 0 && key.codes[0] >= 0x21 && key.codes[0] <= 0x7E) {
                onKeyboardActionListener.onKey(key.codes[0] + FULL_WIDTH_OFFSET, null)
                return true
            }

            // English lower-case to upper-case
            if (currentKeyboard!!.isEnglish && key.popupResId == 0 && key.codes[0] >= 97 && key.codes[0] <= 124) {
                onKeyboardActionListener.onKey(key.codes[0] + UPPER_CASE_OFFSET, null)
                return true
            }
        }
        // Long pressed CHANGE_LETTER as options
        if (key.codes[0] == SoftKeyboard.KEYCODE_MODE_CHANGE_LETTER) {
            onKeyboardActionListener.onKey(SoftKeyboard.KEYCODE_OPTIONS, null)
            return true
        }
        return super.onLongPress(key)
    }

    companion object {
        private const val FULL_WIDTH_OFFSET = 0xFEE0
        private const val UPPER_CASE_OFFSET = -32
        private var invalidateKeyMethod: Method? = null

        init {
            try {
                invalidateKeyMethod = KeyboardView::class.java.getMethod(
                    "invalidateKey", *arrayOf<Class<*>?>(Int::class.javaPrimitiveType)
                )
            } catch (nsme: NoSuchMethodException) {
                Log.e(this::class.java.simpleName, "NoSuchMethodException")
            }
        }

        fun canRedrawKey(): Boolean {
            return invalidateKeyMethod != null
        }
    }
}