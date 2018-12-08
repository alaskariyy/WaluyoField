package id.medigo.waluyofield

import android.content.Context
import android.content.res.Configuration
import android.os.Parcel
import android.os.Parcelable
import android.text.InputType
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import id.medigo.waluyofield.datepicker.*
import java.text.DateFormatSymbols
import java.util.*

/**
 * This code is rewritten and modified to kotlin from https://github.com/drawers/SpinnerDatePicker
 */

class WaluyoDatePicker @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val DEFAULT_ENABLED_STATE = true

    private var mDaySpinnerInput: EditText? = null

    private var mMonthSpinnerInput: EditText? = null

    private var mYearSpinnerInput: EditText? = null

    private var mShortMonths: Array<String?>? = null

    private var mNumberOfMonths: Int = 0

    private var mTempDate: Calendar? = null

    private var mMinDate: Calendar? = null

    private var mMaxDate: Calendar? = null

    private var mCurrentDate: Calendar? = null

    private var mIsEnabled = DEFAULT_ENABLED_STATE

    private var listener: ((monthOfYear: Int?, dayOfMonth: Int?, year: Int?) -> Unit)? = null

    val view: View by lazy {
        LayoutInflater.from(context).inflate(R.layout._date_picker, this, true)
    }

    var mDaySpinner: WaluyoNumberPicker = view.findViewById(R.id.np_day)

    var mMonthSpinner: WaluyoNumberPicker = view.findViewById(R.id.np_mont)

    var mYearSpinner: WaluyoNumberPicker = view.findViewById(R.id.np_year)

    init {
        setCurrentLocale(Locale.getDefault())
        val onChangeListener = NumberPicker.OnValueChangeListener { picker, oldVal, newVal ->
            updateInputState()
            mTempDate!!.timeInMillis = mCurrentDate!!.timeInMillis
            // take care of wrapping of days and months to update greater fields
            if (picker === mDaySpinner) {
                val maxDayOfMonth = mTempDate!!.getActualMaximum(Calendar.DAY_OF_MONTH)
                if (oldVal == maxDayOfMonth && newVal == 1) {
                    mTempDate!!.add(Calendar.DAY_OF_MONTH, 1)
                } else if (oldVal == 1 && newVal == maxDayOfMonth) {
                    mTempDate!!.add(Calendar.DAY_OF_MONTH, -1)
                } else {
                    mTempDate!!.add(Calendar.DAY_OF_MONTH, newVal - oldVal)
                }
            } else if (picker === mMonthSpinner) {
                if (oldVal == 11 && newVal == 0) {
                    mTempDate!!.add(Calendar.MONTH, 1)
                } else if (oldVal == 0 && newVal == 11) {
                    mTempDate!!.add(Calendar.MONTH, -1)
                } else {
                    mTempDate!!.add(Calendar.MONTH, newVal - oldVal)
                }
            } else if (picker === mYearSpinner) {
                mTempDate!!.set(Calendar.YEAR, newVal)
            } else {
                throw IllegalArgumentException()
            }
            // now set the date to the adjusted one
            setDate(mTempDate!!.get(Calendar.YEAR), mTempDate!!.get(Calendar.MONTH),
                    mTempDate!!.get(Calendar.DAY_OF_MONTH))
            updateSpinners()
            notifyDateChanged()
        }

        mDaySpinner.setFormatter(TwoDigitFormatter())
        mDaySpinner.setOnLongPressUpdateInterval(100)
        mDaySpinner.setOnValueChangedListener(onChangeListener)
        mDaySpinnerInput = NumberPickers.findEditText(mDaySpinner)

        // month
        mMonthSpinner.minValue = 0
        mMonthSpinner.maxValue = mNumberOfMonths - 1
        mMonthSpinner.displayedValues = mShortMonths
        mMonthSpinner.setOnLongPressUpdateInterval(200)
        mMonthSpinner.setOnValueChangedListener(onChangeListener)
        mMonthSpinnerInput = NumberPickers.findEditText(mMonthSpinner)

        // year
        mYearSpinner.setOnLongPressUpdateInterval(100)
        mYearSpinner.setOnValueChangedListener(onChangeListener)
        mYearSpinnerInput = NumberPickers.findEditText(mYearSpinner)

        // initialize to current date
        mCurrentDate!!.timeInMillis = System.currentTimeMillis()

        // re-order the number spinners to match the current date format
        reorderSpinners()

        // If not explicitly specified this view is important for accessibility.
        if (importantForAccessibility == View.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
    }

    fun init(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        setDate(year, monthOfYear, dayOfMonth)
        updateSpinners()
        notifyDateChanged()
    }

    private fun getYear(): Int? {
        return mCurrentDate?.get(Calendar.YEAR)
    }

    private fun getMonth(): Int {
        return mCurrentDate!!.get(Calendar.MONTH)
    }

    private fun getDayOfMonth(): Int {
        return mCurrentDate!!.get(Calendar.DAY_OF_MONTH)
    }

    fun setMinDate(minDate: Long) {
        mTempDate!!.timeInMillis = minDate
        if (mTempDate!!.get(Calendar.YEAR) == mMinDate!!.get(Calendar.YEAR) && mTempDate!!.get(Calendar.DAY_OF_YEAR) == mMinDate!!.get(Calendar.DAY_OF_YEAR)) {
            // Same day, no-op.
            return
        }
        mMinDate!!.timeInMillis = minDate
        if (mCurrentDate!!.before(mMinDate)) {
            mCurrentDate!!.timeInMillis = mMinDate!!.timeInMillis
        }
        updateSpinners()
    }

    fun setMaxDate(maxDate: Long) {
        mTempDate!!.timeInMillis = maxDate
        if (mTempDate!!.get(Calendar.YEAR) == mMaxDate!!.get(Calendar.YEAR) && mTempDate!!.get(Calendar.DAY_OF_YEAR) == mMaxDate!!.get(Calendar.DAY_OF_YEAR)) {
            // Same day, no-op.
            return
        }
        mMaxDate!!.timeInMillis = maxDate
        if (mCurrentDate!!.after(mMaxDate)) {
            mCurrentDate!!.timeInMillis = mMaxDate!!.timeInMillis
        }
        updateSpinners()
    }

    override fun setEnabled(enabled: Boolean) {
        mDaySpinner.isEnabled = enabled
        mMonthSpinner.isEnabled = enabled
        mYearSpinner.isEnabled = enabled
        mIsEnabled = enabled
    }

    override fun isEnabled(): Boolean {
        return mIsEnabled
    }

    public override fun onConfigurationChanged(newConfig: Configuration) {
        setCurrentLocale(newConfig.locale)
    }

    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent): Boolean {
        onPopulateAccessibilityEvent(event)
        return true
    }

    /**
     * Sets the current locale.
     *
     * @param locale The current locale.
     */
    private fun setCurrentLocale(locale: Locale) {
        mTempDate = getCalendarForLocale(mTempDate, locale)
        mMinDate = getCalendarForLocale(mMinDate, locale)
        mMaxDate = getCalendarForLocale(mMaxDate, locale)
        mCurrentDate = getCalendarForLocale(mCurrentDate, locale)

        mNumberOfMonths = mTempDate!!.getActualMaximum(Calendar.MONTH) + 1
        mShortMonths = DateFormatSymbols().shortMonths

        if (usingNumericMonths()) {
            // We're in a locale where a date should either be all-numeric, or all-text.
            // All-text would require custom NumberPicker formatters for day and year.
            mShortMonths = arrayOfNulls(mNumberOfMonths)
            for (i in 0 until mNumberOfMonths) {
                mShortMonths!![i] = String.format("%d", i + 1)
            }
        }
    }

    /**
     * Tests whether the current locale is one where there are no real month names,
     * such as Chinese, Japanese, or Korean locales.
     */
    private fun usingNumericMonths(): Boolean {
        return Character.isDigit(mShortMonths!![Calendar.JANUARY]?.get(0)!!)
    }

    /**
     * Gets a calendar for locale bootstrapped with the value of a given calendar.
     *
     * @param oldCalendar The old calendar.
     * @param locale      The locale.
     */
    private fun getCalendarForLocale(oldCalendar: Calendar?, locale: Locale): Calendar {
        return if (oldCalendar == null) {
            Calendar.getInstance(locale)
        } else {
            val currentTimeMillis = oldCalendar.timeInMillis
            val newCalendar = Calendar.getInstance(locale)
            newCalendar.timeInMillis = currentTimeMillis
            newCalendar
        }
    }

    /**
     * Reorders the spinners according to the date format that is
     * explicitly set by the user and if no such is set fall back
     * to the current locale's default format.
     */
    private fun reorderSpinners() {
        // We use numeric spinners for year and day, but textual months. Ask icu4c what
        // order the user's locale uses for that combination. http://b/7207103.
        val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMMdd")
        val order = ICU.getDateFormatOrder(pattern)
        val spinnerCount = order.size
        for (i in 0 until spinnerCount) {
            when (order[i]) {
                'd' -> {
                    setImeOptions(mDaySpinner, spinnerCount, i)
                }
                'M' -> {
                    setImeOptions(mMonthSpinner, spinnerCount, i)
                }
                'y' -> {
                    setImeOptions(mYearSpinner, spinnerCount, i)
                }
                else -> throw IllegalArgumentException(Arrays.toString(order))
            }
        }
    }

    private fun setDate(year: Int, month: Int, dayOfMonth: Int) {
        mCurrentDate!!.set(year, month, dayOfMonth)
        if (mCurrentDate!!.before(mMinDate)) {
            mCurrentDate!!.timeInMillis = mMinDate!!.timeInMillis
        } else if (mCurrentDate!!.after(mMaxDate)) {
            mCurrentDate!!.timeInMillis = mMaxDate!!.timeInMillis
        }
    }

    private fun updateSpinners() {
        // set the spinner ranges respecting the min and max dates
        when (mCurrentDate) {
            mMinDate -> {
                mDaySpinner.minValue = mCurrentDate!!.get(Calendar.DAY_OF_MONTH)
                mDaySpinner.maxValue = mCurrentDate!!.getActualMaximum(Calendar.DAY_OF_MONTH)
                mDaySpinner.wrapSelectorWheel = false
                mMonthSpinner.displayedValues = null
                mMonthSpinner.minValue = mCurrentDate!!.get(Calendar.MONTH)
                mMonthSpinner.maxValue = mCurrentDate!!.getActualMaximum(Calendar.MONTH)
                mMonthSpinner.wrapSelectorWheel = false
            }
            mMaxDate -> {
                mDaySpinner.minValue = mCurrentDate!!.getActualMinimum(Calendar.DAY_OF_MONTH)
                mDaySpinner.maxValue = mCurrentDate!!.get(Calendar.DAY_OF_MONTH)
                mDaySpinner.wrapSelectorWheel = false
                mMonthSpinner.displayedValues = null
                mMonthSpinner.minValue = mCurrentDate!!.getActualMinimum(Calendar.MONTH)
                mMonthSpinner.maxValue = mCurrentDate!!.get(Calendar.MONTH)
                mMonthSpinner.wrapSelectorWheel = false
            }
            else -> {
                mDaySpinner.minValue = 1
                mDaySpinner.maxValue = mCurrentDate!!.getActualMaximum(Calendar.DAY_OF_MONTH)
                mDaySpinner.wrapSelectorWheel = true
                mMonthSpinner.displayedValues = null
                mMonthSpinner.minValue = 0
                mMonthSpinner.maxValue = 11
                mMonthSpinner.wrapSelectorWheel = true
            }
        }

        // make sure the month names are a zero based array
        // with the months in the month spinner
        val displayedValues = Arrays.copyOfRange(mShortMonths!!,
                mMonthSpinner.minValue,
                mMonthSpinner.maxValue + 1)
        mMonthSpinner.displayedValues = displayedValues

        // year spinner range does not change based on the current date
        mYearSpinner.minValue = mMinDate!!.get(Calendar.YEAR)
        mYearSpinner.maxValue = mMaxDate!!.get(Calendar.YEAR)
        mYearSpinner.wrapSelectorWheel = false

        // set the spinner values
        mYearSpinner.value = mCurrentDate!!.get(Calendar.YEAR)
        mMonthSpinner.value = mCurrentDate!!.get(Calendar.MONTH)
        mDaySpinner.value = mCurrentDate!!.get(Calendar.DAY_OF_MONTH)

        if (usingNumericMonths()) {
            mMonthSpinnerInput!!.setRawInputType(InputType.TYPE_CLASS_NUMBER)
        }
    }


    /**
     * Notifies the listener, if such, for a change in the selected date.
     */
    private fun notifyDateChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED)
        listener?.invoke(getMonth(), getDayOfMonth(), getYear())
    }

    fun onDateChangeListener(func: ((monthOfYear: Int?, dayOfMonth: Int?, year: Int?) -> Unit)?) {
        listener = func
    }

    /**
     * Sets the IME options for a spinner based on its ordering.
     *
     * @param spinner      The spinner.
     * @param spinnerCount The total spinner count.
     * @param spinnerIndex The index of the given spinner.
     */
    private fun setImeOptions(spinner: WaluyoNumberPicker, spinnerCount: Int, spinnerIndex: Int) {
        val imeOptions = if (spinnerIndex < spinnerCount - 1) {
            EditorInfo.IME_ACTION_NEXT
        } else {
            EditorInfo.IME_ACTION_DONE
        }
        val input = NumberPickers.findEditText(spinner)
        input?.imeOptions = imeOptions
    }

    private fun updateInputState() {
        // Make sure that if the user changes the value and the IME is active
        // for one of the inputs if this widget, the IME is closed. If the user
        // changed the value via the IME and there is a next input the IME will
        // be shown, otherwise the user chose another means of changing the
        // value and having the IME up makes no sense.
        val inputMethodManager = context.getSystemService(
                Context.INPUT_METHOD_SERVICE) as InputMethodManager
        when {
            inputMethodManager.isActive(mYearSpinnerInput) -> {
                mYearSpinnerInput!!.clearFocus()
                inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
            }
            inputMethodManager.isActive(mMonthSpinnerInput) -> {
                mMonthSpinnerInput!!.clearFocus()
                inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
            }
            inputMethodManager.isActive(mDaySpinnerInput) -> {
                mDaySpinnerInput!!.clearFocus()
                inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()

        return WaluyoDatePicker.SavedState(superState, mCurrentDate!!, mMinDate!!, mMaxDate!!)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        mCurrentDate = Calendar.getInstance()
        mCurrentDate!!.timeInMillis = ss.currentDate
        mMinDate = Calendar.getInstance()
        mMinDate!!.timeInMillis = ss.minDate
        mMaxDate = Calendar.getInstance()
        mMaxDate!!.timeInMillis = ss.maxDate
        updateSpinners()
    }

    private class SavedState
    /**
     * Constructor called from [WaluyoDatePicker.onSaveInstanceState]
     */ internal constructor(superState: Parcelable?, currentDate: Calendar, minDate: Calendar, maxDate: Calendar) : View.BaseSavedState(superState) {
        internal val currentDate: Long = currentDate.timeInMillis
        internal val minDate: Long = minDate.timeInMillis
        internal val maxDate: Long = maxDate.timeInMillis

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeLong(currentDate)
            dest.writeLong(minDate)
            dest.writeLong(maxDate)
        }

    }
}