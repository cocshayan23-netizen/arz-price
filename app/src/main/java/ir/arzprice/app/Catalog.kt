package ir.arzprice.app

/** RIAL = قیمت در tgju به ریال است (تقسیم بر ۱۰ می‌شود)، USD = دلاری، TETHER = تتر به تومان (واحد خودکار تشخیص داده می‌شود) */
enum class U { RIAL, USD, TETHER }

class Instrument(
    val key: String,
    val group: Int,
    val code: String,
    val icon: String,
    val bg: Int,
    val fg: Int,
    val fa: String,
    val en: String,
    val tr: String,
    val unit: U
)

object Catalog {
    const val CRYPTO = 0
    const val GOLD = 1
    const val CURRENCY = 2

    val list = ArrayList<Instrument>()
    val byKey = HashMap<String, Instrument>()

    private fun add(i: Instrument) {
        list.add(i)
        byKey[i.key] = i
    }

    private fun cur(key: String, code: String, flag: String, fa: String, en: String, tr: String) {
        add(Instrument(key, CURRENCY, code, flag, 0, 0, fa, en, tr, U.RIAL))
    }

    private fun gold(key: String, code: String, badge: String, bg: Long, fa: String, en: String, tr: String, unit: U = U.RIAL) {
        add(Instrument(key, GOLD, code, badge, bg.toInt(), 0xFF3A2D00.toInt(), fa, en, tr, unit))
    }

    private fun crypto(key: String, code: String, sym: String, bg: Long, fa: String, en: String, tr: String = "", unit: U = U.USD) {
        add(Instrument(key, CRYPTO, code, sym, bg.toInt(), 0xFFFFFFFF.toInt(), fa, en, tr, unit))
    }

    init {
        // ---------- ارز ----------
        cur("price_dollar_rl", "USD", "🇺🇸", "دلار آمریکا", "US Dollar", "Amerikan Doları")
        cur("price_eur", "EUR", "🇪🇺", "یورو", "Euro", "Euro")
        cur("price_gbp", "GBP", "🇬🇧", "پوند انگلیس", "British Pound", "İngiliz Sterlini")
        cur("price_aed", "AED", "🇦🇪", "درهم امارات", "UAE Dirham", "BAE Dirhemi")
        cur("price_try", "TRY", "🇹🇷", "لیر ترکیه", "Turkish Lira", "Türk Lirası")
        cur("price_chf", "CHF", "🇨🇭", "فرانک سوئیس", "Swiss Franc", "İsviçre Frangı")
        cur("price_cny", "CNY", "🇨🇳", "یوان چین", "Chinese Yuan", "Çin Yuanı")
        cur("price_jpy", "JPY", "🇯🇵", "ین ژاپن (۱۰۰ ین)", "Japanese Yen (100)", "Japon Yeni (100)")
        cur("price_cad", "CAD", "🇨🇦", "دلار کانادا", "Canadian Dollar", "Kanada Doları")
        cur("price_aud", "AUD", "🇦🇺", "دلار استرالیا", "Australian Dollar", "Avustralya Doları")
        cur("price_nzd", "NZD", "🇳🇿", "دلار نیوزیلند", "New Zealand Dollar", "Yeni Zelanda Doları")
        cur("price_sgd", "SGD", "🇸🇬", "دلار سنگاپور", "Singapore Dollar", "Singapur Doları")
        cur("price_inr", "INR", "🇮🇳", "روپیه هند", "Indian Rupee", "Hindistan Rupisi")
        cur("price_pkr", "PKR", "🇵🇰", "روپیه پاکستان", "Pakistani Rupee", "Pakistan Rupisi")
        cur("price_iqd", "IQD", "🇮🇶", "دینار عراق", "Iraqi Dinar", "Irak Dinarı")
        cur("price_kwd", "KWD", "🇰🇼", "دینار کویت", "Kuwaiti Dinar", "Kuveyt Dinarı")
        cur("price_syp", "SYP", "🇸🇾", "پوند سوریه", "Syrian Pound", "Suriye Lirası")
        cur("price_afn", "AFN", "🇦🇫", "افغانی", "Afghan Afghani", "Afgan Afganisi")
        cur("price_rub", "RUB", "🇷🇺", "روبل روسیه", "Russian Ruble", "Rus Rublesi")
        cur("price_dkk", "DKK", "🇩🇰", "کرون دانمارک", "Danish Krone", "Danimarka Kronu")
        cur("price_sek", "SEK", "🇸🇪", "کرون سوئد", "Swedish Krona", "İsveç Kronu")
        cur("price_nok", "NOK", "🇳🇴", "کرون نروژ", "Norwegian Krone", "Norveç Kronu")
        cur("price_qar", "QAR", "🇶🇦", "ریال قطر", "Qatari Riyal", "Katar Riyali")
        cur("price_amd", "AMD", "🇦🇲", "درام ارمنستان", "Armenian Dram", "Ermeni Dramı")
        cur("price_krw", "KRW", "🇰🇷", "وون کره جنوبی", "South Korean Won", "Güney Kore Wonu")
        cur("price_omr", "OMR", "🇴🇲", "ریال عمان", "Omani Rial", "Umman Riyali")
        cur("price_myr", "MYR", "🇲🇾", "رینگیت مالزی", "Malaysian Ringgit", "Malezya Ringgiti")
        cur("price_gel", "GEL", "🇬🇪", "لاری گرجستان", "Georgian Lari", "Gürcü Larisi")
        cur("price_sar", "SAR", "🇸🇦", "ریال عربستان", "Saudi Riyal", "Suudi Riyali")
        cur("price_azn", "AZN", "🇦🇿", "منات آذربایجان", "Azerbaijani Manat", "Azerbaycan Manatı")
        cur("price_thb", "THB", "🇹🇭", "بات تایلند", "Thai Baht", "Tayland Bahtı")
        cur("price_bhd", "BHD", "🇧🇭", "دینار بحرین", "Bahraini Dinar", "Bahreyn Dinarı")
        cur("price_brl", "BRL", "🇧🇷", "ریال برزیل", "Brazilian Real", "Brezilya Reali")
        cur("price_ars", "ARS", "🇦🇷", "پزو آرژانتین", "Argentine Peso", "Arjantin Pesosu")

        // ---------- طلا و سکه ----------
        val g = 0xFFFFD54F
        gold("geram18", "18K", "18K", g, "طلای ۱۸ عیار (گرم)", "18K Gold per Gram", "18 Ayar Altın (Gram)")
        gold("geram24", "24K", "24K", g, "طلای ۲۴ عیار (گرم)", "24K Gold per Gram", "24 Ayar Altın (Gram)")
        gold("mesghal", "MESGHAL", "M", g, "مثقال طلا", "Gold (Mesghal)", "Mısqal Altın")
        gold("sekee", "EMAMI", "S", g, "سکه امامی", "Full Coin (Imami)", "Tam Sikke (İmami)")
        gold("sekeb", "AZADI", "B", g, "سکه بهار آزادی", "Bahar Azadi Coin", "Bahar Azadi Sikkesi")
        gold("nim", "NIM", "½", g, "نیم سکه", "Half Coin", "Yarım Sikke")
        gold("rob", "ROB", "¼", g, "ربع سکه", "Quarter Coin", "Çeyrek Sikke")
        gold("gerami", "GRAM", "1g", g, "سکه گرمی", "Gram Coin", "Gram Sikke")
        gold("ons", "XAU", "XAU", g, "انس طلا (دلار)", "Gold Ounce (USD)", "Altın Ons (USD)", U.USD)
        val s = 0xFFC9CED6
        gold("silver", "XAG", "XAG", s, "انس نقره (دلار)", "Silver Ounce (USD)", "Gümüş Ons (USD)", U.USD)
        gold("silver_999", "AG999", "999", s, "نقره ۹۹۹ (گرم)", "Silver 999 (Gram)", "Gümüş 999 (Gram)")
        gold("silver_925", "AG925", "925", s, "نقره ۹۲۵ (گرم)", "Silver 925 (Gram)", "Gümüş 925 (Gram)")

        // ---------- ارز دیجیتال (قیمت دلاری) ----------
        crypto("crypto-bitcoin", "BTC", "₿", 0xFFF7931A, "بیت‌کوین", "Bitcoin", "Bitcoin")
        crypto("crypto-ethereum", "ETH", "Ξ", 0xFF627EEA, "اتریوم", "Ethereum", "Ethereum")
        crypto("crypto-tether-irr", "USDT", "₮", 0xFF26A17B, "تتر (تومان)", "Tether (Toman)", "Tether (Toman)", U.TETHER)
        crypto("crypto-tether", "USDT", "₮", 0xFF26A17B, "تتر (دلار)", "Tether (USD)", "Tether (USD)")
        crypto("crypto-ripple", "XRP", "XRP", 0xFF23292F, "ریپل", "Ripple", "Ripple")
        crypto("crypto-binance-coin", "BNB", "BNB", 0xFFD9A400, "بایننس کوین", "Binance Coin", "Binance Coin")
        crypto("crypto-shiba-inu", "SHIB", "SHIB", 0xFFE42D04, "شیبا اینو", "Shiba Inu", "Shiba Inu")
        crypto("crypto-cardano", "ADA", "₳", 0xFF0033AD, "کاردانو", "Cardano", "Cardano")
        crypto("crypto-dogecoin", "DOGE", "Ð", 0xFFC2A633, "دوج‌کوین", "Dogecoin", "Dogecoin")
        crypto("crypto-toncoin", "TON", "TON", 0xFF0098EA, "تون‌کوین", "Toncoin", "Toncoin")
        crypto("crypto-solana", "SOL", "SOL", 0xFF2B2B3C, "سولانا", "Solana", "Solana")
        crypto("crypto-tron", "TRX", "TRX", 0xFFEB0029, "ترون", "TRON", "TRON")
        crypto("crypto-polkadot", "DOT", "DOT", 0xFFE6007A, "پولکادات", "Polkadot", "Polkadot")
        crypto("crypto-litecoin", "LTC", "Ł", 0xFF345D9D, "لایت‌کوین", "Litecoin", "Litecoin")
        crypto("crypto-chainlink", "LINK", "LINK", 0xFF2A5ADA, "چین‌لینک", "Chainlink", "Chainlink")
        crypto("crypto-avalanche", "AVAX", "AVAX", 0xFFE84142, "آوالانچ", "Avalanche", "Avalanche")
        crypto("crypto-stellar", "XLM", "XLM", 0xFF14B6E7, "استلار", "Stellar", "Stellar")
        crypto("crypto-monero", "XMR", "XMR", 0xFFFF6600, "مونرو", "Monero", "Monero")
        crypto("crypto-eos", "EOS", "EOS", 0xFF443F54, "ایاس", "EOS", "EOS")
        crypto("crypto-dash", "DASH", "DASH", 0xFF008CE7, "دش", "Dash", "Dash")
        crypto("crypto-bitcoin-cash", "BCH", "BCH", 0xFF8DC351, "بیت‌کوین کش", "Bitcoin Cash", "Bitcoin Cash")
        crypto("crypto-usd-coin", "USDC", "USDC", 0xFF2775CA, "یواس‌دی کوین", "USD Coin", "USD Coin")
        crypto("crypto-uniswap", "UNI", "UNI", 0xFFFF007A, "یونی‌سواپ", "Uniswap", "Uniswap")
        crypto("crypto-tezos", "XTZ", "ꜩ", 0xFF2C7DF7, "تزوس", "Tezos", "Tezos")
        crypto("crypto-sandbox", "SAND", "S", 0xFF04ADEF, "سندباکس", "The Sandbox", "The Sandbox")
        crypto("crypto-pancakeswap", "CAKE", "CAKE", 0xFFD1884F, "پنکیک‌سواپ", "Pancakeswap", "Pancakeswap")
        crypto("crypto-neo", "NEO", "NEO", 0xFF58BF00, "نئو", "Neo", "Neo")
        crypto("crypto-nem", "XEM", "XEM", 0xFF67B2E8, "نم", "NEM", "NEM")
        crypto("crypto-maker", "MKR", "MKR", 0xFF1AAB9B, "میکر", "Maker", "Maker")
        crypto("crypto-gala", "GALA", "GALA", 0xFF333333, "گالا", "Gala", "Gala")
        crypto("crypto-flow", "FLOW", "FLOW", 0xFF00B36B, "فلو", "Flow", "Flow")
        crypto("crypto-filecoin", "FIL", "FIL", 0xFF0090FF, "فایل‌کوین", "Filecoin", "Filecoin")
        crypto("crypto-fantom", "FTM", "FTM", 0xFF1969FF, "فانتوم", "Fantom", "Fantom")
        crypto("crypto-elrond", "EGLD", "EGLD", 0xFF23A8A0, "التروند", "Elrond", "Elrond")
        crypto("crypto-ethereum-classic", "ETC", "ETC", 0xFF328332, "اتریوم کلاسیک", "Ethereum Classic", "Ethereum Classic")
        crypto("crypto-decred", "DCR", "DCR", 0xFF2970FF, "دکرد", "Decred", "Decred")
        crypto("crypto-cosmos", "ATOM", "ATOM", 0xFF2E3148, "کازماس", "Cosmos", "Cosmos")
        crypto("crypto-bittorrent", "BTT", "BTT", 0xFF2F2F2F, "بیت‌تورنت", "BitTorrent", "BitTorrent")
        crypto("crypto-zcash", "ZEC", "ZEC", 0xFFC99A1D, "زی‌کش", "Zcash", "Zcash")
        crypto("crypto-waves", "WAVES", "WAVES", 0xFF0155FF, "ویوز", "Waves", "Waves")
    }
}
