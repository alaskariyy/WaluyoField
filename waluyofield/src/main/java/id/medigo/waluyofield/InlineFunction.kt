package id.medigo.waluyofield

import android.app.Activity
import android.app.AlertDialog
import android.support.v4.app.Fragment
import id.medigo.waluyofield.datepicker.WaluyoDateDialog
import java.util.*

inline fun Activity.showDatePickerDialog(func: WaluyoDateDialog.() -> Unit,
                                         defaultDate: Calendar = GregorianCalendar(1980, 0, 1)
                                         , minDate: Calendar = GregorianCalendar(1900, 0, 1)
                                         , maxDate: Calendar = GregorianCalendar(2100, 0, 1)): AlertDialog =
        WaluyoDateDialog(this, defaultDate, minDate, maxDate).apply {
            func()
        }.create()

inline fun Fragment.showDatePickerDialog(defaultDate: Calendar = GregorianCalendar(1980, 0, 1)
                                         , minDate: Calendar = GregorianCalendar(1900, 0, 1)
                                         , maxDate: Calendar = GregorianCalendar(2100, 0, 1)
                                         , func: WaluyoDateDialog.() -> Unit): AlertDialog =
        WaluyoDateDialog(context!!, defaultDate, minDate, maxDate).apply {
            func()
        }.create()