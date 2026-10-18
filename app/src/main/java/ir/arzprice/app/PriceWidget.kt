package ir.arzprice.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class PriceWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        refresh(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) refresh(context)
    }

    private fun refresh(context: Context) {
        val app = context.applicationContext
        val pending = goAsync()
        Thread {
            try {
                Prices.refresh(app)
            } catch (e: Exception) {
                // اینترنت قطع است؛ آخرین قیمت ذخیره‌شده نمایش داده می‌شود
            }
            updateAll(app)
            pending.finish()
        }.start()
    }

    companion object {
        const val ACTION_REFRESH = "ir.arzprice.app.REFRESH"

        private val NAME_IDS = intArrayOf(R.id.n1, R.id.n2, R.id.n3, R.id.n4, R.id.n5)
        private val PRICE_IDS = intArrayOf(R.id.p1, R.id.p2, R.id.p3, R.id.p4, R.id.p5)

        fun updateAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, PriceWidget::class.java))
            if (ids.isEmpty()) return

            val quotes = Prices.loadCached(context)
            val views = RemoteViews(context.packageName, R.layout.widget)
            for (i in NAME_IDS.indices) {
                val q = quotes.getOrNull(i)
                views.setTextViewText(NAME_IDS[i], q?.title ?: "")
                views.setTextViewText(PRICE_IDS[i], if (q != null) Prices.formatToman(q.tomans) else "")
            }
            views.setTextViewText(
                R.id.time,
                Prices.lastUpdateText(context) + " • برای بروزرسانی لمس کنید"
            )

            val intent = Intent(context, PriceWidget::class.java).setAction(ACTION_REFRESH)
            val pi = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.root, pi)

            mgr.updateAppWidget(ids, views)
        }
    }
}
