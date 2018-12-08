package id.medigo.waluyofield

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import id.medigo.waluyofield.textview.FontCache

class WaluyoTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    init {
        applyCustomFont(context, "fonts/noto_sans_regular.ttf") // default font
    }

    private fun applyCustomFont(context: Context, fontPath: String) {
        val customFont = FontCache.getTypeface(fontPath, context)
        typeface = customFont
    }
}