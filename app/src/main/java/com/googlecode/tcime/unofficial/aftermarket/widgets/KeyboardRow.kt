package com.googlecode.tcime.unofficial.aftermarket.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

abstract class KeyboardRow(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    init {
        this.orientation = HORIZONTAL
    }
}