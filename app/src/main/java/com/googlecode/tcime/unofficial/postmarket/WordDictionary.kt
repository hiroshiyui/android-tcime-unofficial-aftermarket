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
import android.util.Log
import java.util.concurrent.CountDownLatch

/**
 * Reads a word-dictionary and provides word-suggestions as a list of characters
 * for the specified input.
 */
abstract class WordDictionary protected constructor(
    context: Context, dictionaryId: Int, approxDictionarySize: Int
) {
    private val loading = CountDownLatch(1)
    private val loader: DictionaryLoader

    init {
        loader = DictionaryLoader(
            context.resources.openRawResource(dictionaryId),
            approxDictionarySize, loading
        )
        Thread(loader).start()
    }

    protected fun dictionary(): Array<CharArray> {
        try {
            loading.await()
        } catch (e: InterruptedException) {
            Log.e("WordDictionary", "Loading is interrupted: ", e)
        }
        return loader.result()
    }

    /**
     * Returns a string containing words as suggestions for the specified input.
     *
     * @param input should not be null.
     * @return a concatenated string of characters, or an empty string if there
     * is no word for that input.
     */
    abstract fun getWords(input: CharSequence?): String?
}