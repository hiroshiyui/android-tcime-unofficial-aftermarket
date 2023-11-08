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
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.preference.PreferenceManager
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import com.googlecode.tcime.unofficial.aftermarket.KeyboardView.Layout

/**
 * Zhuyin input method.
 */
class ZhuyinIME : AbstractIME() {
    private var keyMapping: HashMap<Int, Int>? = null
    private lateinit var preferences: SharedPreferences
    private var isAltUsed = false
    private var isMS3 = false
    public override fun createKeyboardSwitch(context: Context): KeyboardSwitch {
        return KeyboardSwitch(context, Layout.ZHUYIN)
    }

    public override fun createEditor(): Editor {
        return ZhuyinEditor()
    }

    public override fun createWordDictionary(context: Context): WordDictionary {
        return ZhuyinDictionary(context)
    }

    override fun onCreate() {
        super.onCreate()
        keyMapping = HashMap()
        keyMapping!![8] = 0x3105
        keyMapping!![45] = 0x3106
        keyMapping!![29] = 0x3107
        keyMapping!![54] = 0x3108
        keyMapping!![9] = 0x3109
        keyMapping!![51] = 0x310A
        keyMapping!![47] = 0x310B
        keyMapping!![52] = 0x310C
        keyMapping!![33] = 0x310D
        keyMapping!![32] = 0x310E
        keyMapping!![31] = 0x310F
        keyMapping!![46] = 0x3110
        keyMapping!![34] = 0x3111
        keyMapping!![50] = 0x3112
        keyMapping!![12] = 0x3113
        keyMapping!![48] = 0x3114
        keyMapping!![35] = 0x3115
        keyMapping!![30] = 0x3116
        keyMapping!![53] = 0x3117
        keyMapping!![36] = 0x3118
        keyMapping!![42] = 0x3119
        keyMapping!![49] = 0x3127
        keyMapping!![38] = 0x3128
        keyMapping!![41] = 0x3129
        keyMapping!![15] = 0x311A
        keyMapping!![37] = 0x311B
        keyMapping!![39] = 0x311C
        keyMapping!![55] = 0x311D
        keyMapping!![16] = 0x311E
        keyMapping!![43] = 0x311F
        keyMapping!![40] = 0x3120
        keyMapping!![56] = 0x3121
        keyMapping!![7] = 0x3122
        keyMapping!![44] = 0x3123
        keyMapping!![74] = 0x3124
        keyMapping!![72] = 0x3124 // MS1 fix: KEYCODE_RIGHT_BRACKET(?) as
        // KEYCODE_SEMICOLON(;)
        keyMapping!![76] = 0x3125
        keyMapping!![69] = 0x3126
        keyMapping!![77] = 0x3126 // MS1/2 fix: KEYCODE_AT(@) as
        // KEYCODE_MINUS(-)
        keyMapping!![10] = 0x2C7
        keyMapping!![11] = 0x2CB
        keyMapping!![13] = 0x2CA
        keyMapping!![14] = 0x2D9

        // Get the setting from SharedPreferences. See if this is Milestone 3
        preferences = PreferenceManager
            .getDefaultSharedPreferences(this)
        isMS3 = preferences.getBoolean(getString(R.string.prefs_ms3_key), false)
    }

    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        showStatusIcon(keyboardSwitch.languageIcon)
        isMS3 = preferences.getBoolean(getString(R.string.prefs_ms3_key), false)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        showStatusIcon(keyboardSwitch.languageIcon)
    }

    override fun onKeyDown(keyCodeParam: Int, event: KeyEvent): Boolean {
        // Capture the hardware keyboard
        var keyCode = keyCodeParam
        if (hasHardKeyboard) {
            // Check the status
            val softKeyboard = keyboardSwitch.currentKeyboard
            if (!checkHardKeyboardAvailable(softKeyboard)) {
                return super.onKeyDown(keyCode, event)
            }

            // Shift + Space
            if (handleLanguageChange(keyCode, event)) {
                isAltUsed = false // Clear Alt status
                return true
            }

            // Handle HardKB event on Chinese mode only
            if (softKeyboard?.isChinese == true) {
                // Milestone first row key
                // (If alt is pressed before, emulate 1 - 0 keys)
                if (isAltUsed || event.isAltPressed) {
                    var isTriggered = false
                    when (keyCode) {
                        KeyEvent.KEYCODE_Q -> {
                            keyCode = KeyEvent.KEYCODE_1
                            isTriggered = true
                        }

                        KeyEvent.KEYCODE_W -> {
                            keyCode = KeyEvent.KEYCODE_2
                            isTriggered = true
                        }

                        KeyEvent.KEYCODE_E -> {
                            keyCode = KeyEvent.KEYCODE_3
                            isTriggered = true
                        }

                        KeyEvent.KEYCODE_R -> {
                            keyCode = KeyEvent.KEYCODE_4
                            isTriggered = true
                        }

                        KeyEvent.KEYCODE_T -> {
                            keyCode = KeyEvent.KEYCODE_5
                            isTriggered = true
                        }

                        KeyEvent.KEYCODE_Y -> {
                            keyCode = KeyEvent.KEYCODE_6
                            isTriggered = true
                        }

                        KeyEvent.KEYCODE_U -> {
                            keyCode = KeyEvent.KEYCODE_7
                            isTriggered = true
                        }

                        KeyEvent.KEYCODE_I -> {
                            keyCode = KeyEvent.KEYCODE_8
                            isTriggered = true
                        }

                        KeyEvent.KEYCODE_O -> {
                            keyCode = KeyEvent.KEYCODE_9
                            isTriggered = true
                        }

                        KeyEvent.KEYCODE_P -> {
                            keyCode = if (isMS3) { // MS3 fix (Alt + P = ㄦ)
                                KeyEvent.KEYCODE_MINUS
                            } else {
                                KeyEvent.KEYCODE_0
                            }
                            isTriggered = true
                        }

                        KeyEvent.KEYCODE_V -> {
                            keyCode = KeyEvent.KEYCODE_MINUS
                            isTriggered = true
                        }

                        KeyEvent.KEYCODE_COMMA, KeyEvent.KEYCODE_L -> {
                            keyCode = KeyEvent.KEYCODE_SEMICOLON
                            isTriggered = true
                        }

                        KeyEvent.KEYCODE_PERIOD -> {
                            keyCode = KeyEvent.KEYCODE_SLASH
                            isTriggered = true
                        }
                    }
                    if (isTriggered) {
                        clearKeyboardMetaState()
                        isAltUsed = false
                    } else {
                        // Pressed Alt key only
                        // Record if Alt key was pressed before
                        isAltUsed = true
                        return true
                    }
                }

                // Simulate soft keyboard press
                if (keyMapping!!.containsKey(keyCode)) {
                    onKey(keyMapping!![keyCode]!!, null)
                    return true
                }
                // Handle Delete
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    onKey(SoftKeyboard.KEYCODE_DELETE, null)
                    return true
                }
                // Handle Space
                if (keyCode == KeyEvent.KEYCODE_SPACE) {
                    onKey(SoftKeyboard.KEYCODE_SPACE, null)
                    return true
                }
                // Handle Enter
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    onKey(SoftKeyboard.KEYCODE_ENTER, null)
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}