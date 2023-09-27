package com.googlecode.tcime.unofficial.aftermarket.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.googlecode.tcime.unofficial.aftermarket.R

abstract class KeyboardView : LinearLayout {
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    enum class Layout(val layoutXmlResource: Int) {
        QWERTY(R.xml.qwerty),
        QWERTY_5ROWS(R.xml.qwerty_5row),
        ZHUYIN(R.xml.zhuyin),
        CANGJIE(R.xml.cangjie),
        SYMBOLS(R.xml.symbols),
    }
}