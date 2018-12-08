package id.medigo.waluyofield.datepicker

import android.widget.EditText
import id.medigo.waluyofield.WaluyoNumberPicker

/**
 * This code is rewritten and modified to kotlin from https://github.com/drawers/SpinnerDatePicker
 */

object NumberPickers {

    //inefficient way of obtaining EditText from inside NumberPicker - not too bad here as View
    //hierarchy is very small -
    fun findEditText(np: WaluyoNumberPicker): EditText? {
        for (i in 0 until np.childCount) {
            if (np.getChildAt(i) is EditText) {
                return np.getChildAt(i) as EditText
            }
        }
        return null
    }
}