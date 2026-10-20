package ir.arzprice.app

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

object Theme {
    var dark = true
    val bg: Int get() = Color.parseColor(if (dark) "#12171D" else "#F4F6F8")
    val card: Int get() = Color.parseColor(if (dark) "#1E252E" else "#FFFFFF")
    val card2: Int get() = Color.parseColor(if (dark) "#2A323D" else "#E6EAEE")
    val text: Int get() = Color.parseColor(if (dark) "#EAF0F6" else "#1B2430")
    val sub: Int get() = Color.parseColor(if (dark) "#8B97A4" else "#6B7785")
    val up: Int get() = Color.parseColor(if (dark) "#4CD38A" else "#1B8A5A")
    val down: Int get() = Color.parseColor(if (dark) "#FF6B6B" else "#D64545")
    val accent: Int get() = Color.parseColor("#3949AB")
    val menu: Int get() = Color.parseColor(if (dark) "#434A55" else "#FFFFFF")
    val line: Int get() = Color.parseColor(if (dark) "#38414C" else "#D5DAE0")
}

object Ui {
    const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
    const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

    fun dp(ctx: Context, n: Int): Int = (n * ctx.resources.displayMetrics.density).toInt()

    fun rounded(ctx: Context, color: Int, radiusDp: Int): GradientDrawable {
        val g = GradientDrawable()
        g.setColor(color)
        g.cornerRadius = radiusDp * ctx.resources.displayMetrics.density
        return g
    }

    fun roundedTop(ctx: Context, color: Int, radiusDp: Int): GradientDrawable {
        val g = GradientDrawable()
        g.setColor(color)
        val r = radiusDp * ctx.resources.displayMetrics.density
        g.cornerRadii = floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f)
        return g
    }

    fun oval(color: Int): GradientDrawable {
        val g = GradientDrawable()
        g.shape = GradientDrawable.OVAL
        g.setColor(color)
        return g
    }

    fun lp(w: Int, h: Int, weight: Float = 0f): LinearLayout.LayoutParams = LinearLayout.LayoutParams(w, h, weight)

    fun tv(ctx: Context, text: String, sp: Float, color: Int, bold: Boolean = false): TextView {
        val t = TextView(ctx)
        t.text = text
        t.textSize = sp
        t.setTextColor(color)
        if (bold) t.setTypeface(null, Typeface.BOLD)
        return t
    }

    /** آیکون دایره‌ای: پرچم (ایموجی) یا نشان رنگی */
    fun icon(ctx: Context, ins: Instrument, sizeDp: Int): TextView {
        val t = TextView(ctx)
        t.text = ins.icon
        t.gravity = Gravity.CENTER
        t.includeFontPadding = false
        t.layoutParams = LinearLayout.LayoutParams(dp(ctx, sizeDp), dp(ctx, sizeDp))
        if (ins.bg == 0) {
            t.textSize = sizeDp * 0.55f
        } else {
            t.textSize = if (ins.icon.length <= 2) sizeDp * 0.40f else sizeDp * 0.25f
            t.setTextColor(ins.fg)
            t.setTypeface(null, Typeface.BOLD)
            t.background = oval(ins.bg)
        }
        return t
    }

    fun changeColor(q: Quote): Int = when {
        q.change > 0 -> Theme.up
        q.change < 0 -> Theme.down
        else -> Theme.sub
    }

    fun unitLabel(ctx: Context, q: Quote): String = if (q.usd) "USD" else I18n.t(ctx, "toman")
}

/** رنگ نوار وضعیت و جهت صفحه بر اساس تم و زبان */
fun Activity.prepareWindow() {
    Theme.dark = Settings.dark(this)
    window.statusBarColor = Theme.bg
    window.navigationBarColor = Theme.bg
    var flags = window.decorView.systemUiVisibility
    if (Theme.dark) {
        flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        if (Build.VERSION.SDK_INT >= 26) flags = flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
    } else {
        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        if (Build.VERSION.SDK_INT >= 26) flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }
    window.decorView.systemUiVisibility = flags
}

fun Activity.layoutDir(): Int =
    if (I18n.isRtl(this)) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
