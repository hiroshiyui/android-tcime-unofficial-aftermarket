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

internal object ZhuyinTable {
    // All Chinese characters are mapped into a zhuyin table as described in
    // http://en.wikipedia.org/wiki/Zhuyin_table.
    private const val INITIALS_SIZE = 22
    const val DEFAULT_TONE = ' '

    // Finals that can be appended after 'ㄧ' (yi), 'ㄨ' (wu), or 'ㄩ' (yu).
    private val yiEndingFinals = charArrayOf(
        '\u311a', '\u311b', '\u311d', '\u311e', '\u3120', '\u3121', '\u3122',
        '\u3123', '\u3124', '\u3125'
    )
    private val wuEndingFinals = charArrayOf(
        '\u311a', '\u311b', '\u311e', '\u311f', '\u3122', '\u3123', '\u3124',
        '\u3125'
    )
    private val yuEndingFinals = charArrayOf('\u311d', '\u3122', '\u3123', '\u3125')

    // 'ㄧ' (yi) finals start from position 14 and are followed by 'ㄨ' (wu)
    // finals, and 'ㄩ' (yu) finals follow after 'ㄨ' (wu) finals.
    private const val YI_FINALS_INDEX = 14
    private const val WU_FINALS_INDEX = 25
    private const val YU_FINALS_INDEX = 34

    // 'ㄧ' (yi), 'ㄨ' (wu) , and 'ㄩ' (yu) finals.
    private const val YI_FINALS = '\u3127'
    private const val WU_FINALS = '\u3128'
    private const val YU_FINALS = '\u3129'

    // Default tone and four tone symbols: '˙', 'ˊ', 'ˇ', and 'ˋ'.
    private val tones = charArrayOf(DEFAULT_TONE, '\u02d9', '\u02ca', '\u02c7', '\u02cb')

    /**
     * Returns the row-index in the zhuyin table for the given initials.
     *
     * @return [0, INITIALS_SIZE - 1] for valid initials index; otherwise -1.
     */
    fun getInitials(initials: Char): Int {
        // Calculate the index by its distance to the first initials 'ㄅ' (b).
        val index = initials.code - '\u3105'.code + 1
        if (index >= INITIALS_SIZE) {
            // Syllables starting with finals can still be valid.
            return 0
        }
        return if (index >= 0) index else -1
    }

    /**
     * Returns the column-index in the zhuyin table for the given finals.
     *
     * @return a negative value for invalid finals.
     */
    fun getFinals(finals: String): Int {
        if (finals.isEmpty()) {
            // Syllables ending with no finals can still be valid.
            return 0
        }
        if (finals.length > 2) {
            return -1
        }

        // Compute the index instead of direct lookup the whole array to save
        // traversing time. First calculate the distance to the first finals
        // 'ㄚ' (a).
        var index = finals[0].code - '\u311a'.code + 1
        if (index < YI_FINALS_INDEX) {
            return index
        }

        // Check 'ㄧ' (yi), 'ㄨ' (wu) , and 'ㄩ' (yu) group finals.
        val endingFinals: CharArray
        when (finals[0]) {
            YI_FINALS -> {
                index = YI_FINALS_INDEX
                endingFinals = yiEndingFinals
            }

            WU_FINALS -> {
                index = WU_FINALS_INDEX
                endingFinals = wuEndingFinals
            }

            YU_FINALS -> {
                index = YU_FINALS_INDEX
                endingFinals = yuEndingFinals
            }

            else -> return -1
        }
        if (finals.length == 1) {
            return index
        }
        for (i in endingFinals.indices) {
            if (finals[1] == endingFinals[i]) {
                return index + i + 1
            }
        }
        return -1
    }

    /**
     * Returns the index in the dictionary for the given syllables.
     *
     * @param syllables should not be null or an empty string.
     * @return a negative value for invalid syllables.
     */
    fun getSyllablesIndex(syllables: String): Int {
        val initials = getInitials(syllables[0])
        if (initials < 0) {
            return -1
        }

        // Strip out initials before getting finals column-index.
        val finals = getFinals(
            if (initials != 0) syllables.substring(1) else syllables
        )
        return if (finals < 0) {
            -1
        } else finals * INITIALS_SIZE + initials
    }

    /**
     * Returns the tone index for the given character.
     */
    fun getTones(c: Char): Int {
        for (i in tones.indices) {
            if (tones[i] == c) {
                return i
            }
        }
        // Treat all other characters as the default tone with the index 0.
        return 0
    }

    /**
     * Returns the count of available tones.
     */
    fun getTonesCount(): Int {
        return tones.size
    }

    /**
     * Checks if the character is one of the four tone marks.
     */
    fun isTone(c: Char): Boolean {
        for (i in tones.indices) {
            if (tones[i] == c) {
                return true
            }
        }
        return false
    }

    fun isYiWuYuFinals(c: Char): Boolean {
        when (c) {
            YI_FINALS, WU_FINALS, YU_FINALS -> return true
        }
        return false
    }

    /**
     * Strips the input into two parts: syllables and its tone.
     *
     * @return the first element as the syllables and second element as the tone;
     * null if the input couldn't be stripped into two parts.
     */
    fun stripTones(input: String): Array<String>? {
        val last = input.length - 1
        if (last < 0) {
            return null
        }
        val tone = input[last]
        if (isTone(tone)) {
            val syllables = input.substring(0, last)
            return if (syllables.isEmpty()) {
                null
            } else arrayOf(syllables, tone.toString())
        }
        // Treat the tone-less input as the default tone (tone-0).
        return arrayOf(input, DEFAULT_TONE.toString())
    }
}