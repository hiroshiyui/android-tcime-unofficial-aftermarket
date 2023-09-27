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

/**
 * Extends Editor to compose by cangjie rules.
 */
class CangjieEditor : Editor() {
    var simplified: Boolean = false

    /**
     * Composes the key-code into the composing-text by cangjie composing rules.
     */
    public override fun doCompose(keyCode: Int): Boolean {
        val c = keyCode.toChar()
        if (!CangjieTable.isLetter(c)) {
            return false
        }
        val maxLength =
            if (simplified) CangjieTable.MAX_SIMPLIFIED_CODE_LENGTH else CangjieTable.MAX_CODE_LENGTH
        if (composingText.length >= maxLength) {
            // Handle the key-code with no-op.
            return true
        }
        composingText.append(c)
        return true
    }
}