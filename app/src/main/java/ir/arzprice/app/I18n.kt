package ir.arzprice.app

import android.content.Context
import java.util.Calendar
import java.util.Locale

/** تنظیمات برنامه */
object Settings {
    private fun p(ctx: Context) = ctx.getSharedPreferences("ui", Context.MODE_PRIVATE)

    fun lang(ctx: Context): String = p(ctx).getString("lang", "fa") ?: "fa"
    fun setLang(ctx: Context, v: String) = p(ctx).edit().putString("lang", v).apply()

    fun calendar(ctx: Context): String = p(ctx).getString("cal", "persian") ?: "persian"
    fun setCalendar(ctx: Context, v: String) = p(ctx).edit().putString("cal", v).apply()

    fun view(ctx: Context): String = p(ctx).getString("view", "grid") ?: "grid"
    fun setView(ctx: Context, v: String) = p(ctx).edit().putString("view", v).apply()

    fun dark(ctx: Context): Boolean = p(ctx).getBoolean("dark", true)
    fun setDark(ctx: Context, v: Boolean) = p(ctx).edit().putBoolean("dark", v).apply()
}

object I18n {
    private const val FA_DIGITS = "۰۱۲۳۴۵۶۷۸۹"

    // ترتیب: فارسی، انگلیسی، ترکی
    private val S: Map<String, Array<String>> = mapOf(
        "menu_view" to arrayOf("حالت نمایش", "View Mode", "Görünüm"),
        "grid" to arrayOf("شبکه‌ای", "Grid", "Izgara"),
        "list" to arrayOf("لیستی", "List", "Liste"),
        "calendar" to arrayOf("تقویم", "Calendar", "Takvim"),
        "persian" to arrayOf("شمسی", "Persian", "Farsça"),
        "gregorian" to arrayOf("میلادی", "Gregorian", "Miladi"),
        "language" to arrayOf("زبان", "Language", "Dil"),
        "theme" to arrayOf("پوسته", "Theme", "Tema"),
        "dark" to arrayOf("تیره", "Dark", "Koyu"),
        "light" to arrayOf("روشن", "Light", "Açık"),
        "manage" to arrayOf("مدیریت علاقه‌مندی‌ها", "Manage Favorites", "Favorileri Yönet"),
        "tab_crypto" to arrayOf("ارز دیجیتال", "Cryptocurrency", "Kripto"),
        "tab_gold" to arrayOf("طلا و سکه", "Gold", "Altın"),
        "tab_currency" to arrayOf("ارز", "Currency", "Döviz"),
        "search" to arrayOf("جستجو...", "Search...", "Ara..."),
        "fav_info" to arrayOf(
            "۳ مورد اول علاقه‌مندی‌ها در ویجت نمایش داده می‌شود. با فلش‌ها ترتیب را عوض کنید.",
            "Top 3 favorites will be shown in the widget. Use the arrows to reorder.",
            "İlk 3 favori widget'ta gösterilir. Sıralamak için okları kullanın."
        ),
        "empty_fav" to arrayOf(
            "موردی انتخاب نشده است. دکمه + را بزنید.",
            "Nothing here yet. Tap + to add.",
            "Henüz bir şey yok. Eklemek için + 'ya dokunun."
        ),
        "loading" to arrayOf("در حال دریافت قیمت‌ها...", "Loading prices...", "Fiyatlar yükleniyor..."),
        "updated" to arrayOf("آخرین بروزرسانی", "Last update", "Son güncelleme"),
        "offline" to arrayOf(
            "اتصال برقرار نشد؛ آخرین قیمت‌های ذخیره‌شده نمایش داده می‌شود.",
            "Couldn't connect; showing saved prices.",
            "Bağlanılamadı; kayıtlı fiyatlar gösteriliyor."
        ),
        "toman" to arrayOf("تومان", "Toman", "Toman"),
        "r1m" to arrayOf("۱ ماه", "1M", "1A"),
        "r1y" to arrayOf("۱ سال", "1Y", "1Y"),
        "rall" to arrayOf("همه", "All", "Tümü"),
        "high" to arrayOf("بیشترین", "H", "Y"),
        "low" to arrayOf("کمترین", "L", "D"),
        "nodata" to arrayOf("داده‌ای برای نمودار نیست.", "No chart data.", "Grafik verisi yok."),
        "chart_loading" to arrayOf("در حال دریافت نمودار...", "Loading chart...", "Grafik yükleniyor..."),
        "converter" to arrayOf("مبدل", "Converter", "Çevirici"),
        "amount" to arrayOf("مقدار", "Amount", "Miktar"),
        "value" to arrayOf("ارزش", "Value", "Değer"),
        "alerts" to arrayOf("هشدار قیمت", "Price alerts", "Fiyat uyarıları"),
        "add_alert" to arrayOf("+ افزودن هشدار", "+ Add alert", "+ Uyarı ekle"),
        "above" to arrayOf("بالاتر از", "Above", "Üstünde"),
        "below" to arrayOf("پایین‌تر از", "Below", "Altında"),
        "price" to arrayOf("قیمت", "Price", "Fiyat"),
        "save" to arrayOf("ذخیره", "Save", "Kaydet"),
        "cancel" to arrayOf("لغو", "Cancel", "İptal"),
        "no_alerts" to arrayOf("هشداری تنظیم نشده است.", "No alerts set.", "Uyarı yok."),
        "alert_hit" to arrayOf("به %s رسید", "reached %s", "%s seviyesine ulaştı")
    )

    private val WEEK = arrayOf(
        arrayOf("یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه"),
        arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"),
        arrayOf("Pazar", "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi")
    )

    private val PMONTH_FA = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )
    private val PMONTH_EN = arrayOf(
        "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
        "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand"
    )
    private val GMONTH = arrayOf(
        arrayOf("ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن", "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"),
        arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"),
        arrayOf("Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık")
    )

    fun idx(ctx: Context): Int = when (Settings.lang(ctx)) {
        "fa" -> 0
        "tr" -> 2
        else -> 1
    }

    fun isRtl(ctx: Context): Boolean = Settings.lang(ctx) == "fa"

    fun t(ctx: Context, key: String): String = S[key]?.get(idx(ctx)) ?: key

    fun name(ctx: Context, ins: Instrument): String = when (idx(ctx)) {
        0 -> ins.fa
        1 -> ins.en
        else -> if (ins.tr.isBlank()) ins.en else ins.tr
    }

    fun digits(ctx: Context, s: String): String {
        if (idx(ctx) != 0) return s
        val sb = StringBuilder()
        for (c in s) sb.append(if (c in '0'..'9') FA_DIGITS[c - '0'] else c)
        return sb.toString()
    }

    /** تبدیل تاریخ میلادی به شمسی */
    fun jalali(gy: Int, gm: Int, gd: Int): IntArray {
        val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gdm[gm - 1]
        var jy = -1595 + 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm = if (days < 186) 1 + days / 31 else 7 + (days - 186) / 30
        val jd = if (days < 186) 1 + days % 31 else 1 + (days - 186) % 30
        return intArrayOf(jy, jm, jd)
    }

    /** مثلاً «دوشنبه ۲۷ مهر • ۱۰:۴۷» */
    fun headerDate(ctx: Context): String {
        val c = Calendar.getInstance()
        val gy = c.get(Calendar.YEAR)
        val gm = c.get(Calendar.MONTH) + 1
        val gd = c.get(Calendar.DAY_OF_MONTH)
        val i = idx(ctx)
        val wd = WEEK[i][c.get(Calendar.DAY_OF_WEEK) - 1]
        val day: Int
        val month: String
        if (Settings.calendar(ctx) == "persian") {
            val j = jalali(gy, gm, gd)
            day = j[2]
            month = if (i == 0) PMONTH_FA[j[1] - 1] else PMONTH_EN[j[1] - 1]
        } else {
            day = gd
            month = GMONTH[i][gm - 1]
        }
        val time = String.format(Locale.US, "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
        return digits(ctx, "$wd $day $month  •  $time")
    }

    /** برچسب تاریخ روی نمودار */
    fun chartDate(ctx: Context, p: Point, yearOnly: Boolean): String {
        val persian = Settings.calendar(ctx) == "persian"
        val y: Int
        val m: Int
        val d: Int
        if (persian) {
            val j = jalali(p.y, p.m, p.d)
            y = j[0]; m = j[1]; d = j[2]
        } else {
            y = p.y; m = p.m; d = p.d
        }
        val s = if (yearOnly) "$y" else "$m/$d"
        return digits(ctx, s)
    }
}

object Fmt {
    private fun trim(s: String): String {
        if (!s.contains('.')) return s
        return s.trimEnd('0').trimEnd('.')
    }

    /** عدد خام (لاتین) برای قیمت */
    fun raw(v: Double, usd: Boolean): String {
        if (!usd) return String.format(Locale.US, "%,d", Math.round(v))
        return when {
            v >= 100 -> String.format(Locale.US, "%,.0f", v)
            v >= 1 -> String.format(Locale.US, "%,.2f", v)
            else -> trim(String.format(Locale.US, "%.6f", v))
        }
    }

    fun price(ctx: Context, v: Double, usd: Boolean): String = I18n.digits(ctx, raw(v, usd))

    /** مقدار تغییر؛ اعداد بزرگ کوتاه می‌شوند (مثلاً 1.2M) */
    fun change(ctx: Context, v: Double, usd: Boolean): String {
        val a = Math.abs(v)
        val s = if (!usd && a >= 1_000_000) String.format(Locale.US, "%.1fM", a / 1_000_000.0) else raw(a, usd)
        return I18n.digits(ctx, s)
    }

    fun percent(ctx: Context, v: Double): String =
        I18n.digits(ctx, String.format(Locale.US, "%.2f%%", Math.abs(v)))

    /** عدد ساده برای کادر مبدل */
    fun plain(v: Double): String {
        val s = if (Math.abs(v) >= 100) String.format(Locale.US, "%.0f", v) else String.format(Locale.US, "%.4f", v)
        return trim(s)
    }

    fun changeText(ctx: Context, q: Quote): String = when {
        q.change > 0 -> "↑ " + change(ctx, q.change, q.usd)
        q.change < 0 -> "↓ " + change(ctx, q.change, q.usd)
        else -> "– " + I18n.digits(ctx, "0")
    }
}
