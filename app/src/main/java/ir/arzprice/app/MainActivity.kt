package ir.arzprice.app

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var listBox: LinearLayout
    private lateinit var status: TextView
    private lateinit var button: Button

    private fun dpi(n: Int): Int = (n * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setBackgroundColor(Color.parseColor("#F4F6F8"))
            setPadding(dpi(16), dpi(40), dpi(16), dpi(16))
        }

        val title = TextView(this).apply {
            text = "قیمت ارز (تومان)"
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#1B2430"))
        }
        status = TextView(this).apply {
            textSize = 12f
            setTextColor(Color.parseColor("#6B7785"))
            setPadding(0, dpi(4), 0, dpi(8))
        }
        button = Button(this).apply {
            text = "بروزرسانی"
            setOnClickListener { load() }
        }
        listBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(listBox) }

        root.addView(title)
        root.addView(status)
        root.addView(button)
        root.addView(scroll)
        setContentView(root)

        render(Prices.loadCached(this))
        load()
    }

    private fun load() {
        button.isEnabled = false
        status.text = "در حال دریافت قیمت‌ها..."
        Thread {
            var error = false
            try {
                Prices.refresh(applicationContext)
            } catch (e: Exception) {
                error = true
            }
            PriceWidget.updateAll(applicationContext)
            runOnUiThread {
                button.isEnabled = true
                render(Prices.loadCached(this))
                status.text = if (error)
                    "اتصال برقرار نشد؛ آخرین قیمت ذخیره‌شده نمایش داده می‌شود. " + Prices.lastUpdateText(this)
                else
                    Prices.lastUpdateText(this)
            }
        }.start()
    }

    private fun render(list: List<Quote>) {
        listBox.removeAllViews()
        if (list.isEmpty()) {
            status.text = "قیمتی ذخیره نشده است."
            return
        }
        for (q in list) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setBackgroundColor(Color.WHITE)
                setPadding(dpi(14), dpi(14), dpi(14), dpi(14))
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dpi(8) }
            row.layoutParams = lp

            val name = TextView(this).apply {
                text = q.title
                textSize = 16f
                setTextColor(Color.parseColor("#1B2430"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val priceBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            val price = TextView(this).apply {
                text = Prices.formatToman(q.tomans)
                textSize = 17f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#1B2430"))
            }
            val change = TextView(this).apply {
                text = Prices.formatPercent(q.changePercent)
                textSize = 12f
                setTextColor(
                    if (q.changePercent < 0) Color.parseColor("#D64545")
                    else Color.parseColor("#1B8A5A")
                )
            }
            priceBox.addView(price)
            priceBox.addView(change)
            row.addView(name)
            row.addView(priceBox)
            listBox.addView(row)
        }
    }
}
