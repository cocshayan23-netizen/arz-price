package ir.arzprice.app

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : Activity() {

    private var dark = false
    private var showFav = false

    private lateinit var listBox: LinearLayout
    private lateinit var status: TextView
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var tabAll: TextView
    private lateinit var tabFav: TextView

    // رنگ‌ها (بسته به تم روشن/تیره)
    private var cBg = 0
    private var cCard = 0
    private var cText = 0
    private var cSub = 0
    private var cAccent = 0
    private var cTab = 0
    private var cRed = 0

    private fun dpi(n: Int): Int = (n * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radiusDp: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radiusDp * resources.displayMetrics.density
        }

    private fun isDarkMode(): Boolean {
        val p = getSharedPreferences("ui", MODE_PRIVATE)
        if (p.contains("dark")) return p.getBoolean("dark", false)
        val night = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return night == Configuration.UI_MODE_NIGHT_YES
    }

    private fun setupColors() {
        if (dark) {
            cBg = Color.parseColor("#12171D")
            cCard = Color.parseColor("#1C232B")
            cText = Color.parseColor("#EAF0F6")
            cSub = Color.parseColor("#8B97A4")
            cAccent = Color.parseColor("#2FB67C")
            cTab = Color.parseColor("#26303A")
            cRed = Color.parseColor("#FF6B6B")
        } else {
            cBg = Color.parseColor("#F4F6F8")
            cCard = Color.parseColor("#FFFFFF")
            cText = Color.parseColor("#1B2430")
            cSub = Color.parseColor("#6B7785")
            cAccent = Color.parseColor("#1B8A5A")
            cTab = Color.parseColor("#E3E7EB")
            cRed = Color.parseColor("#D64545")
        }
    }

    private fun setupSystemBars() {
        window.statusBarColor = cBg
        window.navigationBarColor = cBg
        var flags = window.decorView.systemUiVisibility
        if (dark) {
            flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            if (Build.VERSION.SDK_INT >= 26) {
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
        } else {
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            if (Build.VERSION.SDK_INT >= 26) {
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        }
        window.decorView.systemUiVisibility = flags
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dark = isDarkMode()
        showFav = getSharedPreferences("ui", MODE_PRIVATE).getBoolean("fav_tab", false)
        setupColors()
        setupSystemBars()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setBackgroundColor(cBg)
            setPadding(dpi(16), dpi(12), dpi(16), 0)
        }

        // --- سربرگ: عنوان + دکمه تم ---
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = TextView(this).apply {
            text = "قیمت ارز (تومان)"
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setTextColor(cText)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val themeBtn = TextView(this).apply {
            text = if (dark) "☀️" else "🌙"
            textSize = 22f
            setPadding(dpi(10), dpi(6), dpi(10), dpi(6))
            setOnClickListener {
                getSharedPreferences("ui", MODE_PRIVATE).edit().putBoolean("dark", !dark).apply()
                recreate()
            }
        }
        header.addView(title)
        header.addView(themeBtn)

        status = TextView(this).apply {
            textSize = 12f
            setTextColor(cSub)
            setPadding(0, dpi(2), 0, dpi(10))
        }

        // --- تب‌ها: همه / انتخاب‌شده‌ها ---
        val tabs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpi(4) }
        }
        tabAll = makeTab("همه ارزها", false)
        tabFav = makeTab("★ انتخاب‌شده‌ها", true)
        tabs.addView(tabAll)
        tabs.addView(tabFav)
        styleTabs()

        // --- لیست با قابلیت کشیدن به پایین برای بروزرسانی ---
        listBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dpi(16))
        }
        val scroll = ScrollView(this).apply {
            addView(listBox)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        swipe = SwipeRefreshLayout(this).apply {
            setColorSchemeColors(cAccent)
            setProgressBackgroundColorSchemeColor(cCard)
            setOnRefreshListener { load() }
            addView(scroll)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }

        root.addView(header)
        root.addView(status)
        root.addView(tabs)
        root.addView(swipe)
        setContentView(root)

        status.text = Prices.lastUpdateText(this)
        render()
        swipe.post { load() }
    }

    private fun makeTab(label: String, fav: Boolean): TextView {
        return TextView(this).apply {
            text = label
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(dpi(8), dpi(10), dpi(8), dpi(10))
            layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            ).apply {
                marginStart = dpi(4)
                marginEnd = dpi(4)
            }
            setOnClickListener {
                showFav = fav
                getSharedPreferences("ui", MODE_PRIVATE).edit().putBoolean("fav_tab", fav).apply()
                styleTabs()
                render()
            }
        }
    }

    private fun styleTabs() {
        val sel = if (showFav) tabFav else tabAll
        val other = if (showFav) tabAll else tabFav
        sel.background = rounded(cAccent, 10)
        sel.setTextColor(Color.WHITE)
        other.background = rounded(cTab, 10)
        other.setTextColor(cSub)
    }

    private fun load() {
        swipe.isRefreshing = true
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
                swipe.isRefreshing = false
                render()
                status.text = if (error)
                    "اتصال برقرار نشد؛ آخرین قیمت ذخیره‌شده نمایش داده می‌شود. " + Prices.lastUpdateText(this)
                else
                    Prices.lastUpdateText(this)
            }
        }.start()
    }

    private fun render() {
        listBox.removeAllViews()
        val favKeys = Prices.getFavorites(this)
        val list = if (showFav) Prices.favoriteQuotes(this) else Prices.loadCached(this)

        if (showFav && list.isEmpty()) {
            val hint = TextView(this).apply {
                text = "هنوز ارزی انتخاب نکرده‌اید.\nدر تب «همه ارزها» روی ☆ کنار هر ارز بزنید تا اینجا و در ویجت نمایش داده شود."
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(cSub)
                setPadding(dpi(16), dpi(40), dpi(16), dpi(16))
            }
            listBox.addView(hint)
            return
        }

        for (q in list) {
            val isFav = favKeys.contains(q.key)

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = rounded(cCard, 12)
                setPadding(dpi(8), dpi(12), dpi(14), dpi(12))
            }
            row.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dpi(8) }

            val star = TextView(this).apply {
                text = if (isFav) "★" else "☆"
                textSize = 24f
                setTextColor(if (isFav) Color.parseColor("#F5B301") else cSub)
                setPadding(dpi(8), dpi(2), dpi(12), dpi(2))
                setOnClickListener {
                    Prices.toggleFavorite(this@MainActivity, q.key)
                    PriceWidget.updateAll(this@MainActivity.applicationContext)
                    render()
                }
            }

            val name = TextView(this).apply {
                text = q.title
                textSize = 16f
                setTextColor(cText)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            val priceBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            val price = TextView(this).apply {
                text = Prices.formatToman(q.tomans)
                textSize = 17f
                setTypeface(null, Typeface.BOLD)
                setTextColor(cText)
            }
            val change = TextView(this).apply {
                text = Prices.formatPercent(q.changePercent)
                textSize = 12f
                setTextColor(if (q.changePercent < 0) cRed else cAccent)
            }
            priceBox.addView(price)
            priceBox.addView(change)

            row.addView(star)
            row.addView(name)
            row.addView(priceBox)
            listBox.addView(row)
        }
    }
}
