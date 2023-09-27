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

/**
 * Extends WordDictionary to provide zhuyin word-suggestions.
 */
class ZhuyinDictionary(context: Context) :
    WordDictionary(context, R.raw.dict_zhuyin, APPROX_DICTIONARY_SIZE) {
    override fun getWords(input: CharSequence?): String {
        // Look up the syllables index; return empty string for invalid syllables.
        val pair = ZhuyinTable.stripTones(input.toString())
        val syllablesIndex = if (pair != null) ZhuyinTable.getSyllablesIndex(pair[0]) else -1
        if (syllablesIndex < 0) {
            return ""
        }

        // [22-initials * 39-finals] syllables array; each syllables entry points to
        // a char[] containing words for that syllables.
        val dictionary = dictionary()
        val data = (dictionary[syllablesIndex]) ?: return ""

        // Counts of words for each tone are stored in the array beginning.
        var tone = ZhuyinTable.getTones(pair!![1][0])
        var length = 0
        // Default tone: show first available tone group words for selecting
        // If first tone not found, find second tone. If second not found, find third... Unless there's one tone available.
        if (tone == 0) {
            tone = 0
            while (tone < TONES_COUNT) {
                length = data[tone].code
                // Found one tone available
                if (length > 0) {
                    break
                }
                tone++
            }
            // Now tone = one available tone (May not be first tone)
        } else {
            length = data[tone].code
        }
        if (length == 0) {
            return ""
        }
        var start = TONES_COUNT
        for (i in 0 until tone) {
            start += data[i].code
        }
        return String(data, start, length)
    }

    companion object {
        private const val APPROX_DICTIONARY_SIZE = 65536
        private val TONES_COUNT = ZhuyinTable.getTonesCount()
    }
}