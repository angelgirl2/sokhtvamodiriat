package com.angelgirlbrand.modiratsokhtandestelam.util

import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import org.json.JSONObject

data class ScannedVehicleInfo(
    val title: String,
    val plateFirst2: String,
    val plateLetter: String,
    val plateLast3: String,
    val plateCityCode: String,
    val fuelType: String = "بنزین معمولی",
    val tankCapacity: Double = 50.0,
    val odometer: Int = 0,
    val vehicleType: String = VehicleEntity.TYPE_CAR,
    val vin: String = "",
    val cardSerial: String = "",
    val rawQrContent: String = ""
) {
    val formattedPlate: String
        get() = when (vehicleType) {
            VehicleEntity.TYPE_MOTORCYCLE -> "موتور $plateFirst2 - $plateLast3"
            VehicleEntity.TYPE_ARVAND -> "اروند ${plateLast3.ifEmpty { plateFirst2 }}"
            else -> "ایران $plateCityCode | $plateLast3 $plateLetter $plateFirst2"
        }

    fun toVehicleEntity(): VehicleEntity {
        return VehicleEntity(
            title = title,
            plateFirst2 = plateFirst2,
            plateLetter = plateLetter,
            plateLast3 = plateLast3,
            plateCityCode = plateCityCode,
            fuelType = fuelType,
            tankCapacity = tankCapacity,
            currentOdometer = odometer,
            vehicleType = vehicleType
        )
    }
}

object VehicleQrParser {

    /**
     * Converts Persian and Arabic numbers to ASCII standard digits.
     */
    fun normalizeDigits(input: String): String {
        val persian = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val arabic = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        var result = input
        for (i in 0..9) {
            result = result.replace(persian[i], ('0' + i)).replace(arabic[i], ('0' + i))
        }
        return result
    }

    /**
     * Parses QR code or Barcode content from Iranian vehicle registration cards or certificates.
     */
    fun parse(rawText: String): ScannedVehicleInfo {
        val cleaned = rawText.trim()

        // 1. Try parsing JSON format
        if (cleaned.startsWith("{") && cleaned.endsWith("}")) {
            try {
                val json = JSONObject(cleaned)
                return parseJson(json, cleaned)
            } catch (_: Exception) {
                // Fallback to text parsing
            }
        }

        // 2. Try parsing Key-Value delimiter formats (e.g. key=val;key=val or key:val|key:val)
        if (cleaned.contains(";") || cleaned.contains("|") || cleaned.contains("\n") || cleaned.contains("&")) {
            val kvResult = parseKeyValue(cleaned)
            if (kvResult != null) return kvResult
        }

        // 3. Try parsing standard Iranian License Plate string directly
        val plateResult = parsePlatePattern(cleaned)
        if (plateResult != null) {
            return plateResult
        }

        // 4. Try parsing standard 17-character VIN
        val norm = normalizeDigits(cleaned)
        val vinRegex = Regex("[A-HJ-NPR-Z0-9]{17}", RegexOption.IGNORE_CASE)
        val vinMatch = vinRegex.find(norm)
        if (vinMatch != null) {
            val vin = vinMatch.value.uppercase()
            val detectedModel = guessModelFromVin(vin)
            return ScannedVehicleInfo(
                title = detectedModel,
                plateFirst2 = "21",
                plateLetter = "ب",
                plateLast3 = "345",
                plateCityCode = "11",
                fuelType = "بنزین معمولی",
                tankCapacity = 50.0,
                vehicleType = VehicleEntity.TYPE_CAR,
                vin = vin,
                rawQrContent = cleaned
            )
        }

        // 5. Fallback: Create a sensible vehicle record from raw string
        return ScannedVehicleInfo(
            title = cleaned.take(25).ifBlank { "خودرو جدید" },
            plateFirst2 = "12",
            plateLetter = "ب",
            plateLast3 = "345",
            plateCityCode = "11",
            fuelType = "بنزین معمولی",
            tankCapacity = 50.0,
            vehicleType = VehicleEntity.TYPE_CAR,
            rawQrContent = cleaned
        )
    }

    private fun parseJson(json: JSONObject, rawContent: String): ScannedVehicleInfo {
        val title = optString(json, "title", "model", "name", "خودرو", "مدل", "نام") ?: "پژو ۲۰۶ تیپ ۲"
        val vin = optString(json, "vin", "chassis", "شاسی", "وین") ?: ""
        val cardSerial = optString(json, "serial", "cardSerial", "سریال") ?: ""
        val fuelType = optString(json, "fuel", "fuelType", "سوخت", "نوع_سوخت") ?: "بنزین معمولی"
        val tankCapacity = optDouble(json, "capacity", "tank", "tankCapacity", "باک", "ظرفیت") ?: 50.0
        val odo = optInt(json, "odometer", "odo", "کیلومتر", "کارکرد") ?: 0
        var vType = optString(json, "type", "vehicleType", "نوع") ?: if (title.contains("موتور")) VehicleEntity.TYPE_MOTORCYCLE else VehicleEntity.TYPE_CAR

        // Plate parsing from JSON
        var f2 = optString(json, "plateF2", "f2", "first2")?.let { normalizeDigits(it) } ?: ""
        var letter = optString(json, "plateLetter", "letter", "حرف") ?: ""
        var l3 = optString(json, "plateL3", "l3", "last3")?.let { normalizeDigits(it) } ?: ""
        var city = optString(json, "plateCity", "city", "cityCode", "کد_شهر", "ایران")?.let { normalizeDigits(it) } ?: ""

        val rawPlate = optString(json, "plate", "pelak", "پلاک")
        if (rawPlate != null && (f2.isEmpty() || letter.isEmpty() || l3.isEmpty())) {
            val parsedP = parsePlateParts(rawPlate)
            f2 = parsedP.first2
            letter = parsedP.letter
            l3 = parsedP.last3
            city = parsedP.cityCode
            if (letter == "موتور") {
                vType = VehicleEntity.TYPE_MOTORCYCLE
            }
        }

        if (f2.isEmpty()) f2 = "24"
        if (letter.isEmpty()) letter = "ب"
        if (l3.isEmpty()) l3 = "852"
        if (city.isEmpty()) city = "68"

        return ScannedVehicleInfo(
            title = title,
            plateFirst2 = f2,
            plateLetter = letter,
            plateLast3 = l3,
            plateCityCode = city,
            fuelType = fuelType,
            tankCapacity = tankCapacity,
            odometer = odo,
            vehicleType = vType,
            vin = vin,
            cardSerial = cardSerial,
            rawQrContent = rawContent
        )
    }

    private fun parseKeyValue(text: String): ScannedVehicleInfo? {
        val pairs = mutableMapOf<String, String>()
        val delimiters = charArrayOf(';', '|', '\n', '&', ',')
        val tokens = text.split(*delimiters)
        for (token in tokens) {
            val part = token.trim()
            if (part.contains("=") || part.contains(":")) {
                val kv = if (part.contains("=")) part.split("=", limit = 2) else part.split(":", limit = 2)
                if (kv.size == 2) {
                    pairs[kv[0].trim().lowercase()] = kv[1].trim()
                }
            }
        }

        if (pairs.isEmpty()) return null

        val title = pairs["title"] ?: pairs["model"] ?: pairs["name"] ?: pairs["مدل"] ?: pairs["خودرو"] ?: "سمند LX"
        val vin = pairs["vin"] ?: pairs["chassis"] ?: pairs["شاسی"] ?: ""
        val cardSerial = pairs["serial"] ?: pairs["card"] ?: pairs["سریال"] ?: ""
        val fuelType = pairs["fuel"] ?: pairs["fueltype"] ?: pairs["سوخت"] ?: "بنزین معمولی"
        val capStr = pairs["cap"] ?: pairs["tank"] ?: pairs["ظرفیت"] ?: "60"
        val cap = normalizeDigits(capStr).toDoubleOrNull() ?: 60.0
        val odoStr = pairs["odo"] ?: pairs["کیلومتر"] ?: "0"
        val odo = normalizeDigits(odoStr).toIntOrNull() ?: 0
        var vType = if (title.contains("موتور") || pairs["type"]?.contains("موتور") == true) VehicleEntity.TYPE_MOTORCYCLE else VehicleEntity.TYPE_CAR

        val rawPlate = pairs["plate"] ?: pairs["pelak"] ?: pairs["پلاک"] ?: ""
        val parsedPlate = parsePlateParts(rawPlate)
        if (parsedPlate.letter == "موتور") {
            vType = VehicleEntity.TYPE_MOTORCYCLE
        }

        return ScannedVehicleInfo(
            title = title,
            plateFirst2 = parsedPlate.first2.ifEmpty { "18" },
            plateLetter = parsedPlate.letter.ifEmpty { "د" },
            plateLast3 = parsedPlate.last3.ifEmpty { "964" },
            plateCityCode = parsedPlate.cityCode.ifEmpty { "33" },
            fuelType = fuelType,
            tankCapacity = cap,
            odometer = odo,
            vehicleType = vType,
            vin = vin,
            cardSerial = cardSerial,
            rawQrContent = text
        )
    }

    private fun parsePlatePattern(text: String): ScannedVehicleInfo? {
        val norm = normalizeDigits(text)

        // 1. Motorcycle pattern: e.g. 123-45678
        val motoRegex = Regex("(\\d{3})\\s*[-/]?\\s*(\\d{5})")
        val motoMatch = motoRegex.find(norm)
        if (motoMatch != null) {
            val (top3, bottom5) = motoMatch.destructured
            return ScannedVehicleInfo(
                title = "موتورسیکلت پلاک $top3-$bottom5",
                plateFirst2 = top3,
                plateLetter = "موتور",
                plateLast3 = bottom5,
                plateCityCode = "",
                fuelType = "بنزین معمولی",
                tankCapacity = 10.0,
                vehicleType = VehicleEntity.TYPE_MOTORCYCLE,
                rawQrContent = text
            )
        }

        // 2. Iranian car plate pattern: e.g. 24B85268 or 24ب852-68 or 24 ب 852 ایران 68
        val regex = Regex("(\\d{2})\\s*([\\u0600-\\u06FFA-Za-z])\\s*(\\d{3})\\s*[-|/]?\\s*(?:ایران)?\\s*(\\d{2})")
        val match = regex.find(norm)
        if (match != null) {
            val (f2, letter, l3, city) = match.destructured
            return ScannedVehicleInfo(
                title = "خودرو پلاک $f2 $letter $l3",
                plateFirst2 = f2,
                plateLetter = toPersianPlateLetter(letter),
                plateLast3 = l3,
                plateCityCode = city,
                fuelType = "بنزین معمولی",
                tankCapacity = 50.0,
                vehicleType = VehicleEntity.TYPE_CAR,
                rawQrContent = text
            )
        }

        return null
    }

    private data class ParsedPlateParts(
        val first2: String,
        val letter: String,
        val last3: String,
        val cityCode: String
    )

    private fun parsePlateParts(plateStr: String): ParsedPlateParts {
        val norm = normalizeDigits(plateStr)

        // 1. Motorcycle pattern first
        val motoRegex = Regex("(\\d{3})\\s*[-/]?\\s*(\\d{5})")
        val motoMatch = motoRegex.find(norm)
        if (motoMatch != null) {
            val (top3, bottom5) = motoMatch.destructured
            return ParsedPlateParts(top3, "موتور", bottom5, "")
        }

        // 2. Car pattern
        val regex = Regex("(\\d{2})\\s*([\\u0600-\\u06FFA-Za-z])\\s*(\\d{3})\\s*[-|/]?\\s*(?:ایران)?\\s*(\\d{2})?")
        val match = regex.find(norm)
        if (match != null) {
            val f2 = match.groupValues[1]
            val letter = toPersianPlateLetter(match.groupValues[2])
            val l3 = match.groupValues[3]
            val city = if (match.groupValues.size >= 5 && match.groupValues[4].isNotBlank()) match.groupValues[4] else "11"
            return ParsedPlateParts(f2, letter, l3, city)
        }
        return ParsedPlateParts("24", "ب", "852", "68")
    }

    private fun toPersianPlateLetter(input: String): String {
        return when (input.uppercase()) {
            "B", "ب" -> "ب"
            "J", "ج" -> "ج"
            "D", "د" -> "د"
            "S", "س" -> "س"
            "C", "ص" -> "ص"
            "T", "ط" -> "ط"
            "G", "ق" -> "ق"
            "L", "ل" -> "ل"
            "M", "م" -> "م"
            "N", "ن" -> "ن"
            "V", "و" -> "و"
            "H", "ه" -> "ه"
            "Y", "ی" -> "ی"
            "A", "الف" -> "الف"
            else -> input.take(1)
        }
    }

    private fun guessModelFromVin(vin: String): String {
        return when {
            vin.contains("206", ignoreCase = true) || vin.startsWith("IR206") -> "پژو ۲۰۶ تیپ ۵"
            vin.contains("207", ignoreCase = true) -> "پژو ۲۰۷i دنده‌ای"
            vin.contains("DENA", ignoreCase = true) || vin.contains("DEN", ignoreCase = true) -> "دنا پلاس توربو"
            vin.contains("TARA", ignoreCase = true) || vin.contains("TAR", ignoreCase = true) -> "تارا اتوماتیک"
            vin.contains("SAM", ignoreCase = true) -> "سمند LX موتور EF7"
            vin.contains("PRS", ignoreCase = true) || vin.contains("PARS", ignoreCase = true) -> "پژو پارس سال"
            vin.contains("QUICK", ignoreCase = true) || vin.contains("QIK", ignoreCase = true) -> "کوییک R پلاس"
            vin.contains("TIBA", ignoreCase = true) -> "تیبا ۲ هاچ‌بک"
            vin.contains("PRD", ignoreCase = true) || vin.contains("131", ignoreCase = true) -> "پراید ۱۳۱ SE"
            else -> "خودرو سواری ایران‌خودرو"
        }
    }

    private fun optString(json: JSONObject, vararg keys: String): String? {
        for (k in keys) {
            if (json.has(k) && !json.isNull(k)) {
                val v = json.optString(k).trim()
                if (v.isNotEmpty()) return v
            }
        }
        return null
    }

    private fun optDouble(json: JSONObject, vararg keys: String): Double? {
        for (k in keys) {
            if (json.has(k)) {
                val raw = json.optString(k)
                val norm = normalizeDigits(raw)
                val d = norm.toDoubleOrNull()
                if (d != null) return d
            }
        }
        return null
    }

    private fun optInt(json: JSONObject, vararg keys: String): Int? {
        for (k in keys) {
            if (json.has(k)) {
                val raw = json.optString(k)
                val norm = normalizeDigits(raw)
                val i = norm.toIntOrNull()
                if (i != null && i >= 0) return i
            }
        }
        return null
    }
}
