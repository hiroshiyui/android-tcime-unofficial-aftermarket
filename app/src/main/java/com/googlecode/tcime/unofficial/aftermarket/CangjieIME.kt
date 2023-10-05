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
import android.content.res.Configuration
import android.inputmethodservice.Keyboard
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import com.googlecode.tcime.unofficial.aftermarket.widgets.KeyboardView.Layout

/**
 * Cangjie input method.
 */
class CangjieIME : AbstractIME() {
    private lateinit var keyMapping: HashMap<Int, Int>
    private lateinit var cangjieEditor: CangjieEditor
    private lateinit var cangjieDictionary: CangjieDictionary
    public override fun createKeyboardSwitch(context: Context): KeyboardSwitch {
        return KeyboardSwitch(context, Layout.CANGJIE)
    }

    public override fun createEditor(): Editor {
        cangjieEditor = CangjieEditor()
        return cangjieEditor
    }

    public override fun createWordDictionary(context: Context): WordDictionary {
        cangjieDictionary = CangjieDictionary(context)
        return cangjieDictionary
    }

    override fun onCreate() {
        super.onCreate()
        keyMapping = HashMap()
        keyMapping[KeyEvent.KEYCODE_Q] = 25163
        keyMapping[KeyEvent.KEYCODE_W] = 30000
        keyMapping[KeyEvent.KEYCODE_E] = 27700
        keyMapping[KeyEvent.KEYCODE_R] = 21475
        keyMapping[KeyEvent.KEYCODE_T] = 24319
        keyMapping[KeyEvent.KEYCODE_Y] = 21340
        keyMapping[KeyEvent.KEYCODE_U] = 23665
        keyMapping[KeyEvent.KEYCODE_I] = 25096
        keyMapping[KeyEvent.KEYCODE_O] = 20154
        keyMapping[KeyEvent.KEYCODE_P] = 24515
        keyMapping[KeyEvent.KEYCODE_A] = 26085
        keyMapping[KeyEvent.KEYCODE_S] = 23608
        keyMapping[KeyEvent.KEYCODE_D] = 26408
        keyMapping[KeyEvent.KEYCODE_F] = 28779
        keyMapping[KeyEvent.KEYCODE_G] = 22303
        keyMapping[KeyEvent.KEYCODE_H] = 31481
        keyMapping[KeyEvent.KEYCODE_J] = 21313
        keyMapping[KeyEvent.KEYCODE_K] = 22823
        keyMapping[KeyEvent.KEYCODE_L] = 20013

        //keyMapping.put(KeyEvent.KEYCODE_Z, 0);
        keyMapping[KeyEvent.KEYCODE_X] = 38627
        keyMapping[KeyEvent.KEYCODE_C] = 37329
        keyMapping[KeyEvent.KEYCODE_V] = 22899
        keyMapping[KeyEvent.KEYCODE_B] = 26376
        keyMapping[KeyEvent.KEYCODE_N] = 24339
        keyMapping[KeyEvent.KEYCODE_M] = 19968
    }

    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        var ico = keyboardSwitch.languageIcon
        if (ico == R.drawable.ime_ch && cangjieEditor.simplified) {
            ico = R.drawable.ime_chsp
        }
        showStatusIcon(ico)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        var ico = keyboardSwitch.languageIcon
        if (ico == R.drawable.ime_ch && cangjieEditor.simplified) {
            ico = R.drawable.ime_chsp
        }
        showStatusIcon(ico)
    }

    override fun onKeyDown(keyCodeParam: Int, event: KeyEvent): Boolean {
        // Capture the hardware keyboard
        if (hasHardKeyboard) {
            // Check the status
            val softKeyboard = keyboardSwitch.currentKeyboard as SoftKeyboard
            if (!checkHardKeyboardAvailable(softKeyboard)) {
                return super.onKeyDown(keyCodeParam, event)
            }

            // Shift + Space
            if (handleLanguageChange(keyCodeParam, event)) {
                // Determine if it is simplified cangjie
                // Because the softKeyboard we got is old. The Cangjie keyboard should be English after handleShiftSpacekey().
                val isCangjie = !softKeyboard.isCangjie
                if (isCangjie && cangjieEditor.simplified) showStatusIcon(R.drawable.ime_chsp)
                return true
            }

            // Handle HardKB event on Chinese mode only
            if (softKeyboard.isChinese) {
                // Simulate soft keyboard press
                if (keyMapping.containsKey(keyCodeParam)) {
                    onKey(keyMapping[keyCodeParam]!!, null)
                    return true
                }
                // Handle Alt (As Cangjie simplified switch)
                if (event.isAltPressed) {
                    clearKeyboardMetaState()
                    onKey(Keyboard.KEYCODE_SHIFT, null)
                    showStatusIcon(if (cangjieEditor.simplified) R.drawable.ime_chsp else R.drawable.ime_ch)
                    return true
                }
                // Handle Delete
                if (keyCodeParam == KeyEvent.KEYCODE_DEL) {
                    onKey(SoftKeyboard.KEYCODE_DELETE, null)
                    return true
                }
                // Handle Space
                if (keyCodeParam == KeyEvent.KEYCODE_SPACE) {
                    onKey(SoftKeyboard.KEYCODE_SPACE, null)
                    return true
                }
                // Handle Enter
                if (keyCodeParam == KeyEvent.KEYCODE_ENTER) {
                    onKey(SoftKeyboard.KEYCODE_ENTER, null)
                    return true
                }
            }
        }
        return super.onKeyDown(keyCodeParam, event)
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        if (handleCangjieSimplified(primaryCode)) {
            return
        }
        super.onKey(primaryCode, keyCodes)
    }

    private fun handleCangjieSimplified(keyCode: Int): Boolean {
        if (keyCode == Keyboard.KEYCODE_SHIFT) {
            if (inputView.toggleCangjieSimplified()) {
                val isCangjieSimplified = inputView.isCangjieSimplified
                cangjieEditor.simplified = isCangjieSimplified
                cangjieDictionary.simplified = isCangjieSimplified
                escape()
                return true
            }
        }
        return false
    }
}