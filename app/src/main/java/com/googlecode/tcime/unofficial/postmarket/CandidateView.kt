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
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat

/**
 * View to show candidate words.
 */
class CandidateView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    /**
     * Listens to candidate-view actions.
     */
    interface CandidateViewListener {
        fun onPickCandidate(candidate: String?)
    }

    private lateinit var listener: CandidateViewListener
    private var candidates = ""
    private var highlightIndex = 0
    private val candidateHighlight: Drawable?
    private val candidateSeparator: Drawable?
    private val paint: Paint
    private val candidateRect = arrayOfNulls<Rect>(MAX_CANDIDATE_COUNT)

    init {
        val r = context.resources
        candidateHighlight = ResourcesCompat.getDrawable(resources, R.drawable.candidate_highlight, null)
        candidateSeparator = ResourcesCompat.getDrawable(resources, R.drawable.candidate_separator, null)
        paint = Paint()
        paint.color = ResourcesCompat.getColor(resources, R.color.candidate_normal, null)
        paint.isAntiAlias = true
        paint.textSize = r.getDimensionPixelSize(R.dimen.candidate_font_height).toFloat()
        paint.strokeWidth = 0f
        setWillNotDraw(false)
    }

    fun setCandidateViewListener(listener: CandidateViewListener) {
        this.listener = listener
    }

    /**
     * Sets the candidates being shown, and the view will be updated accordingly.
     *
     * @param candidates a string contains 0 to MAX_CANDIDATE_COUNT candidates.
     */
    fun setCandidates(candidates: String) {
        this.candidates = candidates
        removeHighlight()
    }

    /**
     * Highlight the first candidate as the default candidate.
     */
    fun highlightDefault() {
        if (candidates.isNotEmpty()) {
            changeHighlight(0)
        }
    }

    /**
     * Direct change the highlight index.
     *
     * @param index a value that should be control in range by yourself
     */
    fun changeHighlight(index: Int) {
        highlightIndex = index
        invalidate()
    }

    /**
     * Move the highlight to previous one.
     *
     * @return true if the index reaches the left bound (needs previous page)
     */
    fun highlightLeft(): Boolean {
        if (candidates.isNotEmpty() && highlightIndex > 0) {
            changeHighlight(highlightIndex - 1)
        } else if (highlightIndex == 0) {
            return true
        }
        return false
    }

    /**
     * Move the highlight to next one.
     *
     * @return true if the index reaches the right bound (needs next page)
     */
    fun highlightRight(): Boolean {
        val max = candidates.length
        if (max > 0 && highlightIndex < max - 1) {
            changeHighlight(highlightIndex + 1)
            invalidate()
        } else if (highlightIndex == max - 1) {
            return true
        }
        return false
    }

    /**
     * Picks the highlighted candidate.
     *
     * @return `false` if no candidate is highlighted and picked.
     */
    fun pickHighlighted(): Boolean {
        if (highlightIndex >= 0) {
            listener.onPickCandidate(getCandidate(highlightIndex))
            return true
        }
        return false
    }

    private fun updateHighlight(x: Int, y: Int): Boolean {
        val index = getCandidateIndex(x, y)
        if (index >= 0) {
            changeHighlight(index)
            return true
        }
        return false
    }

    private fun removeHighlight() {
        changeHighlight(-1)
        requestLayout()
    }

    private fun drawHighlight(canvas: Canvas) {
        if (highlightIndex >= 0) {
            candidateHighlight?.apply {
                bounds = candidateRect[highlightIndex]!!
                draw(canvas)
            }
        }
    }

    private fun drawCandidates(canvas: Canvas) {
        val count = candidates.length
        if (count > 0) {
            // Draw the separator at the left edge of the first candidate.
            candidateSeparator?.apply {
                setBounds(
                    candidateRect[0]!!.left,
                    candidateRect[0]!!.top,
                    candidateRect[0]!!.left + this.intrinsicWidth,
                    candidateRect[0]!!.bottom
                )
                draw(canvas)
            }
        }
        val y = ((height - paint.textSize) / 2 - paint.ascent()).toInt()
        for (i in 0 until count) {
            // Calculate a position where the text could be centered in the rectangle.
            val candidate = getCandidate(i)
            val x = ((candidateRect[i]!!.left + candidateRect[i]!!.right -
                    paint.measureText(candidate)) / 2).toInt().toFloat()
            canvas.drawText(candidate, x, y.toFloat(), paint)

            // Draw the separator at the right edge of each candidate.
            candidateSeparator?.apply {
                setBounds(
                    candidateRect[i]!!.right - this.intrinsicWidth,
                    candidateRect[i]!!.top,
                    candidateRect[i]!!.right,
                    candidateRect[i]!!.bottom
                )
                draw(canvas)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawHighlight(canvas)
        drawCandidates(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val top = 0
        val candidateWidth = w / MAX_CANDIDATE_COUNT

        // Set the first candidate 1-pixel wider since it'd accommodate two
        // candidate-separators.
        candidateRect[0] = Rect(0, top, candidateWidth + 1, h)
        var i = 1
        var x = candidateRect[0]!!.right
        while (i < MAX_CANDIDATE_COUNT) {
            candidateRect[i] = Rect(x, top, candidateWidth.let { x += it; x }, h)
            i++
        }
    }

    /**
     * Let the Container handle the onTouchEvent first.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }

    /**
     * A workaround to avoid the focused CandidateView handling onTouchEvent first.
     * We let Container to handle first.
     *
     * @param me MotionEvent
     * @return true if handled
     */
    fun onTouchEventReal(me: MotionEvent): Boolean {
        val action = me.action
        val x = me.x.toInt()
        val y = me.y.toInt()
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> updateHighlight(x, y)
            MotionEvent.ACTION_UP -> if (updateHighlight(x, y)) {
                pickHighlighted()
            }
        }
        return true
    }

    /**
     * Returns the index of the candidate which the given coordinate points to.
     *
     * @return -1 if no candidate is mapped to the given (x, y) coordinate.
     */
    private fun getCandidateIndex(x: Int, y: Int): Int {
        val r = Rect()
        for (i in 0 until MAX_CANDIDATE_COUNT) {
            if (candidateRect[i] != null) {
                // Enlarge the rectangle to be more responsive to user clicks.
                r.set(candidateRect[i]!!)
                r.inset(0, CANDIDATE_TOUCH_OFFSET)
                if (r.contains(x, y)) {
                    // Returns -1 if there is no candidate in the hitting rectangle.
                    return if (i < candidates.length) i else -1
                }
            }
        }
        return -1
    }

    /**
     * Returns the candidate by the given candidate index.
     *
     * @param index should be >= 0 and < candidates.length().
     */
    private fun getCandidate(index: Int): String {
        return candidates.substring(index, index + 1)
    }

    companion object {
        const val MAX_CANDIDATE_COUNT = 6
        private const val CANDIDATE_TOUCH_OFFSET = -12
    }
}