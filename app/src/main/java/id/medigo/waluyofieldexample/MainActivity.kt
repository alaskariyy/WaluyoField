package id.medigo.waluyofieldexample

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import id.medigo.waluyofield.showDatePickerDialog

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btn = findViewById<Button>(R.id.button)
        val tv = findViewById<TextView>(R.id.tv)
        btn.setOnClickListener {
            val dialog = showDatePickerDialog({
                headerText = "HAHAHAHA"
                wdp.mDaySpinner.textStyle = R.style.Title
                wdp.mMonthSpinner.textSize = 60
                wdp.mYearSpinner.textColor = Color.CYAN
                wdp.onDateChangeListener { monthOfYear, dayOfMonth, year ->
                    headerText = "$dayOfMonth "+getEngMonthName(monthOfYear)+" $year"
                }
                finishButtonClickListener { month: Int?, day: Int?, year: Int? ->
                    tv.text = "$day "+getEngMonthName(month)+" $year"
                }
            })
            val lp = WindowManager.LayoutParams()

            lp.copyFrom(dialog.window.attributes)
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            dialog.show()
            dialog.window.attributes = lp
        }
    }
}
