package com.angelgirlbrand.modiratsokhtandestelam.security

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PriceCategory(val title: String) {
    ALL("همه خدمات"),
    INQUIRY("استعلام‌ها و خلافی خودرو"),
    INSURANCE("خرید و صدور انواع بیمه")
}

data class ServicePrice(
    val key: String,
    val persianName: String,
    val defaultPriceText: String,
    val defaultDescription: String,
    val category: PriceCategory = PriceCategory.INQUIRY,
    val defaultNumericPrice: Long = 0L
) {
    val description: String get() = defaultDescription
}

data class TariffServiceData(
    val key: String,
    val persianName: String,
    val priceText: String,
    val description: String,
    val category: PriceCategory,
    val lastUpdated: Long = System.currentTimeMillis()
)

class PriceManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("fuel_service_prices", Context.MODE_PRIVATE)

    init {
        updateStateFlow()
    }

    companion object {
        val SERVICES = listOf(
            // ================= 1. استعلام‌ها و عوارض خودرو (Inquiries & Tolls) =================
            ServicePrice(
                key = "car_fine_inquiry",
                persianName = "استعلام جامع خلافی خودرو",
                defaultPriceText = "۱۵,۰۰۰ تومان",
                defaultDescription = "بررسی آنی تخلفات راهور، تصاویر ثبت تخلف، شناسه قبض و تسویه لحظه‌ای",
                category = PriceCategory.INQUIRY,
                defaultNumericPrice = 15000L
            ),
            ServicePrice(
                key = "motorcycle_fine_inquiry",
                persianName = "استعلام خلافی موتورسیکلت",
                defaultPriceText = "۱۰,۰۰۰ تومان",
                defaultDescription = "بررسی جرائم رانندگی پلاک موتور و تسویه حساب فوری با راهور",
                category = PriceCategory.INQUIRY,
                defaultNumericPrice = 10000L
            ),
            ServicePrice(
                key = "highway_tolls_inquiry",
                persianName = "استعلام و تسویه عوارض آزادراهی",
                defaultPriceText = "۸,۰۰۰ تومان",
                defaultDescription = "بررسی ترددها، عوارض آزادراه‌های کشور و جلوگیری از جریمه دیرکرد تردد",
                category = PriceCategory.INQUIRY,
                defaultNumericPrice = 8000L
            ),
            ServicePrice(
                key = "negative_points_inquiry",
                persianName = "استعلام نمره منفی گواهی‌نامه",
                defaultPriceText = "۱۲,۰۰۰ تومان",
                defaultDescription = "بررسی نمره منفی ثبت‌شده راننده، تخلفات حادثه‌ساز و وضعیت تعلیق",
                category = PriceCategory.INQUIRY,
                defaultNumericPrice = 12000L
            ),
            ServicePrice(
                key = "license_status_inquiry",
                persianName = "استعلام وضعیت گواهی‌نامه و سوابق",
                defaultPriceText = "۱۵,۰۰۰ تومان",
                defaultDescription = "بررسی تاریخ انقضا، اعتبار قانونی گواهی‌نامه و وضعیت پستی کارت",
                category = PriceCategory.INQUIRY,
                defaultNumericPrice = 15000L
            ),
            ServicePrice(
                key = "technical_inspection_inquiry",
                persianName = "استعلام معاینه فنی خودرو",
                defaultPriceText = "۱۵,۰۰۰ تومان",
                defaultDescription = "بررسی اصالت و تاریخ اعتبار معاینه فنی در سامانه یکپارچه سیمفا",
                category = PriceCategory.INQUIRY,
                defaultNumericPrice = 15000L
            ),
            ServicePrice(
                key = "vehicle_documents_inquiry",
                persianName = "استعلام اسناد خودرو و پلاک فعال",
                defaultPriceText = "۲۰,۰۰۰ تومان",
                defaultDescription = "بررسی اصالت برگ سبز، مشخصات مالک ثبتی و تعداد پلاک‌های فعال بنام",
                category = PriceCategory.INQUIRY,
                defaultNumericPrice = 20000L
            ),
            ServicePrice(
                key = "annual_tolls_inquiry",
                persianName = "استعلام عوارض سالیانه شهرداری",
                defaultPriceText = "۱۲,۰۰۰ تومان",
                defaultDescription = "بررسی بدهی عوارض سالیانه و نوسازی خودرو در سامانه سمیع و شهرداری‌ها",
                category = PriceCategory.INQUIRY,
                defaultNumericPrice = 12000L
            ),
            ServicePrice(
                key = "transfer_tax_inquiry",
                persianName = "استعلام مالیات نقل و انتقال خودرو",
                defaultPriceText = "۲۵,۰۰۰ تومان",
                defaultDescription = "محاسبه و پرداخت برخط مالیات پیش از معامله و تعویض پلاک در سامانه امور مالیاتی",
                category = PriceCategory.INQUIRY,
                defaultNumericPrice = 25000L
            ),
            ServicePrice(
                key = "fuel_card_inquiry",
                persianName = "استعلام و پیگیری کارت سوخت",
                defaultPriceText = "۴۵,۰۰۰ تومان",
                defaultDescription = "پیگیری بارکد پستی، نوبت صدور کارت سوخت المثنی و نو شماره از سامانه شرکت نفت",
                category = PriceCategory.INQUIRY,
                defaultNumericPrice = 45000L
            ),

            // ================= 2. خرید و صدور انواع بیمه‌نامه (Insurance Purchase) =================
            ServicePrice(
                key = "third_party_car_insurance",
                persianName = "خرید و تمدید بیمه شخص ثالث خودرو",
                defaultPriceText = "استعلام نرخ و صدور آنلاین",
                defaultDescription = "تمدید فوری بیمه‌نامه شخص ثالث با اعمال سوابق عدم خسارت و صدور بیمه‌نامه سنهاب",
                category = PriceCategory.INSURANCE,
                defaultNumericPrice = 85000L
            ),
            ServicePrice(
                key = "body_car_insurance",
                persianName = "خرید و صدور بیمه بدنه خودرو",
                defaultPriceText = "کارشناسی آنلاین و استعلام",
                defaultDescription = "صدور بیمه‌نامه بدنه با پوشش حوادث، سرقت، آتش‌سوزی و بلایای طبیعی با بازدید برخط",
                category = PriceCategory.INSURANCE,
                defaultNumericPrice = 120000L
            ),
            ServicePrice(
                key = "motorcycle_insurance",
                persianName = "خرید و تمدید بیمه موتورسیکلت",
                defaultPriceText = "استعلام نرخ و صدور آنی",
                defaultDescription = "صدور بیمه شخص ثالث انواع موتورسیکلت با بخشودگی جرائم و پوشش کامل جانی و مالی",
                category = PriceCategory.INSURANCE,
                defaultNumericPrice = 50000L
            ),
            ServicePrice(
                key = "driver_incident_insurance",
                persianName = "بیمه حوادث راننده",
                defaultPriceText = "پوشش تکمیلی مصوب",
                defaultDescription = "پوشش غرامت فوت و نقص عضو راننده مقصر حادثه مطابق آخرین دیه ابلاغی قوه قضاییه",
                category = PriceCategory.INSURANCE,
                defaultNumericPrice = 30000L
            )
        )

        private val _tariffDataState = MutableStateFlow<Map<String, TariffServiceData>>(emptyMap())
        val tariffDataState: StateFlow<Map<String, TariffServiceData>> = _tariffDataState.asStateFlow()

        private val _pricesState = MutableStateFlow<Map<String, String>>(emptyMap())
        val pricesState: StateFlow<Map<String, String>> = _pricesState.asStateFlow()
    }

    fun updateStateFlow() {
        val map = SERVICES.associate { service ->
            service.key to TariffServiceData(
                key = service.key,
                persianName = service.persianName,
                priceText = getPriceText(service.key),
                description = getDescription(service.key),
                category = service.category
            )
        }
        _tariffDataState.value = map
        _pricesState.value = map.mapValues { it.value.priceText }
    }

    /**
     * Returns custom text/price for service. Can be any text (e.g. "توافقی", "تماس بگیرید", "150000 تومان", etc.)
     */
    fun getPriceText(key: String): String {
        val service = SERVICES.firstOrNull { it.key == key } ?: return "استعلام آنلاین"
        val customText = prefs.getString("price_text_$key", null)
        if (!customText.isNullOrBlank()) {
            return customText
        }
        // Fallback to legacy numeric price if set
        val legacyLong = prefs.getLong("price_$key", -1L)
        if (legacyLong != -1L) {
            return "${String.format("%,d", legacyLong)} تومان"
        }
        return service.defaultPriceText
    }

    /**
     * Sets any text as price (can be numeric or any custom Persian text)
     */
    fun setPriceText(key: String, text: String) {
        val clean = text.trim()
        prefs.edit().putString("price_text_$key", clean).apply()
        updateStateFlow()
    }

    /**
     * Returns current description for service (can be customized from Bale bot)
     */
    fun getDescription(key: String): String {
        val service = SERVICES.firstOrNull { it.key == key } ?: return ""
        val customDesc = prefs.getString("desc_$key", null)
        if (!customDesc.isNullOrBlank()) {
            return customDesc
        }
        return service.defaultDescription
    }

    /**
     * Sets custom description for service from Bale bot
     */
    fun setDescription(key: String, description: String) {
        val clean = description.trim()
        prefs.edit().putString("desc_$key", clean).apply()
        updateStateFlow()
    }

    fun getPrice(key: String): Long {
        val service = SERVICES.firstOrNull { it.key == key } ?: return 0L
        val text = getPriceText(key)
        val digitsOnly = text.filter { it.isDigit() }
        return digitsOnly.toLongOrNull() ?: prefs.getLong("price_$key", service.defaultNumericPrice)
    }

    fun setPrice(key: String, price: Long) {
        prefs.edit().putLong("price_$key", price).apply()
        setPriceText(key, "${String.format("%,d", price)} تومان")
    }

    /**
     * Formats the complete inquiries & insurance tariff into Markdown for Bale bot admin response
     */
    fun buildBaleTariffSummary(): String = buildString {
        append("📋 *فهرست کامل هزینه‌های استعلام‌ها و خرید بیمه:*\n\n")

        append("🔍 *استعلام‌ها و عوارض خودرو:*\n")
        SERVICES.filter { it.category == PriceCategory.INQUIRY }.forEach { sp ->
            val currentVal = getPriceText(sp.key)
            val currentDesc = getDescription(sp.key)
            append("• *${sp.persianName}:* $currentVal\n")
            append("  📝 توضیحات: $currentDesc\n")
            append("  💰 تغییر قیمت: `/setprice ${sp.key} [مقدار]`\n")
            append("  ✍️ تغییر توضیحات: `/setdesc ${sp.key} [متن توضیحات]`\n\n")
        }

        append("🛡 *خرید و صدور انواع بیمه‌نامه:*\n")
        SERVICES.filter { it.category == PriceCategory.INSURANCE }.forEach { sp ->
            val currentVal = getPriceText(sp.key)
            val currentDesc = getDescription(sp.key)
            append("• *${sp.persianName}:* $currentVal\n")
            append("  📝 توضیحات: $currentDesc\n")
            append("  💰 تغییر قیمت: `/setprice ${sp.key} [مقدار]`\n")
            append("  ✍️ تغییر توضیحات: `/setdesc ${sp.key} [متن توضیحات]`\n\n")
        }

        append("💡 *راهنمای دستورات ربات بله برای مدیر:*\n")
        append("۱. برای تغییر مبلغ یا متن قیمت هر خدمت:\n")
        append("`/setprice [کلید] [هر متنی یا مبلغی]`\n")
        append("مثال: `/setprice car_fine_inquiry ۲۰,۰۰۰ تومان` یا `/setprice third_party_car_insurance توافقی`\n\n")
        append("۲. برای تغییر متن توضیحات هر خدمت:\n")
        append("`/setdesc [کلید] [متن جدید توضیحات]` یا `/توضیحات [کلید] [متن]`\n")
        append("مثال: `/setdesc car_fine_inquiry تسویه آنی همراه با تصویر دوربین راهور`\n\n")
        append("⚡ تغییرات بلافاصله به صورت خودکار و درجا در برنامه جایگزین خواهند شد. ✅")
    }
}
