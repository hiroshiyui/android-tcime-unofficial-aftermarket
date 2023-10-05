package com.googlecode.tcime.unofficial.aftermarket

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class PhraseDictionaryInstrumentedTest {
    private lateinit var context: Context

    @Before
    fun before() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testPhraseDictionary() {
        val phraseDictionary = PhraseDictionary(context)
        val followingWords = phraseDictionary.getFollowingWords('綠')
        assert(followingWords.contains('色'))
        assert(followingWords.contains('林'))
        Assert.assertFalse(followingWords.contains('貓'))
    }
}