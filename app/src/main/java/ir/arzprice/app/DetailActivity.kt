package ir.arzprice.app

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView

class DetailActivity : Activity() {

    private lateinit var ins: Instrument
    private lateinit var quote: Quote

    private lateinit var chart: ChartView
    private lateinit var chartMsg: TextView
    private lateinit var hView: TextView
    private lateinit var lView: TextView
    private lateinit var alertsBox: LinearLayout
    private val rangeViews = ArrayList<TextView>()

    private var all: List<Point> = emptyList()
    private var range = 0
    private var loaded = false

    private fun dp(n: Int): Int = Ui.dp(this, n)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepareWindow()

        val key = intent.getStringExtra("key")
        val i = if (key == null) null else Catalog.byKey[key]
        val q = if (i == null) null else Data.quote(this, i, Data.cached(this))
        if (i == null || q == null) {
            finish()
            return
        }
        ins = i
        quote = q

        val content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.layoutDirection = layoutDir()
        content.setPadding(dp(16), dp(14), dp(16), dp(24))

        // سربرگ
        val head = LinearLayout(this)
        head.orientation = LinearLayout.HORIZONTAL
        head.gravity = Gravity.CENTER_VERTICAL
        head.addView(Ui.icon(this, ins, 52))
        val names = LinearLayout(this)
        names.orientation = LinearLayout.VERTICAL
        names.layoutParams = Ui.lp(0, Ui.WRAP, 1f)
        names.setPadding(dp(12), 0, dp(12), 0)
        names.addView(Ui.tv(this, I18n.name(this, ins), 20f, Theme.text, true))
        names.addView(Ui.tv(this, ins.code, 14f, Theme.sub))
        head.addView(names)
        val close = Ui.tv(this, "✕", 24f, Theme.text, true)
        close.setPadding(dp(10), dp(6), dp(10), dp(6))
        close.setOnClickListener { finish() }
        head.addView(close)
        content.addView(head)

        // قیمت
        val chg = Ui.tv(this, Fmt.changeText(this, quote), 16f, Ui.changeColor(quote))
        chg.setPadding(0, dp(22), 0, 0)
        content.addView(chg)
        content.addView(Ui.tv(this, Fmt.price(this, quote.prev, quote.usd), 16f, Theme.sub))
        content.addView(Ui.tv(this, Fmt.price(this, quote.price, quote.usd), 40f, Theme.text, true))
        content.addView(Ui.tv(this, Ui.unitLabel(this, quote) + "  •  " + Fmt.percent(this, quote.pct), 13f, Theme.sub))

        // بیشترین / کمترین
        val hl = LinearLayout(this)
        hl.orientation = LinearLayout.HORIZONTAL
        hl.setPadding(0, dp(18), 0, dp(6))
        hView = Ui.tv(this, "", 14f, Theme.up, true)
        hView.layoutParams = Ui.lp(0, Ui.WRAP, 1f)
        lView = Ui.tv(this, "", 14f, Theme.down, true)
        lView.gravity = Gravity.END
        lView.layoutParams = Ui.lp(0, Ui.WRAP, 1f)
        hl.addView(hView)
        hl.addView(lView)
        content.addView(hl)

        // نمودار
        val frame = FrameLayout(this)
        frame.layoutParams = Ui.lp(Ui.MATCH, dp(240))
        chart = ChartView(this)
        chart.layoutParams = FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH)
        chartMsg = Ui.tv(this, I18n.t(this, "chart_loading"), 13f, Theme.sub)
        val mlp = FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP)
        mlp.gravity = Gravity.CENTER
        chartMsg.layoutParams = mlp
        frame.addView(chart)
        frame.addView(chartMsg)
        content.addView(frame)

        // بازه‌ها
        val tabs = LinearLayout(this)
        tabs.orientation = LinearLayout.HORIZONTAL
        tabs.background = Ui.rounded(this, Theme.card2, 14)
        tabs.setPadding(dp(4), dp(4), dp(4), dp(4))
        tabs.layoutParams = Ui.lp(Ui.MATCH, Ui.WRAP).also { it.topMargin = dp(10) }
        val names3 = listOf(I18n.t(this, "r1m"), I18n.t(this, "r1y"), I18n.t(this, "rall"))
        for (r in names3.indices) {
            val t = Ui.tv(this, names3[r], 14f, Theme.sub, true)
            t.gravity = Gravity.CENTER
            t.setPadding(dp(4), dp(10), dp(4), dp(10))
            t.layoutParams = Ui.lp(0, Ui.WRAP, 1f)
            t.setOnClickListener {
                range = r
                applyRange()
            }
            rangeViews.add(t)
            tabs.addView(t)
        }
        content.addView(tabs)

        content.addView(buildConverter())
        content.addView(buildAlerts())

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(Theme.bg)
        scroll.addView(content)
        setContentView(scroll)

        applyRange()
        loadHistory()
    }

    // ---------- نمودار ----------

    private fun loadHistory() {
        Thread {
            var list: List<Point> = emptyList()
            try {
                list = Data.history(applicationContext, ins)
            } catch (e: Exception) {
                list = emptyList()
            }
            runOnUiThread {
                loaded = true
                all = list
                applyRange()
            }
        }.start()
    }

    private fun applyRange() {
        for (i in rangeViews.indices) {
            val on = i == range
            rangeViews[i].setTextColor(if (on) Theme.text else Theme.sub)
            rangeViews[i].background = if (on) Ui.rounded(this, Theme.line, 10) else null
        }
        if (all.size < 2) {
            chart.setData(emptyList(), emptyList(), true)
            chartMsg.visibility = View.VISIBLE
            chartMsg.text = I18n.t(this, if (loaded) "nodata" else "chart_loading")
            hView.text = ""
            lView.text = ""
            return
        }
        chartMsg.visibility = View.GONE
        val sub: List<Point> = when (range) {
            0 -> all.takeLast(30)
            1 -> all.takeLast(365)
            else -> all
        }
        val vals = ArrayList<Double>()
        var hi = -Double.MAX_VALUE
        var lo = Double.MAX_VALUE
        for (p in sub) {
            vals.add(p.v)
            if (p.v > hi) hi = p.v
            if (p.v < lo) lo = p.v
        }
        val labels = ArrayList<String>()
        for (k in 0 until 4) {
            val idx = (sub.size - 1) * k / 3
            labels.add(I18n.chartDate(this, sub[idx], range == 2))
        }
        chart.setData(vals, labels, sub.last().v >= sub.first().v)
        hView.text = I18n.t(this, "high") + ": " + Fmt.price(this, hi, quote.usd)
        lView.text = I18n.t(this, "low") + ": " + Fmt.price(this, lo, quote.usd)
    }

    // ---------- مبدل ----------

    private fun field(hint: String): EditText {
        val e = EditText(this)
        e.hint = hint
        e.setHintTextColor(Theme.sub)
        e.setTextColor(Theme.text)
        e.textSize = 16f
        e.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        e.setSingleLine(true)
        e.background = Ui.rounded(this, Theme.card2, 12)
        e.setPadding(dp(14), dp(12), dp(14), dp(12))
        e.layoutParams = Ui.lp(0, Ui.WRAP, 1f)
        return e
    }

    private fun watcher(f: () -> Unit): TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
        override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        override fun afterTextChanged(s: Editable?) {
            f()
        }
    }

    private fun card(): LinearLayout {
        val c = LinearLayout(this)
        c.orientation = LinearLayout.VERTICAL
        c.background = Ui.rounded(this, Theme.card, 18)
        c.setPadding(dp(16), dp(14), dp(16), dp(16))
        c.layoutParams = Ui.lp(Ui.MATCH, Ui.WRAP).also { it.topMargin = dp(16) }
        return c
    }

    private fun buildConverter(): View {
        val c = card()
        c.addView(Ui.tv(this, I18n.t(this, "converter"), 16f, Theme.text, true))

        val r1 = LinearLayout(this)
        r1.orientation = LinearLayout.HORIZONTAL
        r1.gravity = Gravity.CENTER_VERTICAL
        r1.setPadding(0, dp(10), 0, 0)
        val amount = field(I18n.t(this, "amount"))
        r1.addView(amount)
        val l1 = Ui.tv(this, ins.code, 14f, Theme.sub, true)
        l1.setPadding(dp(10), 0, dp(10), 0)
        l1.minWidth = dp(56)
        r1.addView(l1)

        val r2 = LinearLayout(this)
        r2.orientation = LinearLayout.HORIZONTAL
        r2.gravity = Gravity.CENTER_VERTICAL
        r2.setPadding(0, dp(8), 0, 0)
        val value = field(I18n.t(this, "value"))
        r2.addView(value)
        val l2 = Ui.tv(this, Ui.unitLabel(this, quote), 14f, Theme.sub, true)
        l2.setPadding(dp(10), 0, dp(10), 0)
        l2.minWidth = dp(56)
        r2.addView(l2)

        var busy = false
        amount.addTextChangedListener(watcher {
            if (!busy) {
                busy = true
                val a = amount.text.toString().toDoubleOrNull()
                value.setText(if (a == null) "" else Fmt.plain(a * quote.price))
                busy = false
            }
        })
        value.addTextChangedListener(watcher {
            if (!busy) {
                busy = true
                val v = value.text.toString().toDoubleOrNull()
                amount.setText(if (v == null || quote.price <= 0.0) "" else Fmt.plain(v / quote.price))
                busy = false
            }
        })
        c.addView(r1)
        c.addView(r2)
        return c
    }

    // ---------- هشدار قیمت ----------

    private fun buildAlerts(): View {
        val c = card()
        c.addView(Ui.tv(this, I18n.t(this, "alerts"), 16f, Theme.text, true))
        alertsBox = LinearLayout(this)
        alertsBox.orientation = LinearLayout.VERTICAL
        c.addView(alertsBox)
        val add = Ui.tv(this, I18n.t(this, "add_alert"), 15f, Color.parseColor("#8FA0FF"), true)
        add.setPadding(0, dp(12), 0, 0)
        add.setOnClickListener { showAlertDialog() }
        c.addView(add)
        refreshAlerts()
        return c
    }

    private fun refreshAlerts() {
        alertsBox.removeAllViews()
        val mine = Alerts.all(this).filter { it.key == ins.key }
        if (mine.isEmpty()) {
            val t = Ui.tv(this, I18n.t(this, "no_alerts"), 13f, Theme.sub)
            t.setPadding(0, dp(8), 0, 0)
            alertsBox.addView(t)
            return
        }
        for (a in mine) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            row.setPadding(0, dp(8), 0, 0)
            val dir = if (a.above) I18n.t(this, "above") else I18n.t(this, "below")
            val label = Ui.tv(this, dir + "  " + Fmt.price(this, a.price, quote.usd) + " " + Ui.unitLabel(this, quote), 15f, Theme.text)
            label.layoutParams = Ui.lp(0, Ui.WRAP, 1f)
            row.addView(label)
            val del = Ui.tv(this, "✕", 18f, Theme.down, true)
            del.setPadding(dp(10), dp(4), dp(10), dp(4))
            del.setOnClickListener {
                Alerts.remove(this, a.id)
                refreshAlerts()
            }
            row.addView(del)
            alertsBox.addView(row)
        }
    }

    private fun showAlertDialog() {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(dp(22), dp(10), dp(22), dp(0))

        val group = RadioGroup(this)
        group.orientation = LinearLayout.HORIZONTAL
        val rUp = RadioButton(this)
        rUp.id = 101
        rUp.text = I18n.t(this, "above")
        val rDown = RadioButton(this)
        rDown.id = 102
        rDown.text = I18n.t(this, "below")
        group.addView(rUp)
        group.addView(rDown)
        group.check(101)

        val input = EditText(this)
        input.hint = I18n.t(this, "price") + " (" + Ui.unitLabel(this, quote) + ")"
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.setText(Fmt.plain(quote.price))

        box.addView(group)
        box.addView(input)

        val theme = if (Theme.dark) android.R.style.Theme_Material_Dialog_Alert
        else android.R.style.Theme_Material_Light_Dialog_Alert
        AlertDialog.Builder(this, theme)
            .setTitle(I18n.name(this, ins))
            .setView(box)
            .setPositiveButton(I18n.t(this, "save")) { _, _ ->
                val p = input.text.toString().toDoubleOrNull()
                if (p != null && p > 0) {
                    Alerts.add(this, ins.key, group.checkedRadioButtonId == 101, p)
                    askNotificationPermission()
                    refreshAlerts()
                }
            }
            .setNegativeButton(I18n.t(this, "cancel"), null)
            .show()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            val perm = "android.permission.POST_NOTIFICATIONS"
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(perm), 7)
            }
        }
    }
}
