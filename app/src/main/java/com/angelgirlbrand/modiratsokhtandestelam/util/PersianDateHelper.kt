package com.angelgirlbrand.modiratsokhtandestelam.util

import java.util.Calendar
import java.util.TimeZone

/**
 * Comprehensive Solar Hijri (Shamsi / Jalali) Date & Time utility.
 * Formats all application timestamps into standard Persian dates, times, and month names.
 */
object PersianDateHelper {

    val PERSIAN_MONTH_NAMES = arrayOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(PERSIAN_DIGITS[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    data class PersianDate(
        val year: Int,
        val month: Int,
        val day: Int,
        val hour: Int = 0,
        val minute: Int = 0
    ) {
        val monthName: String
            get() = if (month in 1..12) PERSIAN_MONTH_NAMES[month - 1] else ""

        fun formatDate(): String {
            return String.format("%04d/%02d/%02d", year, month, day)
        }

        fun formatDateTime(): String {
            return String.format("%04d/%02d/%02d - %02d:%02d", year, month, day, hour, minute)
        }

        fun formatLongDate(): String {
            return "$day $monthName $year"
        }

        fun formatTime(): String {
            return String.format("%02d:%02d", hour, minute)
        }
    }

    /**
     * Converts a millisecond timestamp to PersianDate in Asia/Tehran timezone.
     */
    fun getPersianDate(timestampMillis: Long): PersianDate {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        cal.timeInMillis = timestampMillis
        val gYear = cal.get(Calendar.YEAR)
        val gMonth = cal.get(Calendar.MONTH) + 1
        val gDay = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        val (jYear, jMonth, jDay) = gregorianToJalali(gYear, gMonth, gDay)
        return PersianDate(jYear, jMonth, jDay, hour, minute)
    }

    /**
     * Standard Astronomical Gregorian to Jalali conversion algorithm.
     */
    fun gregorianToJalali(gYear: Int, gMonth: Int, gDay: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        val gy = gYear - 1600
        val gm = gMonth - 1
        val gd = gDay - 1

        var gDayNo = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400
        for (i in 0 until gm) {
            gDayNo += gDaysInMonth[i]
        }
        if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        for (i in 0 until 11) {
            if (jDayNo >= jDaysInMonth[i]) {
                jDayNo -= jDaysInMonth[i]
            } else {
                jm = i
                break
            }
            if (i == 10) {
                jm = 11
            }
        }
        val jd = jDayNo + 1
        return Triple(jy, jm + 1, jd)
    }

    /**
     * Returns formatted Shamsi date (e.g. "1403/07/15")
     */
    fun toPersianDate(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return "-"
        return getPersianDate(timestampMillis).formatDate()
    }

    /**
     * Returns formatted Shamsi date and time (e.g. "1403/07/15 - 14:30")
     */
    fun toPersianDateTime(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return "-"
        return getPersianDate(timestampMillis).formatDateTime()
    }

    /**
     * Returns formatted Shamsi long date with month name (e.g. "15 مهر 1403")
     */
    fun toPersianDateWithMonth(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return "-"
        return getPersianDate(timestampMillis).formatLongDate()
    }

    /**
     * Returns formatted Persian time (e.g. "14:30")
     */
    fun toPersianTime(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return "-"
        return getPersianDate(timestampMillis).formatTime()
    }
}
