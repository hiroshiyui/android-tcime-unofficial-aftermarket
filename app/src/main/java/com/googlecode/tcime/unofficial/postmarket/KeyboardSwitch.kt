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
package com.googlecode.tcime.unofficial.postmarket

import android.content.Context
import android.content.SharedPreferences
import android.inputmethodservice.Keyboard
import android.preference.PreferenceManager
import android.text.InputType

/**
 * Switches between four input modes: two symbol modes (number-symbol and
 * shift-symbol) and two letter modes (Chinese and English), by three toggling
 * keys: mode-change-letter, mode-change, and shift keys.
 *
 * <pre>
 * State transition (the initial state is always 'English'):
 * English
 * MODE_CHANGE_LETTER -> Chinese
 * MODE_CHANGE -> NumberSymbol
 * SHIFT -> English (no-op)
 * Chinese
 * MODE_CHANGE_LETTER -> English
 * MODE_CHANGE -> NumberSymbol
 * SHIFT (n/a)
 * NumberSymbol
 * MODE_CHANGE_LETTER (n/a)
 * MODE_CHANGE -> English or Chinese
 * SHIFT -> ShiftSymbol
 * ShiftSymbol
 * MODE_CHANGE_LETTER (n/a)
 * MODE_CHANGE -> English or Chinese
 * SHIFT -> NumberSymbol
</pre> *
 */
class KeyboardSwitch(private val context: Context, private val chineseKeyboardId: Int) {
    private var numberSymbolKeyboard: SoftKeyboard? = null
    private var shiftSymbolKeyboard: SoftKeyboard? = null
    private var englishKeyboard: SoftKeyboard? = null
    private var chineseKeyboard: SoftKeyboard? = null
    var currentKeyboard: SoftKeyboard? = null
    private var wasEnglishToSymbol = false
    private var currentDisplayWidth = 0
    private val preferences: SharedPreferences
    private var qwerty5row = false

    init {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    /**
     * Recreates the keyboards if the display-width has been changed.
     *
     * @param displayWidth the display-width for keyboards.
     */
    fun initializeKeyboard(displayWidth: Int) {
        // Check if user want the 5-row qwerty keyboard
        qwerty5row = preferences.getBoolean(context.getString(R.string.prefs_qwerty5row_key), false)
        if (currentKeyboard != null && displayWidth == currentDisplayWidth) {
            // Update the English keyboard setting
            if (englishKeyboard!!.id != (if (qwerty5row) R.xml.qwerty_5row else R.xml.qwerty)) {
                englishKeyboard =
                    SoftKeyboard(context, if (qwerty5row) R.xml.qwerty_5row else R.xml.qwerty)
                if (currentKeyboard!!.isEnglish) toEnglish()
            }
            return
        }
        currentDisplayWidth = displayWidth
        chineseKeyboard = SoftKeyboard(context, chineseKeyboardId)
        numberSymbolKeyboard = SoftKeyboard(context, R.xml.symbols)
        shiftSymbolKeyboard = SoftKeyboard(context, R.xml.symbols_shift)
        englishKeyboard = SoftKeyboard(context, if (qwerty5row) R.xml.qwerty_5row else R.xml.qwerty)
        if (currentKeyboard == null) {
            // Select English keyboard at the first time the input method is launched.
            toEnglish()
        } else {
            // Preserve the selected keyboard and its shift-status.
            val isShifted = currentKeyboard!!.isShifted
            if (currentKeyboard!!.isEnglish) {
                toEnglish()
            } else if (currentKeyboard!!.isChinese) {
                toChinese()
            } else if (currentKeyboard!!.isNumberSymbol) {
                toNumberSymbol()
            } else if (currentKeyboard!!.isShiftSymbol) {
                toShiftSymbol()
            } else {
                throw IllegalStateException("The keyboard-mode is invalid.")
            }
            // Restore shift-status.
            currentKeyboard!!.isShifted = isShifted
        }
    }

    fun getCurrentKeyboard(): Keyboard? {
        return currentKeyboard
    }

    /**
     * Switches to the appropriate keyboard based on the type of text being
     * edited, for example, the symbol keyboard for numbers.
     *
     * @param inputType one of the `InputType.TYPE_CLASS_*` values listed in
     * [android.text.InputType].
     */
    fun onStartInput(inputType: Int) {
        when (inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_DATETIME, InputType.TYPE_CLASS_PHONE ->                 // Numbers, dates, and phones default to the symbol keyboard, with
                // no extra features.
                toNumberSymbol()

            InputType.TYPE_CLASS_TEXT -> {
                val variation = inputType and InputType.TYPE_MASK_VARIATION
                if (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS || variation == InputType.TYPE_TEXT_VARIATION_URI || variation == InputType.TYPE_TEXT_VARIATION_PASSWORD || variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    toEnglish()
                } else {
                    // Switch to non-symbol keyboard, either Chinese or English keyboard,
                    // for other general text editing.
                    toNonSymbols()
                }
            }

            else ->                 // Switch to non-symbol keyboard, either Chinese or English keyboard,
                // for all other input types.
                toNonSymbols()
        }
    }

    /**
     * Consumes the pressed key-code and switch keyboard if applicable.
     *
     * @return `true` if the keyboard is switched; otherwise `false`.
     */
    fun onKey(keyCode: Int): Boolean {
        when (keyCode) {
            SoftKeyboard.KEYCODE_MODE_CHANGE_LETTER -> {
                if (currentKeyboard!!.isEnglish) {
                    toChinese()
                } else {
                    toEnglish()
                }
                return true
            }

            Keyboard.KEYCODE_MODE_CHANGE -> {
                if (currentKeyboard!!.isSymbols) {
                    toNonSymbols()
                } else {
                    toNumberSymbol()
                }
                return true
            }

            Keyboard.KEYCODE_SHIFT -> if (currentKeyboard!!.isNumberSymbol) {
                toShiftSymbol()
                return true
            } else if (currentKeyboard!!.isShiftSymbol) {
                toNumberSymbol()
                return true
            }
        }

        // Return false if the key isn't consumed to switch a keyboard.
        return false
    }

    /**
     * Switches to the number-symbol keyboard and remembers if it was English.
     */
    private fun toNumberSymbol() {
        if (!currentKeyboard!!.isSymbols) {
            // Remember the current non-symbol keyboard to switch back from symbols.
            wasEnglishToSymbol = currentKeyboard!!.isEnglish
        }
        currentKeyboard = numberSymbolKeyboard
    }

    private fun toShiftSymbol() {
        currentKeyboard = shiftSymbolKeyboard
    }

    private fun toEnglish() {
        currentKeyboard = englishKeyboard
    }

    private fun toChinese() {
        currentKeyboard = chineseKeyboard
    }

    /**
     * Switches from symbol (number-symbol or shift-symbol) keyboard,
     * back to the non-symbol (English or Chinese) keyboard.
     */
    private fun toNonSymbols() {
        if (currentKeyboard!!.isSymbols) {
            if (wasEnglishToSymbol) {
                toEnglish()
            } else {
                toChinese()
            }
        }
    }

    val languageIcon: Int
        /**
         * Return the IME status icon (English / Chinese)
         */
        get() = if (currentKeyboard == null || currentKeyboard!!.isEnglish) ICON_RES_ID[0] else ICON_RES_ID[1]

    companion object {
        private val ICON_RES_ID = intArrayOf(R.drawable.ime_en, R.drawable.ime_ch)
    }
}