package ir.arzprice.app

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : Activity() {

    private lateinit var listBox: LinearLayout
    private lateinit var dateView: TextView
    private lateinit var statusView: TextView
    private lateinit var swipe: SwipeRefreshLayout

    private fun dp(n: Int): Int = Ui.dp(this, n)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepareWindow()

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.layoutDirection = layoutDir()
        root.setBackgroundColor(Theme.bg)
        root.setPadding(dp(16), dp(12), dp(16), dp(10))

        val title = Ui.tv(this, "NABZ | نبض بازار", 26f, Theme.text, true)
        dateView = Ui.tv(this, "", 13f, Theme.sub)
        statusView = Ui.tv(this, "", 11f, Theme.sub)
        statusView.setPadding(0, dp(2), 0, dp(4))

        listBox = LinearLayout(this)
        listBox.orientation = LinearLayout.VERTICAL
        val scroll = ScrollView(this)
        scroll.addView(listBox)
        scroll.layoutParams = ViewGroup.LayoutParams(Ui.MATCH, Ui.MATCH)

        swipe = SwipeRefreshLayout(this)
        swipe.setColorSchemeColors(Theme.up)
        swipe.setProgressBackgroundColorSchemeColor(Theme.card)
        swipe.setOnRefreshListener { load() }
        swipe.addView(scroll)
        swipe.layoutParams = Ui.lp(Ui.MATCH, 0, 1f)

        root.addView(title)
        root.addView(dateView)
        root.addView(statusView)
        root.addView(swipe)
        root.addView(buildBar())
        setContentView(root)

        scheduleWork()
        render()
        swipe.post { load() }
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun scheduleWork() {
        val req = PeriodicWorkRequest.Builder(AlertWorker::class.java, 15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork("nabz_refresh", ExistingPeriodicWorkPolicy.KEEP, req)
    }

    // ---------- نوار پایین: بروزرسانی، + و ⋮ ----------

    private fun barButton(label: String, sp: Float, onClick: () -> Unit): TextView {
        val t = Ui.tv(this, label, sp, Theme.text, true)
        t.gravity = Gravity.CENTER
        t.layoutParams = Ui.lp(dp(52), dp(52))
        t.setOnClickListener { onClick() }
        return t
    }

    private fun buildBar(): View {
        val bar = LinearLayout(this)
        bar.orientation = LinearLayout.HORIZONTAL
        bar.gravity = Gravity.CENTER_VERTICAL
        bar.layoutParams = Ui.lp(Ui.MATCH, Ui.WRAP).also { it.topMargin = dp(8) }

        val refresh = barButton("↻", 24f) { swipe.isRefreshing = true; load() }
        refresh.background = Ui.oval(if (Theme.dark) Color.BLACK else Theme.card2)

        val spacer = View(this)
        spacer.layoutParams = Ui.lp(0, 1, 1f)

        val pill = LinearLayout(this)
        pill.orientation = LinearLayout.HORIZONTAL
        pill.gravity = Gravity.CENTER_VERTICAL
        pill.background = Ui.rounded(this, Theme.card, 28)
        pill.addView(barButton("+", 28f) { showAddSheet() })
        pill.addView(barButton("⋮", 24f) { showMenu() })

        bar.addView(refresh)
        bar.addView(spacer)
        bar.addView(pill)
        return bar
    }

    // ---------- بارگیری و نمایش ----------

    private fun load() {
        swipe.isRefreshing = true
        statusView.text = I18n.t(this, "loading")
        Thread {
            var error = false
            try {
                Data.refresh(applicationContext)
            } catch (e: Exception) {
                error = true
            }
            WidgetUpdater.updateAll(applicationContext)
            runOnUiThread {
                swipe.isRefreshing = false
                render()
                statusView.text = if (error) I18n.t(this, "offline") else updatedText()
            }
        }.start()
    }

    private fun updatedText(): String {
        val t = Data.lastUpdate(this)
        if (t == 0L) return ""
        val c = java.util.Calendar.getInstance()
        c.timeInMillis = t
        val s = String.format(java.util.Locale.US, "%02d:%02d", c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE))
        return I18n.t(this, "updated") + ": " + I18n.digits(this, s)
    }

    private fun render() {
        dateView.text = I18n.headerDate(this)
        listBox.removeAllViews()
        val quotes = Data.favoriteQuotes(this)
        if (quotes.isEmpty()) {
            val msg = if (Data.cached(this).isEmpty()) I18n.t(this, "loading") else I18n.t(this, "empty_fav")
            val h = Ui.tv(this, msg, 14f, Theme.sub)
            h.gravity = Gravity.CENTER
            h.setPadding(dp(16), dp(60), dp(16), dp(16))
            listBox.addView(h)
            return
        }
        if (Settings.view(this) == "grid") {
            var i = 0
            while (i < quotes.size) {
                val row = LinearLayout(this)
                row.orientation = LinearLayout.HORIZONTAL
                row.layoutParams = Ui.lp(Ui.MATCH, Ui.WRAP).also { it.topMargin = dp(10) }
                row.addView(gridCard(quotes[i]))
                if (i + 1 < quotes.size) {
                    row.addView(gridCard(quotes[i + 1]))
                } else {
                    val empty = View(this)
                    empty.layoutParams = Ui.lp(0, 1, 1f)
                    row.addView(empty)
                }
                listBox.addView(row)
                i += 2
            }
        } else {
            for (q in quotes) listBox.addView(listRow(q))
        }
    }

    private fun openDetail(q: Quote) {
        val i = Intent(this, DetailActivity::class.java)
        i.putExtra("key", q.ins.key)
        startActivity(i)
    }

    private fun gridCard(q: Quote): View {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.background = Ui.rounded(this, Theme.card, 22)
        card.setPadding(dp(14), dp(14), dp(14), dp(14))
        card.minimumHeight = dp(175)
        card.layoutParams = Ui.lp(0, Ui.WRAP, 1f).also {
            it.leftMargin = dp(5)
            it.rightMargin = dp(5)
        }
        card.setOnClickListener { openDetail(q) }

        val head = LinearLayout(this)
        head.orientation = LinearLayout.HORIZONTAL
        head.gravity = Gravity.CENTER_VERTICAL
        head.addView(Ui.icon(this, q.ins, 40))
        val names = LinearLayout(this)
        names.orientation = LinearLayout.VERTICAL
        names.layoutParams = Ui.lp(0, Ui.WRAP, 1f)
        names.setPadding(dp(10), 0, dp(10), 0)
        val nm = Ui.tv(this, I18n.name(this, q.ins), 14f, Theme.text, true)
        nm.maxLines = 2
        names.addView(nm)
        names.addView(Ui.tv(this, q.ins.code, 12f, Theme.sub))
        head.addView(names)
        card.addView(head)

        val fill = View(this)
        fill.layoutParams = Ui.lp(Ui.MATCH, 0, 1f)
        card.addView(fill)

        card.addView(Ui.tv(this, Fmt.changeText(this, q), 13f, Ui.changeColor(q)))
        val prev = Ui.tv(this, Fmt.price(this, q.prev, q.usd), 13f, Theme.sub)
        prev.setPadding(0, dp(4), 0, 0)
        card.addView(prev)
        val price = Ui.tv(this, Fmt.price(this, q.price, q.usd), 24f, Theme.text, true)
        price.setPadding(0, dp(2), 0, 0)
        card.addView(price)
        return card
    }

    private fun listRow(q: Quote): View {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        row.background = Ui.rounded(this, Theme.card, 16)
        row.setPadding(dp(14), dp(12), dp(14), dp(12))
        row.layoutParams = Ui.lp(Ui.MATCH, Ui.WRAP).also { it.topMargin = dp(8) }
        row.setOnClickListener { openDetail(q) }

        row.addView(Ui.icon(this, q.ins, 40))
        val names = LinearLayout(this)
        names.orientation = LinearLayout.VERTICAL
        names.layoutParams = Ui.lp(0, Ui.WRAP, 1f)
        names.setPadding(dp(12), 0, dp(12), 0)
        names.addView(Ui.tv(this, I18n.name(this, q.ins), 15f, Theme.text, true))
        names.addView(Ui.tv(this, q.ins.code, 12f, Theme.sub))
        row.addView(names)

        val right = LinearLayout(this)
        right.orientation = LinearLayout.VERTICAL
        right.gravity = Gravity.END
        right.addView(Ui.tv(this, Fmt.price(this, q.price, q.usd), 18f, Theme.text, true))
        right.addView(Ui.tv(this, Fmt.changeText(this, q), 12f, Ui.changeColor(q)))
        row.addView(right)
        return row
    }

    // ---------- منوی ⋮ ----------

    private fun newDialog(): Dialog {
        val d = Dialog(this)
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return d
    }

    private fun showMenu() {
        val d = newDialog()
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.layoutDirection = layoutDir()
        box.background = Ui.rounded(this, Theme.menu, 22)
        box.setPadding(dp(6), dp(6), dp(6), dp(6))

        menuSection(box, d, "▦", I18n.t(this, "menu_view"),
            listOf("grid" to I18n.t(this, "grid"), "list" to I18n.t(this, "list")),
            Settings.view(this)) { Settings.setView(this, it) }
        menuSection(box, d, "▣", I18n.t(this, "calendar"),
            listOf("persian" to I18n.t(this, "persian"), "gregorian" to I18n.t(this, "gregorian")),
            Settings.calendar(this)) { Settings.setCalendar(this, it) }
        menuSection(box, d, "◍", I18n.t(this, "language"),
            listOf("en" to "English 🇬🇧", "tr" to "Türkçe 🇹🇷", "fa" to "فارسی 🇮🇷"),
            Settings.lang(this)) { Settings.setLang(this, it) }
        menuSection(box, d, "◐", I18n.t(this, "theme"),
            listOf("dark" to I18n.t(this, "dark"), "light" to I18n.t(this, "light")),
            if (Settings.dark(this)) "dark" else "light") { Settings.setDark(this, it == "dark") }

        val manage = menuHead("♡", I18n.t(this, "manage"), false)
        manage.setOnClickListener {
            d.dismiss()
            startActivity(Intent(this, ManageActivity::class.java))
        }
        box.addView(manage)

        d.setContentView(box)
        val w = d.window
        if (w != null) {
            w.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            w.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
            val a = w.attributes
            a.y = dp(80)
            w.attributes = a
            w.setLayout(dp(300), WindowManager.LayoutParams.WRAP_CONTENT)
        }
        d.show()
    }

    private fun menuHead(icon: String, label: String, expandable: Boolean): LinearLayout {
        val h = LinearLayout(this)
        h.orientation = LinearLayout.HORIZONTAL
        h.gravity = Gravity.CENTER_VERTICAL
        h.setPadding(dp(14), dp(16), dp(14), dp(16))
        val chev = Ui.tv(this, if (expandable) "›" else " ", 20f, Theme.text, true)
        chev.setPadding(0, 0, dp(10), 0)
        val txt = Ui.tv(this, label, 16f, Theme.text)
        txt.layoutParams = Ui.lp(0, Ui.WRAP, 1f)
        h.addView(chev)
        h.addView(txt)
        h.addView(Ui.tv(this, icon, 20f, Theme.sub))
        return h
    }

    private fun menuSection(
        box: LinearLayout, d: Dialog, icon: String, title: String,
        opts: List<Pair<String, String>>, current: String, onPick: (String) -> Unit
    ) {
        val head = menuHead(icon, title, true)
        val sub = LinearLayout(this)
        sub.orientation = LinearLayout.VERTICAL
        sub.visibility = View.GONE
        for (o in opts) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            row.setPadding(dp(40), dp(12), dp(20), dp(12))
            val on = o.first == current
            val label = Ui.tv(this, o.second, 15f, if (on) Color.parseColor("#A9B4FF") else Theme.text)
            label.layoutParams = Ui.lp(0, Ui.WRAP, 1f)
            row.addView(label)
            if (on) row.addView(Ui.tv(this, "✓", 18f, Color.parseColor("#A9B4FF"), true))
            row.setOnClickListener {
                onPick(o.first)
                d.dismiss()
                recreate()
            }
            sub.addView(row)
        }
        head.setOnClickListener {
            sub.visibility = if (sub.visibility == View.GONE) View.VISIBLE else View.GONE
        }
        box.addView(head)
        box.addView(sub)
        val line = View(this)
        line.setBackgroundColor(Theme.line)
        line.layoutParams = Ui.lp(Ui.MATCH, 1)
        box.addView(line)
    }

    // ---------- برگه‌ی افزودن (+) ----------

    private fun showAddSheet() {
        val d = newDialog()
        var tab = Catalog.CURRENCY
        var query = ""

        val sheet = LinearLayout(this)
        sheet.orientation = LinearLayout.VERTICAL
        sheet.layoutDirection = layoutDir()
        sheet.background = Ui.roundedTop(this, Theme.bg, 26)
        sheet.setPadding(dp(16), dp(18), dp(16), dp(8))

        val tabs = LinearLayout(this)
        tabs.orientation = LinearLayout.HORIZONTAL
        tabs.background = Ui.rounded(this, Theme.card2, 14)
        tabs.setPadding(dp(4), dp(4), dp(4), dp(4))
        val tabViews = ArrayList<TextView>()
        val tabGroups = listOf(Catalog.CRYPTO, Catalog.GOLD, Catalog.CURRENCY)
        val tabNames = listOf(I18n.t(this, "tab_crypto"), I18n.t(this, "tab_gold"), I18n.t(this, "tab_currency"))

        val search = EditText(this)
        search.hint = I18n.t(this, "search")
        search.setHintTextColor(Theme.sub)
        search.setTextColor(Theme.text)
        search.textSize = 16f
        search.setSingleLine(true)
        search.background = Ui.rounded(this, Theme.card, 16)
        search.setPadding(dp(16), dp(14), dp(16), dp(14))
        search.layoutParams = Ui.lp(Ui.MATCH, Ui.WRAP).also { it.topMargin = dp(12) }

        val listBox2 = LinearLayout(this)
        listBox2.orientation = LinearLayout.VERTICAL
        val scroll = ScrollView(this)
        scroll.addView(listBox2)
        scroll.layoutParams = Ui.lp(Ui.MATCH, 0, 1f)

        fun fill() {
            listBox2.removeAllViews()
            val raws = Data.cached(this)
            val favs = Data.getFavs(this)
            val qq = query.trim().lowercase()
            for (ins in Catalog.list) {
                if (ins.group != tab) continue
                if (raws.isNotEmpty() && !raws.containsKey(ins.key)) continue
                if (qq.isNotEmpty()) {
                    val hay = (ins.fa + " " + ins.en + " " + ins.tr + " " + ins.code).lowercase()
                    if (!hay.contains(qq)) continue
                }
                val row = LinearLayout(this)
                row.orientation = LinearLayout.HORIZONTAL
                row.gravity = Gravity.CENTER_VERTICAL
                row.setPadding(dp(4), dp(10), dp(4), dp(10))
                row.addView(Ui.icon(this, ins, 36))
                val label = Ui.tv(this, I18n.name(this, ins), 16f, Theme.text)
                label.layoutParams = Ui.lp(0, Ui.WRAP, 1f)
                label.setPadding(dp(12), 0, dp(12), 0)
                row.addView(label)
                val isFav = favs.contains(ins.key)
                row.addView(Ui.tv(this, if (isFav) "✓" else "+", 22f, if (isFav) Theme.up else Theme.sub, true))
                row.setOnClickListener {
                    Data.toggleFav(this, ins.key)
                    fill()
                }
                listBox2.addView(row)
                val line = View(this)
                line.setBackgroundColor(Theme.line)
                line.layoutParams = Ui.lp(Ui.MATCH, 1)
                listBox2.addView(line)
            }
        }

        fun styleTabs() {
            for (i in tabViews.indices) {
                val on = tabGroups[i] == tab
                tabViews[i].setTextColor(if (on) Theme.text else Theme.sub)
                tabViews[i].background = if (on) Ui.rounded(this, Theme.line, 10) else null
            }
        }

        for (i in tabGroups.indices) {
            val t = Ui.tv(this, tabNames[i], 14f, Theme.sub, true)
            t.gravity = Gravity.CENTER
            t.setPadding(dp(4), dp(11), dp(4), dp(11))
            t.layoutParams = Ui.lp(0, Ui.WRAP, 1f)
            t.setOnClickListener {
                tab = tabGroups[i]
                styleTabs()
                fill()
            }
            tabViews.add(t)
            tabs.addView(t)
        }

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                query = s?.toString() ?: ""
                fill()
            }
        })

        sheet.addView(tabs)
        sheet.addView(search)
        sheet.addView(scroll)
        styleTabs()
        fill()

        d.setContentView(sheet)
        d.setOnDismissListener {
            render()
            WidgetUpdater.updateAll(applicationContext)
        }
        val w = d.window
        if (w != null) {
            w.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            w.setGravity(Gravity.BOTTOM)
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            val h = (resources.displayMetrics.heightPixels * 0.86).toInt()
            w.setLayout(WindowManager.LayoutParams.MATCH_PARENT, h)
        }
        d.show()
    }
}
