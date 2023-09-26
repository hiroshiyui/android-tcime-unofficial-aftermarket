package com.googlecode.tcime.unofficial.postmarket.keyboard_widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

abstract class KeyboardView : LinearLayout {
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
}