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

import android.inputmethodservice.Keyboard
import android.text.InputType
import android.view.inputmethod.InputConnection

/**
 * Updates the editing field and handles composing-text.
 */
abstract class Editor {
    protected var composingText = StringBuilder()
    private var canCompose = false
    private var enterAsLineBreak = false
    fun composingText(): CharSequence {
        return composingText
    }

    fun hasComposingText(): Boolean {
        return composingText.isNotEmpty()
    }

    /**
     * Resets the internal state of this editor, typically called when a new input
     * session commences.
     */
    fun start(inputType: Int) {
        composingText.setLength(0)
        canCompose = true
        enterAsLineBreak = false
        when (inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_DATETIME, InputType.TYPE_CLASS_PHONE ->                 // Composing is disabled for number, date-time, and phone input types.
                canCompose = false

            InputType.TYPE_CLASS_TEXT -> {
                val variation = inputType and InputType.TYPE_MASK_VARIATION
                if (variation == InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE) {
                    // Make enter-key as line-breaks for messaging.
                    enterAsLineBreak = true
                }
            }
        }
    }

    fun clearComposingText(ic: InputConnection?) {
        if (hasComposingText()) {
            // Clear composing only when there's composing-text to avoid the selected
            // text being cleared unexpectedly.
            composingText.setLength(0)
            updateComposingText(ic)
        }
    }

    private fun updateComposingText(ic: InputConnection?) {
        ic?.setComposingText(composingText, 1)
    }

    private fun deleteLastComposingChar(ic: InputConnection): Boolean {
        if (hasComposingText()) {
            // Delete-key are accepted only when there's text in composing.
            composingText.deleteCharAt(composingText.length - 1)
            updateComposingText(ic)
            return true
        }
        return false
    }

    /**
     * Commits the given text to the editing field.
     */
    fun commitText(ic: InputConnection?, text: CharSequence): Boolean {
        ic?.apply {
            if (text.length > 1) {
                // Batch edit a sequence of characters.
                beginBatchEdit()
                commitText(text, 1)
                endBatchEdit()
            } else {
                commitText(text, 1)
            }
            // Composing-text in the editor has been cleared.
            composingText.setLength(0)
            return true
        }
        return false
    }

    fun treatEnterAsLinkBreak(): Boolean {
        return enterAsLineBreak
    }

    /**
     * Composes the composing-text further with the specified key-code.
     *
     * @return `true` if the key is handled and consumed for composing.
     */
    fun compose(ic: InputConnection, keyCode: Int): Boolean {
        if (keyCode == Keyboard.KEYCODE_DELETE) {
            return deleteLastComposingChar(ic)
        }
        if (canCompose && doCompose(keyCode)) {
            updateComposingText(ic)
            return true
        }
        return false
    }

    protected abstract fun doCompose(keyCode: Int): Boolean
}