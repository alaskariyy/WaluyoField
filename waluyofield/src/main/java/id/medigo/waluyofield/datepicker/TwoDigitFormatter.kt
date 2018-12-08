package id.medigo.waluyofield.datepicker

import android.widget.NumberPicker
import java.text.DecimalFormatSymbols
import java.util.*


class TwoDigitFormatter : NumberPicker.Formatter {
    private val mBuilder = StringBuilder()

    private var mZeroDigit: Char = ' '
    private lateinit var mFmt: Formatter

    private val mArgs = arrayOfNulls<Any>(1)

    init {
        val locale = Locale.getDefault()
        init(locale)
    }

    private fun init(locale: Locale) {
        mFmt = createFormatter(locale)
        mZeroDigit = getZeroDigit(locale)
    }

    override fun format(value: Int): String {
        val currentLocale = Locale.getDefault()
        if (mZeroDigit != getZeroDigit(currentLocale)) {
            init(currentLocale)
        }
        mArgs[0] = value
        mBuilder.delete(0, mBuilder.length)
        mFmt.format("%02d", *mArgs)
        return mFmt.toString()
    }

    private fun getZeroDigit(locale: Locale): Char {
        // The original TwoDigitFormatter directly referenced LocaleData's value. Instead,
        // we need to use the public DecimalFormatSymbols API.
        return DecimalFormatSymbols.getInstance(locale).zeroDigit
    }

    private fun createFormatter(locale: Locale): java.util.Formatter {
        return java.util.Formatter(mBuilder, locale)
    }
}
