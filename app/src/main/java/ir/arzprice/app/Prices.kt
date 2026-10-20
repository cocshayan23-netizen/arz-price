package ir.arzprice.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** مقدار خام از tgju */
class Raw(val p: Double, val d: Double, val dp: Double, val dir: Int)

/** قیمت آماده‌ی نمایش (تومان، یا دلار برای ارزهای دیجیتال/اونس) */
class Quote(val ins: Instrument, val price: Double, val change: Double, val pct: Double) {
    val prev: Double get() = price - change
    val usd: Boolean get() = ins.unit == U.USD
}

/** یک نقطه از تاریخچه‌ی قیمت */
class Point(val y: Int, val m: Int, val d: Int, val v: Double)

class Alert(val id: Long, val key: String, val above: Boolean, val price: Double)

object Data {
    private const val PREFS = "prices"

    // منبع داده: tgju.org (غیررسمی). اگر روزی کار نکرد فقط این دو آدرس را عوض کنید.
    private const val AJAX = "https://call3.tgju.org/ajax.json"
    private const val HIST = "https://api.tgju.org/v1/market/indicator/summary-table-data/"

    private val DEFAULT_FAVS = listOf("price_dollar_rl", "price_eur", "geram18", "crypto-bitcoin", "mesghal")
    private val histCache = HashMap<String, List<Point>>()

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun http(url: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 15000
        c.readTimeout = 40000
        c.setRequestProperty("User-Agent", "Mozilla/5.0")
        c.setRequestProperty("Accept", "application/json")
        try {
            return c.inputStream.bufferedReader().use { it.readText() }
        } finally {
            c.disconnect()
        }
    }

    fun parseNum(s: String?): Double? {
        if (s == null) return null
        val t = s.replace(",", "").replace("٬", "").trim()
        return t.toDoubleOrNull()
    }

    // ---------- قیمت‌های لحظه‌ای ----------

    /** از اینترنت می‌گیرد و ذخیره می‌کند؛ در صورت خطا استثنا می‌دهد. */
    fun refresh(ctx: Context): Map<String, Raw> {
        val text = http(AJAX)
        val cur = JSONObject(text).getJSONObject("current")
        val out = HashMap<String, Raw>()
        for (ins in Catalog.list) {
            val o = cur.optJSONObject(ins.key) ?: continue
            val p = parseNum(o.optString("p")) ?: continue
            val d = parseNum(o.optString("d")) ?: 0.0
            val dp = o.optDouble("dp", 0.0)
            val dir = when (o.optString("dt")) {
                "low" -> -1
                "high" -> 1
                else -> 0
            }
            out[ins.key] = Raw(p, Math.abs(d), if (dp.isNaN()) 0.0 else Math.abs(dp), dir)
        }
        if (out.isEmpty()) throw IllegalStateException("empty")

        // واحد قیمت تتر (ریال یا تومان) را با مقایسه با دلار تشخیص می‌دهیم
        val t = out["crypto-tether-irr"]
        val u = out["price_dollar_rl"]
        val irr = if (t != null && u != null && u.p > 0 && t.p < u.p * 0.5) 1.0 else 0.1

        val sb = StringBuilder()
        for ((k, r) in out) {
            sb.append(k).append('\t').append(r.p).append('\t').append(r.d).append('\t')
                .append(r.dp).append('\t').append(r.dir).append('\n')
        }
        prefs(ctx).edit()
            .putString("raw", sb.toString())
            .putLong("time", System.currentTimeMillis())
            .putString("irr", irr.toString())
            .apply()
        return out
    }

    fun cached(ctx: Context): Map<String, Raw> {
        val s = prefs(ctx).getString("raw", null) ?: return emptyMap()
        val out = HashMap<String, Raw>()
        for (line in s.split("\n")) {
            val a = line.split("\t")
            if (a.size < 5) continue
            val p = a[1].toDoubleOrNull() ?: continue
            out[a[0]] = Raw(p, a[2].toDoubleOrNull() ?: 0.0, a[3].toDoubleOrNull() ?: 0.0, a[4].toIntOrNull() ?: 0)
        }
        return out
    }

    fun lastUpdate(ctx: Context): Long = prefs(ctx).getLong("time", 0L)

    private fun irrFactor(ctx: Context): Double = prefs(ctx).getString("irr", null)?.toDoubleOrNull() ?: 0.1

    fun factor(ctx: Context, ins: Instrument): Double = when (ins.unit) {
        U.RIAL -> 0.1
        U.USD -> 1.0
        U.TETHER -> irrFactor(ctx)
    }

    fun quote(ctx: Context, ins: Instrument, raws: Map<String, Raw>): Quote? {
        val r = raws[ins.key] ?: return null
        val f = factor(ctx, ins)
        val sign = r.dir.toDouble()
        return Quote(ins, r.p * f, sign * r.d * f, sign * r.dp)
    }

    fun favoriteQuotes(ctx: Context): List<Quote> {
        val raws = cached(ctx)
        val out = ArrayList<Quote>()
        for (k in getFavs(ctx)) {
            val ins = Catalog.byKey[k] ?: continue
            val q = quote(ctx, ins, raws) ?: continue
            out.add(q)
        }
        return out
    }

    // ---------- علاقه‌مندی‌ها ----------

    fun getFavs(ctx: Context): List<String> {
        val s = prefs(ctx).getString("favs", null) ?: return DEFAULT_FAVS
        return s.split(",").filter { it.isNotBlank() && Catalog.byKey.containsKey(it) }
    }

    private fun saveFavs(ctx: Context, list: List<String>) {
        prefs(ctx).edit().putString("favs", list.joinToString(",")).apply()
    }

    fun isFav(ctx: Context, key: String): Boolean = getFavs(ctx).contains(key)

    fun toggleFav(ctx: Context, key: String) {
        val l = getFavs(ctx).toMutableList()
        if (!l.remove(key)) l.add(key)
        saveFavs(ctx, l)
    }

    fun removeFav(ctx: Context, key: String) {
        saveFavs(ctx, getFavs(ctx).filter { it != key })
    }

    fun moveFav(ctx: Context, index: Int, delta: Int) {
        val l = getFavs(ctx).toMutableList()
        val j = index + delta
        if (index < 0 || index >= l.size || j < 0 || j >= l.size) return
        val t = l[index]
        l[index] = l[j]
        l[j] = t
        saveFavs(ctx, l)
    }

    // ---------- تاریخچه (برای نمودار) ----------

    /** قدیمی‌ترین به جدیدترین. در صورت خطا استثنا می‌دهد. */
    fun history(ctx: Context, ins: Instrument): List<Point> {
        val c = histCache[ins.key]
        if (c != null) return c
        val text = http(HIST + ins.key)
        val arr = JSONObject(text).getJSONArray("data")
        val f = factor(ctx, ins)
        val out = ArrayList<Point>()
        for (i in 0 until arr.length()) {
            val row = arr.optJSONArray(i) ?: continue
            val close = parseNum(row.optString(3)) ?: continue
            val parts = row.optString(6).split("/")
            if (parts.size < 3) continue
            val y = parts[0].trim().toIntOrNull() ?: continue
            val m = parts[1].trim().toIntOrNull() ?: continue
            val d = parts[2].trim().toIntOrNull() ?: continue
            out.add(Point(y, m, d, close * f))
        }
        out.sortWith(compareBy<Point>({ it.y }, { it.m }, { it.d }))
        if (out.size < 2) throw IllegalStateException("no history")
        histCache[ins.key] = out
        return out
    }
}

object Alerts {
    private fun prefs(ctx: Context) = ctx.getSharedPreferences("alerts", Context.MODE_PRIVATE)

    fun all(ctx: Context): List<Alert> {
        val s = prefs(ctx).getString("list", "[]") ?: "[]"
        val out = ArrayList<Alert>()
        try {
            val arr = JSONArray(s)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(Alert(o.getLong("id"), o.getString("key"), o.getBoolean("above"), o.getDouble("price")))
            }
        } catch (e: Exception) {
            // فایل خراب؛ خالی در نظر گرفته می‌شود
        }
        return out
    }

    private fun save(ctx: Context, list: List<Alert>) {
        val arr = JSONArray()
        for (a in list) {
            arr.put(JSONObject().put("id", a.id).put("key", a.key).put("above", a.above).put("price", a.price))
        }
        prefs(ctx).edit().putString("list", arr.toString()).apply()
    }

    fun add(ctx: Context, key: String, above: Boolean, price: Double) {
        val l = all(ctx).toMutableList()
        l.add(Alert(System.currentTimeMillis(), key, above, price))
        save(ctx, l)
    }

    fun remove(ctx: Context, id: Long) {
        save(ctx, all(ctx).filter { it.id != id })
    }

    /** بررسی هشدارها با آخرین قیمت‌های ذخیره‌شده؛ هشدارِ رسیده یک‌بار اعلان می‌دهد و حذف می‌شود. */
    fun check(ctx: Context) {
        val list = all(ctx)
        if (list.isEmpty()) return
        val raws = Data.cached(ctx)
        val remaining = ArrayList<Alert>()
        for (a in list) {
            val ins = Catalog.byKey[a.key]
            val q = if (ins != null) Data.quote(ctx, ins, raws) else null
            if (ins == null || q == null) {
                remaining.add(a)
                continue
            }
            val hit = if (a.above) q.price >= a.price else q.price <= a.price
            if (hit) notify(ctx, a, q) else remaining.add(a)
        }
        save(ctx, remaining)
    }

    private fun notify(ctx: Context, a: Alert, q: Quote) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(NotificationChannel("alerts", "Price alerts", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val open = PendingIntent.getActivity(
            ctx, a.id.toInt(), Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val text = String.format(I18n.t(ctx, "alert_hit"), Fmt.price(ctx, q.price, q.usd) + " " + Ui.unitLabel(ctx, q))
        val n = NotificationCompat.Builder(ctx, "alerts")
            .setSmallIcon(R.drawable.ic_notif)
            .setContentTitle(I18n.name(ctx, q.ins))
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        nm.notify(a.id.toInt(), n)
    }
}
