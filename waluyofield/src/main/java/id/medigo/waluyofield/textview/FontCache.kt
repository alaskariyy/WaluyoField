package id.medigo.waluyofield.textview

import android.content.Context
import android.graphics.Typeface
import java.util.*

object FontCache {

    private val fontCache = HashMap<String, Typeface>()

    fun getTypeface(fontPath: String, context: Context) : Typeface? {
        var typeface = fontCache.get(fontPath)

        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.assets, fontPath)
            } catch (e: Exception) {
                return null
            }

            fontCache[fontPath] = typeface
        }

        return typeface
    }

}