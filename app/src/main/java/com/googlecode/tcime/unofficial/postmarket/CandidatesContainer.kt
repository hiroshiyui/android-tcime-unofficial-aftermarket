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
import android.util.AttributeSet
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.view.GestureDetectorCompat
import com.googlecode.tcime.unofficial.postmarket.CandidateView.CandidateViewListener
import kotlin.math.abs
import kotlin.math.ceil

/**
 * Contains all candidates in pages where users could move forward (next page)
 * or move backward (previous) page to select one of these candidates.
 */
class CandidatesContainer(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    lateinit var candidateView: CandidateView
    lateinit var leftArrow: ImageButton
    lateinit var rightArrow: ImageButton
    private var words: String = ""
    private var highlightDefault = false
    var currentPage = 0
    private var pageCount = 0
    private val gestureDetector = GestureDetectorCompat(context, OnGestureListener())

    fun setCandidateViewListener(
        listener: CandidateViewListener
    ) {
        candidateView.setCandidateViewListener(listener)
    }

    fun setCandidates(words: String, highlightDefault: Boolean) {
        // All the words will be split into pages and shown in the candidate-view.
        this.words = words
        this.highlightDefault = highlightDefault
        pageCount = getPageCount()
        showPage(0)
    }

    fun pickHighlighted(): Boolean {
        return candidateView.pickHighlighted()
    }

    fun showPage(page: Int) {
        if (isPageEmpty(page)) {
            candidateView.setCandidates("")
            enableArrow(leftArrow, false)
            enableArrow(rightArrow, false)
        } else {
            val start = page * CandidateView.MAX_CANDIDATE_COUNT
            val end = start + (words.length - start).coerceAtMost(CandidateView.MAX_CANDIDATE_COUNT)
            candidateView.setCandidates(words.substring(start, end))
            if (highlightDefault) {
                candidateView.highlightDefault()
            }
            // We assume that the user want to choose one word, so we highlight it.
            highlightDefault = true
            enableArrow(leftArrow, page > 0)
            enableArrow(rightArrow, page < pageCount - 1)
        }
        currentPage = page
    }

    /**
     * Change to previous candidate page.
     *
     * @return true if change page successfully
     */
    fun pagePrev(): Boolean {
        val page = currentPage - 1
        val result = !isPageEmpty(page)
        if (result) {
            showPage(page)
        }
        return result
    }

    /**
     * Change to next candidate page.
     *
     * @return true if change page successfully
     */
    fun pageNext(): Boolean {
        val page = currentPage + 1
        val result = !isPageEmpty(page)
        if (result) {
            showPage(page)
        }
        return result
    }

    /**
     * Checks if it's an empty page holding no candidates.
     */
    private fun isPageEmpty(page: Int): Boolean {
        return page < 0 || page >= pageCount

        // There are candidates in this page.
    }

    private fun getPageCount(): Int {
        return ceil(
            words.length.toDouble() / CandidateView.MAX_CANDIDATE_COUNT
        ).toInt()
    }

    private fun enableArrow(arrow: ImageButton?, enabled: Boolean) {
        arrow?.let {
            it.isEnabled = enabled
            it.imageAlpha = (if (enabled) ARROW_ALPHA_ENABLED else ARROW_ALPHA_DISABLED)
        }
    }

    /**
     * A workaround to avoid the focused CandidateView handling onTouchEvent first.
     * We let Container to handle first.
     *
     * @param event MotionEvent
     * @return true if handled
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gestureDetector.onTouchEvent(event)) {
            return true
        }
        // We can let the View to handle it then.
        // Before that, we should offset the x coordinate
        // because the coordinate is based on Container not View
        event.offsetLocation(-leftArrow.measuredWidth.toFloat(), 0f)
        candidateView.onTouchEventReal(event)
        return true
    }

    /**
     * A SimpleOnGestureListener that listens to the gesture of the user.
     */
    inner class OnGestureListener : SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            // The fling distance is not enough as a paging gesture
            e1?.apply {
                if (abs(e1.x - e2.x) < FLING_DISTANCE_THRESHOLD) {
                    return false
                }
                if (e1.x > e2.x) {
                    pageNext()
                } else {
                    pagePrev()
                }
            }
            return true
        }
    }

    fun highlightLeft() {
        if (candidateView.highlightLeft() && pagePrev()) {
            // Move the highlight to last one
            candidateView.changeHighlight(CandidateView.MAX_CANDIDATE_COUNT - 1)
        }
    }

    fun highlightRight() {
        if (candidateView.highlightRight()) pageNext()
    }

    companion object {
        private const val ARROW_ALPHA_ENABLED = 0xff
        private const val ARROW_ALPHA_DISABLED = 0x40
        private const val FLING_DISTANCE_THRESHOLD = 40
    }
}