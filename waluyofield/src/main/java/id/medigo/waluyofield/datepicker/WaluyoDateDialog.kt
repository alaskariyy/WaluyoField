package id.medigo.waluyofield.datepicker

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import id.medigo.waluyofield.R
import id.medigo.waluyofield.WaluyoDatePicker
import id.medigo.waluyofield.WaluyoTextView
import id.medigo.waluyofield.utils.BaseDialogHelper
import org.jetbrains.anko.textAppearance
import java.util.*

@SuppressLint("InflateParams")
class WaluyoDateDialog(context: Context,
                       defaultDate: Calendar = GregorianCalendar(1980, 0, 1),
                       minDate: Calendar = GregorianCalendar(1900, 0, 1),
                       maxDate: Calendar = GregorianCalendar(2100, 0, 1)): BaseDialogHelper() {

    var wdp: WaluyoDatePicker
    private var year: Int? = 0
    private var month: Int? = 0
    private var day: Int? = 0

    //  dialog view
    override val dialogView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.dialog_date_picker, null)
    }

    override val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(dialogView)

    init {
        wdp = dialogView.findViewById(R.id.wdp)
        with(wdp){
            setMinDate(minDate.timeInMillis)
            setMaxDate(maxDate.timeInMillis)
            init(defaultDate.get(Calendar.YEAR), defaultDate.get(Calendar.MONTH), defaultDate.get(Calendar.DAY_OF_MONTH))
        }
        wdp.onDateChangeListener { monthOfYear, dayOfMonth, year ->
            this.year = year
            this.day = dayOfMonth
            this.month = monthOfYear
        }
    }

    fun getEngMonthName(month: Int?): String {
        val months = arrayOf("-","Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        return months[month?: 0]
    }

    //  Header textView
    private val tvHeader: WaluyoTextView by lazy {
        dialogView.findViewById<WaluyoTextView>(R.id.ctv_header_label)
    }

    //  finish button
    private val finishButton: Button by lazy {
        dialogView.findViewById<Button>(R.id.btn_finish)
    }

    private val divider: View by lazy {
        dialogView.findViewById<View>(R.id.div)
    }

    var headerText: String = "$day "+getEngMonthName(month)+" $year"
        set(value) {
            field = value
            updateHeaderTextAttribute()
        }

    // Header Text Color (Default value is Title.White)
    var headerTextStyle: Int = R.style.Title_White
        set(value) {
            field = value
            updateHeaderTextAttribute()
        }

    // Header Background (Default value is colorAccent)
    var headerTextBackground: Int = ContextCompat.getColor(context, R.color.colorAccent)
        set(value) {
            field = value
            updateHeaderTextAttribute()
        }

    // default finish button value is Button.Transparent
    var finishButtonStyle: Int = R.style.Button_Transparent
        set(value) {
            field = value
            updateFinishButtonAttribute()
        }

    var showDividerLine: Boolean = true
        set(value) {
            field = value
            updateDividerLine()
        }

    //  finishButtonClickListener with listener
    fun finishButtonClickListener(func: ((monthOfYear: Int?, dayOfMonth: Int?, year: Int?) -> Unit)? = null) =
            with(finishButton) {
                setClickListenerToFinishButton(func)
            }

    //  view click listener as extension function
    private fun View.setClickListenerToFinishButton(func: ((monthOfYear: Int?, dayOfMonth: Int?, year: Int?) -> Unit)?) =
            setOnClickListener {
                func?.invoke(month, day, year)
                dialog?.dismiss()
            }

    private fun updateHeaderTextAttribute(){
        with(tvHeader){
            text = headerText
            textAppearance = headerTextStyle
            background = ColorDrawable(headerTextBackground)
        }
    }

    private fun updateFinishButtonAttribute(){
        with(finishButton){
            textAppearance = finishButtonStyle
        }
    }

    private fun updateDividerLine(){
        with(divider){
            visibility = if (showDividerLine) View.VISIBLE else View.GONE
        }
    }

}