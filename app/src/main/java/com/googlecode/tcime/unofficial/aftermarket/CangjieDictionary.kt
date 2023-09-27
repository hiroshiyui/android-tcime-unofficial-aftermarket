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
import java.text.Collator
import java.util.Arrays
import java.util.Locale

/**
 * Extends WordDictionary to provide cangjie word-suggestions.
 */
class CangjieDictionary(context: Context) :
    WordDictionary(context, R.raw.dict_cangjie, APPROX_DICTIONARY_SIZE) {
    var simplified = false
    private val collator = Collator.getInstance(Locale.TRADITIONAL_CHINESE)

    override fun getWords(input: CharSequence?): String {
        // Look up the index in the dictionary for the specified input.
        val primaryIndex = input?.let { CangjieTable.getPrimaryIndex(it) }
        if (primaryIndex != null) {
            if (primaryIndex < 0) {
                return ""
            }
        }

        // [25 * 26] char[] array; each primary entry points to a char[]
        // containing words with the same primary index; then words can be looked up
        // by their secondary index stored at the beginning of each char[].
        // For example, the first primary entry is for '日' code and looks like:
        // char[0][]: { 0, 0, '日', '曰' }
        val dictionary = dictionary()
        val data = primaryIndex?.let { dictionary[it] } ?: return ""
        if (simplified) {
            // Sort words of this primary index for simplified-cangjie.
            return sortWords(data)
        }
        val secondaryIndex = CangjieTable.getSecondaryIndex(input)
        return if (secondaryIndex < 0) {
            ""
        } else searchWords(secondaryIndex, data)
        // Find words match this secondary index for cangjie.
    }

    private fun sortWords(data: CharArray): String {
        val length = data.size / 2
        val keys = arrayOfNulls<String>(length)
        for (i in 0 until length) {
            keys[i] = data[length + i].toString()
        }
        Arrays.sort(keys, collator)
        val sorted = CharArray(length)
        for (i in 0 until length) {
            sorted[i] = keys[i]!![0]
        }
        return String(sorted)
    }

    private fun searchWords(secondaryIndex: Int, data: CharArray): String {
        val length = data.size / 2
        val i = binarySearch(data, 0, length, secondaryIndex.toChar())
        if (i < 0) {
            return ""
        }
        // There may be more than one words with the same index; look up words with
        // the same secondary index.
        var start = i
        while (start > 0) {
            if (data[start - 1] != secondaryIndex.toChar()) {
                break
            }
            start--
        }
        var end = i + 1
        while (end < length) {
            if (data[end] != secondaryIndex.toChar()) {
                break
            }
            end++
        }
        return String(data, start + length, end - start)
    }

    companion object {
        private const val APPROX_DICTIONARY_SIZE = 65536

        /**
         * Binary-searches a range of the specified array for the specified value.
         *
         * @param fromIndex: index of the first element (inclusive) to be searched.
         * @param toIndex:   index of the last element (exclusive) to be searched.
         * @return -1 if the value isn't found.
         * TODO: Remove this once Arrays binarySearch supports search within a range.
         */
        private fun binarySearch(
            array: CharArray, fromIndex: Int, toIndex: Int, value: Char
        ): Int {
            var low = fromIndex
            var mid = -1
            var high = toIndex - 1
            while (low <= high) {
                mid = low + high shr 1
                if (value > array[mid]) {
                    low = mid + 1
                } else if (value == array[mid]) {
                    return mid
                } else {
                    high = mid - 1
                }
            }
            return -1
        }
    }
}