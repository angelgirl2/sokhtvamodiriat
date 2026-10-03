package com.angelgirlbrand.modiratsokhtandestelam.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.FuelLogEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.ServiceHistoryEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.ServiceReminderEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReportExporter {

    // =========================================================================
    // 1. PDF EXPORT (COMPREHENSIVE FUEL & SERVICE HISTORY DOSSIER)
    // =========================================================================
    fun exportComprehensivePdf(
        context: Context,
        vehicles: List<VehicleEntity>,
        logs: List<FuelLogEntity>,
        serviceHistory: List<ServiceHistoryEntity> = emptyList(),
        reminders: List<ServiceReminderEntity> = emptyList()
    ): File? {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 portrait
        val page = pdfDoc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(2, 132, 199)
            textSize = 16f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val subPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 9.5f
            textAlign = Paint.Align.CENTER
        }
        val sectionTitlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 11.5f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        val headerPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 9.5f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        val textPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 8.5f
            textAlign = Paint.Align.RIGHT
        }
        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 0.8f
        }
        val rectPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
        }

        // Header Background
        canvas.drawRoundRect(20f, 15f, 575f, 75f, 8f, 8f, rectPaint)
        canvas.drawText("گزارش جامع مدیریت خودرو، سوخت و دفترچه سرویس دوره‌ای", 595f / 2, 42f, titlePaint)
        val dateFormat = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault())
        canvas.drawText("تاریخ ایجاد گزارش: ${dateFormat.format(Date())} | پایش هوشمند خودرو", 595f / 2, 62f, subPaint)

        // Summary Statistics Box
        var y = 88f
        canvas.drawRoundRect(20f, y, 575f, y + 55f, 8f, 8f, Paint().apply { color = Color.rgb(224, 242, 254) })
        val totalLiters = logs.sumOf { it.liters }
        val totalFuelCost = logs.sumOf { it.totalCost }
        val totalServiceCost = serviceHistory.sumOf { it.cost }
        val vehicleMap = vehicles.associateBy { it.id }

        val statPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("خودروها: ${vehicles.size} دستگاه", 550f, y + 22f, statPaint)
        canvas.drawText("مجموع بنزین: %.1f لیتر".format(totalLiters), 380f, y + 22f, statPaint)
        canvas.drawText("هزینه سوخت: %,d تومان".format(totalFuelCost), 200f, y + 22f, statPaint)

        canvas.drawText("سوابق سوخت‌گیری: ${logs.size} نوبت", 550f, y + 42f, statPaint)
        canvas.drawText("سوابق سرویس: ${serviceHistory.size} مورد", 380f, y + 42f, statPaint)
        canvas.drawText("هزینه سرویس‌ها: %,d تومان".format(totalServiceCost), 200f, y + 42f, statPaint)

        // Section 1: Service History Table
        y += 75f
        canvas.drawText("۱. آخرین سوابق سرویس دوره‌ای و تعویض قطعات", 570f, y, sectionTitlePaint)
        y += 12f
        canvas.drawRect(20f, y, 575f, y + 18f, Paint().apply { color = Color.rgb(241, 245, 249) })
        canvas.drawText("ردیف", 560f, y + 13f, headerPaint)
        canvas.drawText("سرویس", 515f, y + 13f, headerPaint)
        canvas.drawText("اقلام تعویضی", 390f, y + 13f, headerPaint)
        canvas.drawText("کیلومتر", 250f, y + 13f, headerPaint)
        canvas.drawText("سرویس بعدی", 175f, y + 13f, headerPaint)
        canvas.drawText("هزینه (تومان)", 85f, y + 13f, headerPaint)
        canvas.drawLine(20f, y + 18f, 575f, y + 18f, linePaint)

        y += 26f
        val logDateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val historyToDraw = serviceHistory.take(8)
        if (historyToDraw.isEmpty()) {
            canvas.drawText("هیچ سابقه سرویس دوره‌ای ثبت نشده است.", 550f, y + 4f, textPaint)
            y += 18f
        } else {
            historyToDraw.forEachIndexed { idx, s ->
                canvas.drawText("${idx + 1}", 560f, y, textPaint)
                canvas.drawText(s.serviceType.take(15), 515f, y, textPaint)
                canvas.drawText(s.itemsChanged.take(24).ifBlank { "سرویس عمومی" }, 390f, y, textPaint)
                canvas.drawText("%,d".format(s.odometer), 250f, y, textPaint)
                canvas.drawText(if (s.nextDueOdometer > 0) "%,d".format(s.nextDueOdometer) else "---", 175f, y, textPaint)
                canvas.drawText("%,d".format(s.cost), 85f, y, textPaint)
                canvas.drawLine(20f, y + 4f, 575f, y + 4f, linePaint)
                y += 16f
            }
        }

        // Section 2: Fuel Consumption Logs Table
        y += 20f
        canvas.drawText("۲. تاریخچه سوخت‌گیری‌های اخیر خودرو", 570f, y, sectionTitlePaint)
        y += 12f
        canvas.drawRect(20f, y, 575f, y + 18f, Paint().apply { color = Color.rgb(224, 242, 254) })
        canvas.drawText("ردیف", 560f, y + 13f, headerPaint)
        canvas.drawText("خودرو", 515f, y + 13f, headerPaint)
        canvas.drawText("تاریخ", 435f, y + 13f, headerPaint)
        canvas.drawText("کیلومتر", 345f, y + 13f, headerPaint)
        canvas.drawText("لیتر", 265f, y + 13f, headerPaint)
        canvas.drawText("فی (تومان)", 185f, y + 13f, headerPaint)
        canvas.drawText("مبلغ پرداختی", 95f, y + 13f, headerPaint)
        canvas.drawLine(20f, y + 18f, 575f, y + 18f, linePaint)

        y += 26f
        val logsToDraw = logs.take(16)
        if (logsToDraw.isEmpty()) {
            canvas.drawText("هیچ سابقه سوخت‌گیری ثبت نشده است.", 550f, y + 4f, textPaint)
            y += 18f
        } else {
            logsToDraw.forEachIndexed { index, log ->
                val vTitle = vehicleMap[log.vehicleId]?.title ?: "خودرو"
                canvas.drawText("${index + 1}", 560f, y, textPaint)
                canvas.drawText(vTitle.take(12), 515f, y, textPaint)
                canvas.drawText(logDateFormat.format(Date(log.dateMillis)), 435f, y, textPaint)
                canvas.drawText("%,d".format(log.odometer), 345f, y, textPaint)
                canvas.drawText("%.1f".format(log.liters), 265f, y, textPaint)
                canvas.drawText("%,d".format(log.pricePerLiter), 185f, y, textPaint)
                canvas.drawText("%,d".format(log.totalCost), 95f, y, textPaint)
                canvas.drawLine(20f, y + 4f, 575f, y + 4f, linePaint)
                y += 16f
            }
        }

        // Footer
        canvas.drawText("صادر شده از سامانه مدیریت سوخت و خدمات خودرو | نسخه اختصاصی", 595f / 2, 825f, subPaint)

        pdfDoc.finishPage(page)

        return try {
            val reportsDir = File(context.getExternalFilesDir(null), "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()
            val file = File(reportsDir, "Car_Report_${System.currentTimeMillis()}.pdf")
            pdfDoc.writeTo(FileOutputStream(file))
            pdfDoc.close()
            file
        } catch (e: Exception) {
            pdfDoc.close()
            null
        }
    }

    // =========================================================================
    // 2. TEXT REPORT GENERATORS (FOR SOCIAL MESSENGERS & COPY)
    // =========================================================================
    fun generateFullDossierTextReport(
        vehicle: VehicleEntity?,
        logs: List<FuelLogEntity>,
        serviceHistory: List<ServiceHistoryEntity>,
        reminders: List<ServiceReminderEntity>
    ): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()

        sb.append("📋 گزارش پرونده هوشمند خودرو و مصرف سوخت\n")
        sb.append("═══════════════════════════\n")
        sb.append("📅 تاریخ ایجاد: ${timeFormat.format(Date())}\n")

        if (vehicle != null) {
            sb.append("\n🚗 مشخصات خودرو:\n")
            sb.append("• عنوان: ${vehicle.title}\n")
            sb.append("• پلاک: ${vehicle.formattedPlate}\n")
            sb.append("• نوع سوخت: ${vehicle.fuelType}\n")
            sb.append("• کارکرد فعلی: %,d کیلومتر\n".format(vehicle.currentOdometer))
            sb.append("• ظرفیت باک: %.1f لیتر\n".format(vehicle.tankCapacity))

            val expiryDays = ((vehicle.effectiveInspectionExpiryMillis - System.currentTimeMillis()) / (24L * 3600 * 1000)).toInt()
            val inspStatus = when {
                expiryDays < 0 -> "منقضی شده 🔴"
                expiryDays <= 15 -> "نزدیک به انقضا ($expiryDays روز مانده) 🟡"
                else -> "معتبر ($expiryDays روز مانده) 🟢"
            }
            sb.append("• وضعیت معاینه فنی: $inspStatus\n")
            sb.append("• مرکز معاینه: ${vehicle.inspectionCenter.ifBlank { "مرکز بیهقی" }}\n")
        }

        val totalLiters = logs.sumOf { it.liters }
        val totalFuelCost = logs.sumOf { it.totalCost }
        val totalServiceCost = serviceHistory.sumOf { it.cost }

        sb.append("\n📊 خلاصه آمار و هزینه‌ها:\n")
        sb.append("• کل بنزین مصرفی: %.1f لیتر\n".format(totalLiters))
        sb.append("• کل هزینه سوخت‌گیری: %,d تومان\n".format(totalFuelCost))
        sb.append("• کل هزینه سرویس و نگهداری: %,d تومان\n".format(totalServiceCost))
        sb.append("• مجموع کل مخارج خودرو: %,d تومان\n".format(totalFuelCost + totalServiceCost))

        if (serviceHistory.isNotEmpty()) {
            sb.append("\n🔧 سوابق سرویس‌های دوره‌ای اخیر (${serviceHistory.size} مورد):\n")
            serviceHistory.take(5).forEachIndexed { idx, s ->
                sb.append("${idx + 1}. [${dateFormat.format(Date(s.dateMillis))}] ${s.serviceType}\n")
                sb.append("   - کیلومتر: %,d km | بعدی: %,d km\n".format(s.odometer, s.nextDueOdometer))
                if (s.itemsChanged.isNotBlank()) sb.append("   - اقلام: ${s.itemsChanged}\n")
                sb.append("   - هزینه: %,d تومان | تعمیرگاه: ${s.mechanicOrShop.ifBlank { "ثبت نشده" }}\n".format(s.cost))
            }
        }

        if (logs.isNotEmpty()) {
            sb.append("\n⛽ تاریخچه سوخت‌گیری‌های اخیر (${logs.size} نوبت):\n")
            logs.take(5).forEachIndexed { idx, l ->
                sb.append("${idx + 1}. [${dateFormat.format(Date(l.dateMillis))}] %.1f لیتر (%,d تومان)\n".format(l.liters, l.totalCost))
                sb.append("   - کارکرد: %,d km | جایگاه: ${l.stationName.ifBlank { "نامشخص" }}\n".format(l.odometer))
            }
        }

        sb.append("\n═══════════════════════════\n")
        sb.append("📲 صادر شده از اپلیکیشن مدیریت سوخت و استعلام خودرو")
        return sb.toString()
    }

    // =========================================================================
    // 3. EXCEL / CSV EXPORT
    // =========================================================================
    fun exportToExcelCsv(
        context: Context,
        vehicles: List<VehicleEntity>,
        logs: List<FuelLogEntity>
    ): File? {
        return try {
            val reportsDir = File(context.getExternalFilesDir(null), "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()
            val file = File(reportsDir, "Fuel_Logs_${System.currentTimeMillis()}.csv")

            val vehicleMap = vehicles.associateBy { it.id }
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

            FileOutputStream(file).use { fos ->
                // Write UTF-8 BOM so Excel opens Persian text without encoding issues
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                val header = "ردیف,نام خودرو,پلاک,تاریخ,کیلومتر کارکرد,حجم سوخت (لیتر),قیمت هر لیتر (تومان),مبلغ پرداختی (تومان),نام جایگاه,باک کامل,توضیحات\n"
                fos.write(header.toByteArray(Charsets.UTF_8))

                logs.forEachIndexed { index, log ->
                    val v = vehicleMap[log.vehicleId]
                    val line = listOf(
                        "${index + 1}",
                        v?.title ?: "نامشخص",
                        "\"${v?.formattedPlate ?: ""}\"",
                        dateFormat.format(Date(log.dateMillis)),
                        "${log.odometer}",
                        "${log.liters}",
                        "${log.pricePerLiter}",
                        "${log.totalCost}",
                        "\"${log.stationName}\"",
                        if (log.isFullTank) "کامل" else "خیر",
                        "\"${log.notes.replace("\"", "\"\"")}\""
                    ).joinToString(",") + "\n"
                    fos.write(line.toByteArray(Charsets.UTF_8))
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    // =========================================================================
    // 4. SHARING UTILITIES
    // =========================================================================
    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "فایل خروجی و گزارش رسمی از برنامه مدیریت سوخت و استعلام خودرو")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "خطا در اشتراک‌گذاری: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareText(context: Context, text: String, title: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "خطا در اشتراک‌گذاری متن: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, text: String, toastMessage: String = "متن گزارش در کلیپ‌بورد کپی شد") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Car Report", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    }
}
