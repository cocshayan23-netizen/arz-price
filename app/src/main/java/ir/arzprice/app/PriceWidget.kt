package ir.arzprice.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews

/** پایه‌ی چهار مدل ویجت */
open class BaseWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val app = context.applicationContext
        WidgetUpdater.updateAll(app)
        val pending = goAsync()
        Thread {
            try {
                Data.refresh(app)
            } catch (e: Exception) {
                // اینترنت قطع است؛ آخرین قیمت ذخیره‌شده نمایش داده می‌شود
            }
            WidgetUpdater.updateAll(app)
            pending.finish()
        }.start()
    }
}

class ListSmallWidget : BaseWidget()
class ListWideWidget : BaseWidget()
class SingleSmallWidget : BaseWidget()
class SingleWideWidget : BaseWidget()

object WidgetUpdater {
    private val ROW = intArrayOf(R.id.row1, R.id.row2, R.id.row3)
    private val ICON = intArrayOf(R.id.icon1, R.id.icon2, R.id.icon3)
    private val CODE = intArrayOf(R.id.code1, R.id.code2, R.id.code3)
    private val CHG = intArrayOf(R.id.chg1, R.id.chg2, R.id.chg3)
    private val PRICE = intArrayOf(R.id.price1, R.id.price2, R.id.price3)
    private val DIV = intArrayOf(R.id.div1, R.id.div2)

    private val UP = Color.parseColor("#4CD38A")
    private val DOWN = Color.parseColor("#FF6B6B")
    private val GRAY = Color.parseColor("#9AA7B5")

    fun updateAll(ctx: Context) {
        val mgr = AppWidgetManager.getInstance(ctx)
        val lists = listOf(ListSmallWidget::class.java, ListWideWidget::class.java)
        val singles = listOf(SingleSmallWidget::class.java, SingleWideWidget::class.java)
        val quotes = Data.favoriteQuotes(ctx)
        for (cls in lists) {
            val ids = mgr.getAppWidgetIds(ComponentName(ctx, cls))
            if (ids.isNotEmpty()) mgr.updateAppWidget(ids, buildList(ctx, quotes))
        }
        for (cls in singles) {
            val ids = mgr.getAppWidgetIds(ComponentName(ctx, cls))
            if (ids.isNotEmpty()) mgr.updateAppWidget(ids, buildSingle(ctx, quotes))
        }
    }

    private fun openApp(ctx: Context): PendingIntent =
        PendingIntent.getActivity(ctx, 0, Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)

    private fun chgColor(q: Quote): Int = when {
        q.change > 0 -> UP
        q.change < 0 -> DOWN
        else -> GRAY
    }

    private fun iconColor(q: Quote): Int = if (q.ins.bg == 0) Color.WHITE else q.ins.bg

    private fun buildList(ctx: Context, quotes: List<Quote>): RemoteViews {
        val v = RemoteViews(ctx.packageName, R.layout.widget)
        for (i in 0 until 3) {
            val q = quotes.getOrNull(i)
            if (q == null) {
                v.setViewVisibility(ROW[i], View.GONE)
                continue
            }
            v.setViewVisibility(ROW[i], View.VISIBLE)
            v.setTextViewText(ICON[i], q.ins.icon)
            v.setTextColor(ICON[i], iconColor(q))
            v.setTextViewText(CODE[i], q.ins.code)
            v.setTextViewText(CHG[i], Fmt.changeText(ctx, q))
            v.setTextColor(CHG[i], chgColor(q))
            v.setTextViewText(PRICE[i], Fmt.price(ctx, q.price, q.usd))
        }
        for (i in 0 until 2) {
            v.setViewVisibility(DIV[i], if (quotes.size > i + 1) View.VISIBLE else View.GONE)
        }
        v.setOnClickPendingIntent(R.id.root, openApp(ctx))
        return v
    }

    private fun buildSingle(ctx: Context, quotes: List<Quote>): RemoteViews {
        val v = RemoteViews(ctx.packageName, R.layout.widget_single)
        val q = quotes.getOrNull(0)
        if (q != null) {
            v.setTextViewText(R.id.s_icon, q.ins.icon)
            v.setTextColor(R.id.s_icon, iconColor(q))
            v.setTextViewText(R.id.s_name, I18n.name(ctx, q.ins))
            v.setTextViewText(R.id.s_code, q.ins.code)
            v.setTextViewText(R.id.s_chg, Fmt.changeText(ctx, q))
            v.setTextColor(R.id.s_chg, chgColor(q))
            v.setTextViewText(R.id.s_price, Fmt.price(ctx, q.price, q.usd))
        }
        v.setOnClickPendingIntent(R.id.root, openApp(ctx))
        return v
    }
}
