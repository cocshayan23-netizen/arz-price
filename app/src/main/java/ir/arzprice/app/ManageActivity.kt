package ir.arzprice.app

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class ManageActivity : Activity() {

    private lateinit var box: LinearLayout

    private fun dp(n: Int): Int = Ui.dp(this, n)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepareWindow()

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.layoutDirection = layoutDir()
        root.setBackgroundColor(Theme.bg)
        root.setPadding(dp(16), dp(14), dp(16), dp(10))

        val head = LinearLayout(this)
        head.orientation = LinearLayout.HORIZONTAL
        head.gravity = Gravity.CENTER_VERTICAL
        val title = Ui.tv(this, I18n.t(this, "manage"), 24f, Theme.text, true)
        title.layoutParams = Ui.lp(0, Ui.WRAP, 1f)
        val close = Ui.tv(this, "✕", 24f, Theme.text, true)
        close.setPadding(dp(10), dp(6), dp(10), dp(6))
        close.setOnClickListener { finish() }
        head.addView(title)
        head.addView(close)

        val info = Ui.tv(this, I18n.t(this, "fav_info"), 14f, Color.WHITE)
        info.background = Ui.rounded(this, Theme.accent, 16)
        info.setPadding(dp(16), dp(14), dp(16), dp(14))
        info.layoutParams = Ui.lp(Ui.MATCH, Ui.WRAP).also { it.topMargin = dp(14); it.bottomMargin = dp(6) }

        box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        val scroll = ScrollView(this)
        scroll.addView(box)
        scroll.layoutParams = Ui.lp(Ui.MATCH, 0, 1f)

        root.addView(head)
        root.addView(info)
        root.addView(scroll)
        setContentView(root)
        rebuild()
    }

    override fun onPause() {
        super.onPause()
        WidgetUpdater.updateAll(applicationContext)
    }

    private fun arrow(label: String, enabled: Boolean, onClick: () -> Unit): TextView {
        val t = Ui.tv(this, label, 20f, if (enabled) Theme.text else Theme.line, true)
        t.setPadding(dp(12), dp(8), dp(12), dp(8))
        if (enabled) t.setOnClickListener { onClick() }
        return t
    }

    private fun rebuild() {
        box.removeAllViews()
        val favs = Data.getFavs(this)
        val medals = arrayOf("🥇", "🥈", "🥉")
        for (i in favs.indices) {
            val ins = Catalog.byKey[favs[i]] ?: continue

            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            row.background = Ui.rounded(this, Theme.card, 16)
            row.setPadding(dp(10), dp(10), dp(6), dp(10))
            row.layoutParams = Ui.lp(Ui.MATCH, Ui.WRAP).also { it.topMargin = dp(10) }

            row.addView(Ui.tv(this, "⋮⋮", 18f, Theme.sub, true))
            val medal = Ui.tv(this, if (i < 3) medals[i] else "", 20f, Theme.text)
            medal.minWidth = dp(36)
            medal.gravity = Gravity.CENTER
            row.addView(medal)

            val names = LinearLayout(this)
            names.orientation = LinearLayout.VERTICAL
            names.layoutParams = Ui.lp(0, Ui.WRAP, 1f)
            names.addView(Ui.tv(this, I18n.name(this, ins), 16f, Theme.text, true))
            names.addView(Ui.tv(this, ins.code, 12f, Theme.sub))
            row.addView(names)

            row.addView(arrow("▲", i > 0) {
                Data.moveFav(this, i, -1)
                rebuild()
            })
            row.addView(arrow("▼", i < favs.size - 1) {
                Data.moveFav(this, i, 1)
                rebuild()
            })
            val del = Ui.tv(this, "✕", 20f, Theme.down, true)
            del.setPadding(dp(12), dp(8), dp(12), dp(8))
            del.setOnClickListener {
                Data.removeFav(this, ins.key)
                rebuild()
            }
            row.addView(del)
            box.addView(row)
        }
        if (favs.isEmpty()) {
            val t = Ui.tv(this, I18n.t(this, "empty_fav"), 14f, Theme.sub)
            t.gravity = Gravity.CENTER
            t.setPadding(dp(16), dp(50), dp(16), dp(16))
            box.addView(t)
        }
    }
}
