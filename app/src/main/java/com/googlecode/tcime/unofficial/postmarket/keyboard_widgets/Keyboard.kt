package com.googlecode.tcime.unofficial.postmarket.keyboard_widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.googlecode.tcime.unofficial.postmarket.R

abstract class Keyboard(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {
    enum class Layout(val layoutResources: Int) {
        QWERTY(R.xml.qwerty),
        QWERTY_5ROWS(R.xml.qwerty_5row),
        ZHUYIN(R.xml.zhuyin),
        CANGJIE(R.xml.cangjie),
        SYMBOLS(R.xml.symbols),
    }

    abstract class Row(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
        init {
            this.orientation = HORIZONTAL
        }
    }

    abstract class Key(context: Context, attrs: AttributeSet) :
        androidx.appcompat.widget.AppCompatButton(context, attrs) {
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
}