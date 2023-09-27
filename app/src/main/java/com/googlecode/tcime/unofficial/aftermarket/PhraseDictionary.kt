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
import android.database.ContentObserver
import android.provider.UserDictionary
import android.util.Log
import java.util.Arrays
import java.util.Locale
import java.util.concurrent.CountDownLatch

/**
 * Reads a phrase dictionary and provides following-word suggestions as a list
 * of characters for the given character.
 */
class PhraseDictionary(private val mContext: Context) {
    private val loading = CountDownLatch(1)
    private val loader: DictionaryLoader
    private var mObserver: ContentObserver? = null
    private var mRequiresReload = true
    private val userDic: HashMap<Char, StringBuilder>

    init {
        loader = DictionaryLoader(
            mContext.resources.openRawResource(R.raw.dict_phrases),
            APPROX_DICTIONARY_SIZE, loading
        )
        Thread(loader).start()
        val cres = mContext.contentResolver
        cres.registerContentObserver(
            UserDictionary.Words.CONTENT_URI,
            true,
            object : ContentObserver(null) {
                override fun onChange(self: Boolean) {
                    // We need to reload the user dictionary
                    mRequiresReload = true
                }
            }.also { mObserver = it })
        userDic = HashMap()
        if (mRequiresReload) userDictionary
    }

    @Synchronized
    fun close() {
        if (mObserver != null) {
            mContext.contentResolver.unregisterContentObserver(mObserver!!)
            mObserver = null
        }
    }

    /**
     * Returns a string containing the following-word suggestions of phrases for
     * the given word.
     *
     * @param c the current word to look for its following words of phrases.
     * @return a concatenated string of characters, or an empty string if there
     * is no following-word suggestions for that word.
     */
    fun getFollowingWords(c: Char): String {
        val candidateWord = StringBuilder("")
        try {
            loading.await()
        } catch (e: InterruptedException) {
            Log.e("PhraseDictionary", "Loading is interrupted: ", e)
        }

        // Check the user dictionary
        if (mRequiresReload) userDictionary
        if (userDic.containsKey(c)) {
            candidateWord.append(userDic[c].toString())
        }

        // Phrases are stored in an array consisting of three character arrays.
        // char[0][] contains a char[] of words to look for phrases.
        // char[2][] contains a char[] of following words for char[0][].
        // char[1][] contains offsets of char[0][] words to map its following words.
        // For example, there are 5 phrases: Aa, Aa', Bb, Bb', Cc.
        // char[0][] { A, B, C }
        // char[1][] { 0, 2, 4 }
        // char[2][] { a, a', b, b', c}
        val dictionary = loader.result()
        if (dictionary.size != 3) {
            return candidateWord.toString()
        }
        val index = Arrays.binarySearch(dictionary[0], c)
        if (index >= 0) {
            val offset = dictionary[1][index].code
            val count =
                if (index < dictionary[1].size - 1) dictionary[1][index + 1].code - offset else dictionary[2].size - offset
            var result = String(dictionary[2], offset, count)
            // Delete the words that already appear in the phrase file
            // = Shift these words to the front position
            if (candidateWord.isNotEmpty()) {
                result = result.replace("[$candidateWord]".toRegex(), "")
            }
            candidateWord.append(result)
        }
        return candidateWord.toString()
    }

    @get:Synchronized
    private val userDictionary: Unit
        /**
         * Load the content of user dictionary
         */
        get() {
            // Use ContentResolver to query the user dictionary
            val cursor = mContext.contentResolver
                .query(
                    UserDictionary.Words.CONTENT_URI,
                    QUERY_PROJECTION,
                    "(locale IS NULL) or (locale=?)",
                    arrayOf(
                        Locale.getDefault().toString()
                    ),
                    null
                )
            try {
                if (cursor == null) throw ClassNotFoundException("getUserDictionary failed!")
                if (cursor.moveToFirst()) {
                    userDic.clear()
                    var node: StringBuilder?
                    while (!cursor.isAfterLast) {
                        val word = cursor.getString(1)
                        val length = word.length
                        if (length < 2) break // 1 word is nonsense
                        // Recursive to build the phrase table for 3+word sentences
                        // Example: ABCD -> AB, BC, CD
                        for (i in 0 until length - 1) {
                            val index = word[i]
                            if (userDic.containsKey(index)) {
                                node = userDic[index]
                                node!!.append(word.substring(i + 1, i + 2))
                            } else {
                                node = StringBuilder(word.substring(i + 1, i + 2))
                                userDic[index] = node
                            }
                        }
                        Log.d("UserDic", "Entry " + word[0] + ": " + word.substring(1))
                        cursor.moveToNext()
                    }
                    cursor.close()
                }
            } catch (e: Exception) {
                Log.e("UserDic", e.message!!)
            } finally {
                mRequiresReload = false
            }
        }

    companion object {
        private const val APPROX_DICTIONARY_SIZE = 131072
        private val QUERY_PROJECTION = arrayOf(
            UserDictionary.Words._ID,
            UserDictionary.Words.WORD
        )
    }
}