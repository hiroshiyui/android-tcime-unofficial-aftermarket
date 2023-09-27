package com.googlecode.tcime.unofficial.aftermarket.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.googlecode.tcime.unofficial.aftermarket.R

abstract class KeyboardKey(context: Context, attrs: AttributeSet) : AppCompatButton(context, attrs) {
    var keyCodeString: String? = null
    var keyLabel: String? = null
    var keyShiftedLabel: String? = null
    var keySymbol: String? = null
    var keyShiftedSymbol: String? = null

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.Key, 0, 0).apply {
            try {
                keyCodeString = this.getString(R.styleable.Key_keyCodeString)
                keyLabel = this.getString(R.styleable.Key_keyLabelString)
                keyShiftedLabel = this.getString(R.styleable.Key_keyShiftedLabelString)
                keySymbol = this.getString(R.styleable.Key_keySymbolString)
                keyShiftedSymbol = this.getString(R.styleable.Key_keyShiftedSymbolString)
            } finally {
                recycle()
            }
        }
    }
}