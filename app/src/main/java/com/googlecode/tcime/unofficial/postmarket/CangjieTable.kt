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

/**
 * Defines cangjie letters and calculates the index of the given cangjie code.
 */
internal object CangjieTable {
    // Cangjie 25 letters with number-index starting from 1:
    // 日月金木水火土竹戈十大中一弓人心手口尸廿山女田難卜
    private val letters = HashMap<Char, Int>()

    init {
        var i = 1
        letters['\u65e5'] = i++
        letters['\u6708'] = i++
        letters['\u91d1'] = i++
        letters['\u6728'] = i++
        letters['\u6c34'] = i++
        letters['\u706b'] = i++
        letters['\u571f'] = i++
        letters['\u7af9'] = i++
        letters['\u6208'] = i++
        letters['\u5341'] = i++
        letters['\u5927'] = i++
        letters['\u4e2d'] = i++
        letters['\u4e00'] = i++
        letters['\u5f13'] = i++
        letters['\u4eba'] = i++
        letters['\u5fc3'] = i++
        letters['\u624b'] = i++
        letters['\u53e3'] = i++
        letters['\u5c38'] = i++
        letters['\u5eff'] = i++
        letters['\u5c71'] = i++
        letters['\u5973'] = i++
        letters['\u7530'] = i++
        letters['\u96e3'] = i++
        letters['\u535c'] = i++
    }

    // Cangjie codes contain at most five letters. A cangjie code can be
    // converted to a numerical code by the number-index of each letter.
    // The absent letter will be indexed as 0 if the cangjie code contains less
    // than five-letters.
    const val MAX_CODE_LENGTH = 5
    const val MAX_SIMPLIFIED_CODE_LENGTH = 2
    private val BASE_NUMBER = letters.size + 1

    /**
     * Returns `true` only if the given character is a valid cangjie letter.
     */
    fun isLetter(c: Char): Boolean {
        return letters.containsKey(c)
    }

    /**
     * Returns the primary index calculated by the first and last letter of
     * the given cangjie code.
     *
     * @param code should not be null.
     * @return -1 for invalid code.
     */
    fun getPrimaryIndex(code: CharSequence): Int {
        val length = code.length
        if (length < 1 || length > MAX_CODE_LENGTH) {
            return -1
        }
        var c = code[0]
        if (!isLetter(c)) {
            return -1
        }
        // The first letter cannot be absent in the code; therefore, the numerical
        // index of the first letter starts from 0 instead.
        val index = (letters[c]!! - 1) * BASE_NUMBER
        if (length < 2) {
            return index
        }
        c = code[length - 1]
        return if (!isLetter(c)) {
            -1
        } else index + letters[c]!!
    }

    /**
     * Returns the secondary index calculated by letters between the first and
     * last letter of the given cangjie code.
     *
     * @param code should not be null.
     * @return -1 for invalid code.
     */
    fun getSecondaryIndex(code: CharSequence): Int {
        var index = 0
        val last = code.length - 1
        for (i in 1 until last) {
            val c = code[i]
            if (!isLetter(c)) {
                return -1
            }
            index = index * BASE_NUMBER + letters[c]!!
        }
        val maxEnd = MAX_CODE_LENGTH - 1
        for (i in last until maxEnd) {
            index *= BASE_NUMBER
        }
        return index
    }
}