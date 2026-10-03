package com.angelgirlbrand.modiratsokhtandestelam.data.repository

import com.angelgirlbrand.modiratsokhtandestelam.data.local.AppDatabase
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.*
import com.angelgirlbrand.modiratsokhtandestelam.data.remote.bale.BaleBotService
import com.angelgirlbrand.modiratsokhtandestelam.data.remote.railway.RailwaySyncService
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class AppRepository(
    private val db: AppDatabase,
    private val baleBotService: BaleBotService = BaleBotService(),
    private val railwaySyncService: RailwaySyncService = RailwaySyncService()
) {
    // --- Vehicles ---
    fun getAllVehicles(): Flow<List<VehicleEntity>> = db.vehicleDao().getAllVehicles()

    suspend fun getVehicleById(id: Long): VehicleEntity? = db.vehicleDao().getVehicleById(id)

    suspend fun insertVehicle(vehicle: VehicleEntity): Long = db.vehicleDao().insertVehicle(vehicle)

    suspend fun updateVehicle(vehicle: VehicleEntity) = db.vehicleDao().updateVehicle(vehicle)

    suspend fun deleteVehicle(vehicle: VehicleEntity) = db.vehicleDao().deleteVehicle(vehicle)

    suspend fun getVehicleCount(): Int = db.vehicleDao().getVehicleCount()

    // --- Fuel Logs ---
    fun getAllFuelLogs(): Flow<List<FuelLogEntity>> = db.fuelLogDao().getAllLogs()

    fun getFuelLogsForVehicle(vehicleId: Long): Flow<List<FuelLogEntity>> =
        db.fuelLogDao().getLogsForVehicle(vehicleId)

    suspend fun insertFuelLog(log: FuelLogEntity): Long {
        val id = db.fuelLogDao().insertLog(log)
        // Also update current odometer of vehicle if higher
        val vehicle = db.vehicleDao().getVehicleById(log.vehicleId)
        if (vehicle != null && log.odometer > vehicle.currentOdometer) {
            db.vehicleDao().updateVehicle(vehicle.copy(currentOdometer = log.odometer))
        }
        return id
    }

    suspend fun updateFuelLog(log: FuelLogEntity) = db.fuelLogDao().updateLog(log)

    suspend fun deleteFuelLog(log: FuelLogEntity) = db.fuelLogDao().deleteLog(log)

    fun getTotalFuelCost(): Flow<Long?> = db.fuelLogDao().getTotalFuelCost()
    fun getTotalFuelLiters(): Flow<Double?> = db.fuelLogDao().getTotalFuelLiters()

    // --- Service Reminders ---
    fun getAllReminders(): Flow<List<ServiceReminderEntity>> = db.serviceReminderDao().getAllReminders()

    suspend fun insertReminder(reminder: ServiceReminderEntity): Long =
        db.serviceReminderDao().insertReminder(reminder)

    suspend fun updateReminder(reminder: ServiceReminderEntity) =
        db.serviceReminderDao().updateReminder(reminder)

    suspend fun deleteReminder(reminder: ServiceReminderEntity) =
        db.serviceReminderDao().deleteReminder(reminder)

    // --- Service History (Periodic Maintenance Records) ---
    fun getAllServiceHistory(): Flow<List<ServiceHistoryEntity>> = db.serviceHistoryDao().getAllHistory()

    fun getServiceHistoryForVehicle(vehicleId: Long): Flow<List<ServiceHistoryEntity>> =
        db.serviceHistoryDao().getHistoryForVehicle(vehicleId)

    suspend fun insertServiceHistory(history: ServiceHistoryEntity): Long =
        db.serviceHistoryDao().insertHistory(history)

    suspend fun updateServiceHistory(history: ServiceHistoryEntity) =
        db.serviceHistoryDao().updateHistory(history)

    suspend fun deleteServiceHistory(history: ServiceHistoryEntity) =
        db.serviceHistoryDao().deleteHistory(history)

    fun getTotalServiceCostForVehicle(vehicleId: Long): Flow<Long?> =
        db.serviceHistoryDao().getTotalCostForVehicle(vehicleId)

    // --- Service Requests & Bale Bot ---
    fun getAllRequests(): Flow<List<ServiceRequestEntity>> = db.serviceRequestDao().getAllRequests()

    suspend fun submitServiceRequest(
        requestType: String,
        title: String,
        fullName: String,
        nationalCode: String,
        phoneNumber: String,
        vehiclePlate: String,
        vinCode: String = "",
        barcode: String = "",
        engineNumber: String = "",
        chassisNumber: String = "",
        postalCode: String = "",
        address: String = "",
        insuranceCategory: String = "",
        insuranceCompany: String = "",
        durationMonths: Int = 12,
        discountPercent: Int = 0,
        details: String = "",
        botToken: String,
        chatId: String
    ): ServiceRequestEntity {
        // Construct detailed notification text
        val botMessageText = buildString {
            append("📋 *درخواست جدید خدمات و استعلام خودرو*\n\n")
            append("🔹 *نوع خدمت:* ").append(requestType).append("\n")
            append("📌 *عنوان:* ").append(title).append("\n")
            if (insuranceCategory.isNotBlank()) {
                append("🛡 *دسته‌بندی بیمه:* ").append(insuranceCategory).append("\n")
            }
            if (insuranceCompany.isNotBlank()) {
                append("🏢 *شرکت بیمه‌گر:* ").append(insuranceCompany).append("\n")
                append("⏳ *مدت اعتبار:* ").append(durationMonths).append(" ماهه\n")
                append("🏷 *تخفیف عدم خسارت:* ").append(discountPercent).append("٪\n")
            }
            append("👤 *اطلاعات فردی متقاضی:*\n")
            append("  • نام و نام خانوادگی: ").append(fullName).append("\n")
            append("  • کد ملی: ").append(nationalCode).append("\n")
            append("  • شماره تماس: ").append(phoneNumber).append("\n\n")
            append("🚗 *مشخصات کامل خودرو:*\n")
            append("  • پلاک: ").append(vehiclePlate).append("\n")
            if (vinCode.isNotBlank()) append("  • کد شناسایی (VIN): ").append(vinCode).append("\n")
            if (barcode.isNotBlank()) append("  • بارکد کارت خودرو: ").append(barcode).append("\n")
            if (engineNumber.isNotBlank()) append("  • شماره موتور: ").append(engineNumber).append("\n")
            if (chassisNumber.isNotBlank()) append("  • شماره شاسی: ").append(chassisNumber).append("\n\n")
            if (postalCode.isNotBlank() || address.isNotBlank()) {
                append("📮 *اطلاعات پستی و سکونت:*\n")
                if (postalCode.isNotBlank()) append("  • کد پستی: ").append(postalCode).append("\n")
                if (address.isNotBlank()) append("  • آدرس: ").append(address).append("\n\n")
            }
            if (details.isNotBlank()) {
                append("📝 *توضیحات تکمیلی:* ").append(details).append("\n\n")
            }
            append("⏳ وضعیت: #در_انتظار_تایید_مدیر\n")
            append("📱 سامانه هوشمند مدیریت خدمات خودرو")
        }

        // Send to remote backend service
        val baleResult = baleBotService.sendMessage(botToken, chatId, botMessageText)
        val messageId = baleResult.getOrNull()?.messageId ?: ("REQ-" + System.currentTimeMillis().toString().takeLast(6))

        val entity = ServiceRequestEntity(
            requestType = requestType,
            title = title,
            fullName = fullName,
            nationalCode = nationalCode,
            phoneNumber = phoneNumber,
            vehiclePlate = vehiclePlate,
            vinCode = vinCode,
            barcodeNumber = barcode,
            engineNumber = engineNumber,
            chassisNumber = chassisNumber,
            postalCode = postalCode,
            address = address,
            insuranceCategory = insuranceCategory,
            insuranceCompany = insuranceCompany,
            durationMonths = durationMonths,
            discountPercent = discountPercent,
            additionalDetails = details,
            status = ServiceRequestEntity.STATUS_PENDING,
            baleMessageId = messageId,
            submissionDateMillis = System.currentTimeMillis(),
            updatedDateMillis = System.currentTimeMillis()
        )
        val id = db.serviceRequestDao().insertRequest(entity)
        return entity.copy(id = id)
    }

    suspend fun updateRequestStatus(requestId: Long, newStatus: String) {
        val existing = db.serviceRequestDao().getRequestById(requestId) ?: return
        db.serviceRequestDao().updateRequest(
            existing.copy(
                status = newStatus,
                updatedDateMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteRequest(request: ServiceRequestEntity) {
        db.serviceRequestDao().deleteRequest(request)
    }

    // --- Inquiries & Payments ---
    fun getAllInquiries(): Flow<List<InquiryRecordEntity>> = db.inquiryDao().getAllInquiries()

    suspend fun submitInquiry(
        inquiryType: String,
        title: String,
        plateNumber: String,
        barcodeOrVin: String,
        nationalId: String,
        amount: Long,
        workflowMethod: String,
        fullName: String = "",
        phoneNumber: String = "",
        vinCode: String = "",
        barcode: String = "",
        engineNumber: String = "",
        chassisNumber: String = "",
        postalCode: String = "",
        address: String = "",
        botToken: String,
        chatId: String
    ): InquiryRecordEntity {
        val status = if (workflowMethod == "ADMIN_REVIEW" || workflowMethod == "ADMIN_BALE") "در حال بررسی توسط کارشناس" else "در انتظار پرداخت آنلاین"
        var ref = ""

        val effectiveVin = vinCode.ifBlank { barcodeOrVin }
        val effectiveBarcode = barcode.ifBlank { barcodeOrVin }

        if (workflowMethod == "ADMIN_REVIEW" || workflowMethod == "ADMIN_BALE") {
            val msg = buildString {
                append("💳 *استعلام و تسویه عوارض و خلافی خودرو*\n\n")
                append("📑 *نوع استعلام:* ").append(inquiryType).append("\n")
                if (fullName.isNotBlank()) append("👤 *نام مالک:* ").append(fullName).append("\n")
                if (nationalId.isNotBlank()) append("🆔 *کد ملی:* ").append(nationalId).append("\n")
                if (phoneNumber.isNotBlank()) append("📱 *شماره تماس:* ").append(phoneNumber).append("\n\n")
                append("🚗 *مشخصات کامل خودرو:*\n")
                append("  • پلاک: ").append(plateNumber).append("\n")
                if (effectiveVin.isNotBlank()) append("  • کد شناسایی (VIN): ").append(effectiveVin).append("\n")
                if (effectiveBarcode.isNotBlank()) append("  • بارکد کارت خودرو: ").append(effectiveBarcode).append("\n")
                if (engineNumber.isNotBlank()) append("  • شماره موتور: ").append(engineNumber).append("\n")
                if (chassisNumber.isNotBlank()) append("  • شماره شاسی: ").append(chassisNumber).append("\n\n")
                if (postalCode.isNotBlank() || address.isNotBlank()) {
                    append("📮 *مشخصات سکونت:*\n")
                    if (postalCode.isNotBlank()) append("  • کد پستی: ").append(postalCode).append("\n")
                    if (address.isNotBlank()) append("  • آدرس: ").append(address).append("\n\n")
                }
                append("💰 *مبلغ برآوردی:* ").append("%,d".format(amount)).append(" تومان\n")
                append("\nلطفا پس از بررسی و تسویه، وضعیت را تایید بفرمایید.")
            }
            val res = baleBotService.sendMessage(botToken, chatId, msg)
            ref = res.getOrNull()?.messageId ?: ("INQ-" + System.currentTimeMillis().toString().takeLast(6))
        }

        val record = InquiryRecordEntity(
            inquiryType = inquiryType,
            title = title,
            plateNumber = plateNumber,
            barcodeOrVin = barcodeOrVin,
            nationalId = nationalId,
            fullName = fullName,
            phoneNumber = phoneNumber,
            vinCode = effectiveVin,
            barcode = effectiveBarcode,
            engineNumber = engineNumber,
            chassisNumber = chassisNumber,
            postalCode = postalCode,
            address = address,
            amount = amount,
            workflowMethod = workflowMethod,
            status = status,
            transactionRef = ref,
            dateMillis = System.currentTimeMillis()
        )
        val id = db.inquiryDao().insertInquiry(record)
        return record.copy(id = id)
    }

    suspend fun updateInquiry(inquiry: InquiryRecordEntity) = db.inquiryDao().updateInquiry(inquiry)

    suspend fun deleteInquiry(inquiry: InquiryRecordEntity) = db.inquiryDao().deleteInquiry(inquiry)

    // --- Donation & Support to Developer ---
    suspend fun submitDonation(
        amount: Long,
        customAmountText: String,
        payerName: String,
        payerPhone: String,
        note: String,
        photoBytes: ByteArray?,
        botToken: String,
        chatId: String
    ): Result<String> {
        val finalAmountText = if (amount > 0) "${String.format("%,d", amount)} تومان" else customAmountText
        val messageText = buildString {
            append("🎁 *رسید حمایت مالی جدید از برنامه‌نویس*\n\n")
            append("💰 *مبلغ حمایت:* ").append(finalAmountText).append("\n")
            if (payerName.isNotBlank()) append("👤 *نام پرداخت‌کننده:* ").append(payerName).append("\n")
            if (payerPhone.isNotBlank()) append("📞 *شماره تماس:* ").append(payerPhone).append("\n")
            if (note.isNotBlank()) append("📝 *پیام/یادداشت:* ").append(note).append("\n\n")
            append("💳 *شماره کارت واریز:* ۶۲۱۹-۸۶۱۹-۲۰۶۹-۶۲۰۹ (میلاد قنواتی)\n")
            append("📱 *برنامه:* مدیریت سوخت و استعلام خودرو")
        }

        return if (photoBytes != null && photoBytes.isNotEmpty()) {
            val res = baleBotService.sendPhoto(botToken, chatId, messageText, photoBytes, "support_receipt.jpg")
            Result.success(res.getOrNull()?.messageId ?: System.currentTimeMillis().toString())
        } else {
            val res = baleBotService.sendMessage(botToken, chatId, messageText)
            Result.success(res.getOrNull()?.messageId ?: System.currentTimeMillis().toString())
        }
    }

    // --- Cloud Sync ---
    suspend fun syncAllToRailway(railwayUrl: String, vehicles: List<VehicleEntity>, logs: List<FuelLogEntity>): Result<String> {
        val json = JSONObject().apply {
            put("app", "com.angelgirlbrand.sokhtandmodiriat")
            put("timestamp", System.currentTimeMillis())
            val vArray = JSONArray()
            vehicles.forEach { v ->
                vArray.put(JSONObject().apply {
                    put("id", v.id)
                    put("title", v.title)
                    put("plate", v.formattedPlate)
                    put("fuelType", v.fuelType)
                    put("odometer", v.currentOdometer)
                })
            }
            put("vehicles", vArray)
            val lArray = JSONArray()
            logs.forEach { l ->
                lArray.put(JSONObject().apply {
                    put("vehicleId", l.vehicleId)
                    put("liters", l.liters)
                    put("cost", l.totalCost)
                    put("odometer", l.odometer)
                    put("date", l.dateMillis)
                })
            }
            put("fuelLogs", lArray)
        }
        return railwaySyncService.syncData(railwayUrl, json.toString())
    }
}
