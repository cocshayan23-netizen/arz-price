package ir.arzprice.app

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Quote(val key: String, val title: String, val tomans: Long, val changePercent: Double)

object Prices {

    // منبع داده: tgju.org (آدرس غیررسمی). اگر روزی کار نکرد فقط همین خط را عوض کنید.
    private const val URL_STR = "https://call3.tgju.org/ajax.json"

    // کلید در tgju  ->  نام نمایشی. کلیدی که در پاسخ نباشد خودکار رد می‌شود.
    val ITEMS = listOf(
        "price_dollar_rl" to "دلار آمریکا",
        "price_eur" to "یورو",
        "price_gbp" to "پوند انگلیس",
        "price_aed" to "درهم امارات",
        "price_try" to "لیر ترکیه",
        "price_cny" to "یوان چین",
        "price_cad" to "دلار کانادا",
        "price_aud" to "دلار استرالیا",
        "price_chf" to "فرانک سوئیس",
        "price_rub" to "روبل روسیه",
        "price_iqd" to "دینار عراق",
        "price_kwd" to "دینار کویت",
        "price_inr" to "روپیه هند",
        "price_afn" to "افغانی",
        "geram18" to "طلا ۱۸ عیار (گرم)",
        "sekee" to "سکه امامی"
    )

    private const val PREFS = "prices"

    private fun parseNumber(s: String): Double? {
        val cleaned = s.replace(",", "").replace("٬", "").trim()
        return cleaned.toDoubleOrNull()
    }

    /** از اینترنت می‌گیرد، ذخیره می‌کند و لیست را برمی‌گرداند. در صورت خطا استثنا می‌دهد. */
    fun refresh(context: Context): List<Quote> {
        val conn = URL(URL_STR).openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
        val text = try {
            conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
        val current = JSONObject(text).getJSONObject("current")
        val result = ArrayList<Quote>()
        for ((key, title) in ITEMS) {
            val o = current.optJSONObject(key) ?: continue
            val rial = parseNumber(o.optString("p")) ?: continue
            val pct = Math.abs(o.optDouble("dp", 0.0))
            val sign = if (o.optString("dt") == "low") -1.0 else 1.0
            result.add(Quote(key, title, Math.round(rial / 10.0), sign * pct))
        }
        if (result.isEmpty()) throw IllegalStateException("empty")
        save(context, result)
        return result
    }

    private fun save(context: Context, list: List<Quote>) {
        val data = list.joinToString("\n") {
            "${it.key}\t${it.title}\t${it.tomans}\t${it.changePercent}"
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("data", data)
            .putLong("time", System.currentTimeMillis())
            .apply()
    }

    fun loadCached(context: Context): List<Quote> {
        val data = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("data", null) ?: return emptyList()
        val out = ArrayList<Quote>()
        for (line in data.split("\n")) {
            val p = line.split("\t")
            if (p.size < 4) continue
            val t = p[2].toLongOrNull() ?: continue
            out.add(Quote(p[0], p[1], t, p[3].toDoubleOrNull() ?: 0.0))
        }
        return out
    }

    // ---------- انتخاب‌شده‌ها ----------

    fun getFavorites(context: Context): List<String> {
        val s = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("favs", "") ?: ""
        return s.split(",").filter { it.isNotBlank() }
    }

    fun toggleFavorite(context: Context, key: String) {
        val list = getFavorites(context).toMutableList()
        if (!list.remove(key)) list.add(key)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("favs", list.joinToString(","))
            .apply()
    }

    fun favoriteQuotes(context: Context): List<Quote> {
        val all = loadCached(context)
        return getFavorites(context).mapNotNull { k -> all.find { it.key == k } }
    }

    /** ویجت: انتخاب‌شده‌ها؛ اگر چیزی انتخاب نشده باشد، همان ارزهای اول لیست. */
    fun widgetQuotes(context: Context): List<Quote> {
        val f = favoriteQuotes(context)
        return if (f.isNotEmpty()) f else loadCached(context)
    }

    // ---------- نمایش ----------

    fun lastUpdateText(context: Context): String {
        val t = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong("time", 0L)
        if (t == 0L) return "هنوز بروزرسانی نشده"
        val s = SimpleDateFormat("HH:mm", Locale.US).format(Date(t))
        return "آخرین بروزرسانی: " + fa(s)
    }

    private const val FA = "۰۱۲۳۴۵۶۷۸۹"

    fun fa(s: String): String {
        val sb = StringBuilder()
        for (c in s) sb.append(if (c in '0'..'9') FA[c - '0'] else c)
        return sb.toString()
    }

    fun formatToman(n: Long): String = fa(String.format(Locale.US, "%,d", n))

    fun formatPercent(p: Double): String {
        val s = String.format(Locale.US, "%.2f", Math.abs(p))
        val arrow = if (p < 0) "▼ " else if (p > 0) "▲ " else ""
        return fa(arrow + s + "٪")
    }
}
