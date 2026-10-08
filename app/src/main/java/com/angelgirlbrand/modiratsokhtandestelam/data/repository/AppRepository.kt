package com.angelgirlbrand.modiratsokhtandestelam.data.repository

import com.angelgirlbrand.modiratsokhtandestelam.data.local.AppDatabase
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.*
import com.angelgirlbrand.modiratsokhtandestelam.data.remote.bale.BaleBotService
import com.angelgirlbrand.modiratsokhtandestelam.data.remote.railway.RailwaySyncService
import com.angelgirlbrand.modiratsokhtandestelam.data.remote.traffic.TrafficFineApiResponse
import com.angelgirlbrand.modiratsokhtandestelam.data.remote.traffic.TrafficFineApiService
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class AppRepository(
    private val db: AppDatabase,
    private val baleBotService: BaleBotService = BaleBotService(),
    private val railwaySyncService: RailwaySyncService = RailwaySyncService(),
    private val trafficFineApiService: TrafficFineApiService = TrafficFineApiService()
) {
    // --- Successful Bale request history ---
    fun getSuccessfulBaleRequestHistory(): Flow<List<BaleRequestHistoryEntity>> =
        db.baleRequestHistoryDao().getAllSuccessfulRequests()

    suspend fun deleteBaleRequestHistory(record: BaleRequestHistoryEntity) =
        db.baleRequestHistoryDao().delete(record)

    suspend fun clearBaleRequestHistory() = db.baleRequestHistoryDao().clearAll()

    private suspend fun recordSuccessfulBaleRequest(
        requestKey: String,
        requestType: String,
        title: String,
        summary: String,
        baleMessageId: String,
        chatId: String
    ) {
        db.baleRequestHistoryDao().insertSuccessfulRequest(
            BaleRequestHistoryEntity(
                requestKey = requestKey,
                requestType = requestType,
                title = title,
                summary = summary,
                baleMessageId = baleMessageId,
                chatId = chatId
            )
        )
    }

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
        val initialEntity = ServiceRequestEntity(
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
            baleMessageId = "",
            submissionDateMillis = System.currentTimeMillis(),
            updatedDateMillis = System.currentTimeMillis()
        )
        val id = db.serviceRequestDao().insertRequest(initialEntity)

        // Construct detailed notification text with exact bot commands
        val botMessageText = buildString {
            append("📋 *درخواست جدید خدمات و استعلام خودرو*\n\n")
            append("🆔 *کد پیگیری درخواست:* #REQ_").append(id).append("\n")
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
            append("👤 *اطلاعات متقاضی:*\n")
            append("  • نام: ").append(fullName).append("\n")
            append("  • کد ملی: ").append(nationalCode).append("\n")
            append("  • تماس: ").append(phoneNumber).append("\n\n")
            append("🚗 *مشخصات وسیله نقلیه:*\n")
            append("  • پلاک: ").append(vehiclePlate).append("\n")
            if (vinCode.isNotBlank()) append("  • VIN: ").append(vinCode).append("\n")
            if (barcode.isNotBlank()) append("  • بارکد کارت: ").append(barcode).append("\n")
            if (engineNumber.isNotBlank()) append("  • شماره موتور: ").append(engineNumber).append("\n")
            if (chassisNumber.isNotBlank()) append("  • شماره شاسی: ").append(chassisNumber).append("\n\n")
            if (postalCode.isNotBlank() || address.isNotBlank()) {
                append("📮 *سکونت:* ").append(address).append(" (کد پستی: $postalCode)\n\n")
            }
            if (details.isNotBlank()) {
                append("📝 *توضیحات متقاضی:* ").append(details).append("\n\n")
            }
            append("⏳ وضعیت فعلی: 🟡 #در_صف_بررسی\n")
            append("───────────────────\n")
            append("⚙️ *دستورات سریع مدیریت (روی دستور کلیک یا ارسال فرمایید):*\n")
            append("🟢 تایید درخواست:\n/ok_$id  یا  /ok $id\n\n")
            append("🔴 رد درخواست:\n/reject_$id  یا  /reject $id\n\n")
            append("🟡 در انتظار / بررسی مجدد:\n/wait_$id  یا  /wait $id\n\n")
            append("🗑 حذف از برنامه:\n/delete_$id  یا  /delete $id\n\n")
            append("💡 (همچنین می‌توانید به این پیام با کلمات /ok یا /reject یا /wait یا /delete یا تایید/رد/حذف پاسخ (Reply) دهید.)")
        }

        // Persist the request only after Bale has actually accepted the message.
        val baleResult = baleBotService.sendMessage(botToken, chatId, botMessageText)
        val sendResponse = baleResult.getOrElse { error ->
            db.serviceRequestDao().deleteRequest(initialEntity.copy(id = id))
            throw IllegalStateException("ارسال درخواست به بله ناموفق بود: ${error.message}", error)
        }
        if (!sendResponse.isSuccess) {
            db.serviceRequestDao().deleteRequest(initialEntity.copy(id = id))
            throw IllegalStateException("ارسال درخواست به بله ناموفق بود.")
        }

        val messageId = sendResponse.messageId
        val finalEntity = initialEntity.copy(id = id, baleMessageId = messageId)
        db.serviceRequestDao().updateRequest(finalEntity)
        recordSuccessfulBaleRequest(
            requestKey = "SERVICE:$id:$messageId",
            requestType = requestType,
            title = title,
            summary = "درخواست خدمات با موفقیت به ربات بله ارسال شد.",
            baleMessageId = messageId,
            chatId = chatId
        )
        return finalEntity
    }

    /**
     * Poll updates from Bale Bot, execute status commands (/ok, /reject, /wait, /delete),
     * update local database, and send confirmation back to Bale Bot.
     */
    suspend fun processBaleBotUpdates(botToken: String, currentOffset: Long): Pair<Long, List<String>> {
        val result = baleBotService.getUpdates(botToken, if (currentOffset > 0) currentOffset else null)
        val updates = result.getOrNull().orEmpty()
        if (updates.isEmpty()) {
            return Pair(currentOffset, emptyList())
        }

        var maxUpdateId = currentOffset
        val appliedMessages = mutableListOf<String>()

        for (update in updates) {
            if (update.updateId >= maxUpdateId) {
                maxUpdateId = update.updateId + 1
            }

            val text = update.text.trim()
            if (text.isBlank()) continue

            // 1. Check if it's a price set or tariff list command
            val normalizedText = normalizeDigits(text)
            val lower = normalizedText.lowercase()

            // Admin requests full prices list: /prices, /tariffs, /تعرفه, /لیست_قیمت, /دستمزد
            if (lower == "/prices" || lower == "prices" || lower == "/تعرفه" || lower == "تعرفه" ||
                lower == "/لیست_قیمت" || lower == "لیست قیمت" || lower == "/دستمزد" || lower == "دستمزد") {
                val pm = com.angelgirlbrand.modiratsokhtandestelam.security.PriceManager(com.angelgirlbrand.modiratsokhtandestelam.FuelApplication.instance)
                val summary = pm.buildBaleTariffSummary()
                baleBotService.sendMessage(botToken, update.chatId.ifEmpty { BaleBotService.ADMIN_CHAT_ID }, summary)
                appliedMessages.add("📋 فهرست کامل تعرفه‌ها و دستمزدها به ربات بله ارسال گردید.")
                continue
            }

            // Admin updates a price: /setprice [key] [any text or number...] or /قیمت [key] [هر متنی...]
            if (lower.startsWith("/setprice") || lower.startsWith("setprice") || lower.startsWith("/price") || lower.startsWith("/قیمت") || lower.startsWith("قیمت")) {
                val cleanText = lower.replace("/setprice", "").replace("setprice", "").replace("/price", "").replace("/قیمت", "").replace("قیمت", "").replace("_", " ").trim()
                val parts = cleanText.split(Regex("\\s+"))
                if (parts.size >= 2) {
                    val keyInput = parts[0]
                    // The rest of the message can be ANYTHING (e.g. "توافقی", "تماس بگیرید", "رایگان", or a number like "150000")
                    val rawValue = cleanText.substringAfter(keyInput).trim()
                    if (rawValue.isNotBlank()) {
                        val pm = com.angelgirlbrand.modiratsokhtandestelam.security.PriceManager(com.angelgirlbrand.modiratsokhtandestelam.FuelApplication.instance)
                        val service = com.angelgirlbrand.modiratsokhtandestelam.security.PriceManager.SERVICES.firstOrNull { 
                            it.key.equals(keyInput, ignoreCase = true) || it.key.contains(keyInput, ignoreCase = true) || keyInput.contains(it.key, ignoreCase = true)
                        } ?: com.angelgirlbrand.modiratsokhtandestelam.security.PriceManager.SERVICES.firstOrNull {
                            it.persianName.contains(keyInput) || keyInput.contains(it.persianName)
                        }

                        val targetKey = service?.key ?: keyInput
                        val serviceName = service?.persianName ?: targetKey

                        // If user entered only digits, format as tomans, otherwise use the exact entered text (توافقی, etc.)
                        val digitsOnly = rawValue.replace(",", "").replace("،", "").trim()
                        val numericVal = digitsOnly.toLongOrNull()
                        val finalValue = if (numericVal != null && rawValue.none { it.isLetter() }) {
                            "${String.format("%,d", numericVal)} تومان"
                        } else {
                            rawValue
                        }

                        pm.setPriceText(targetKey, finalValue)

                        val replyText = "💰 *تغییر تعرفه با موفقیت انجام شد*\n\n" +
                                "🔹 *خدمت / دستمزد:* $serviceName\n" +
                                "🏷 *مقدار / تعرفه جدید:* $finalValue\n" +
                                "⚡ *وضعیت:* بروزرسانی آنی و زنده در اپلیکیشن جایگزین گردید.\n\n" +
                                "📱 سامانه هوشمند مدیریت خدمات خودرو"
                        baleBotService.sendMessage(botToken, update.chatId.ifEmpty { BaleBotService.ADMIN_CHAT_ID }, replyText)
                        appliedMessages.add("💰 تعرفه «$serviceName» به «$finalValue» تغییر یافت.")
                        continue
                    }
                }
            }

            // Admin updates service description: /setdesc [key] [description text] or /توضیحات [key] [متن]
            if (lower.startsWith("/setdesc") || lower.startsWith("setdesc") || lower.startsWith("/desc") || lower.startsWith("/توضیحات") || lower.startsWith("توضیحات")) {
                val cleanText = text.replace("/setdesc", "").replace("setdesc", "").replace("/desc", "").replace("/توضیحات", "").replace("توضیحات", "").trim()
                val parts = cleanText.split(Regex("\\s+"))
                if (parts.size >= 2) {
                    val keyInput = parts[0]
                    val rawDesc = cleanText.substringAfter(keyInput).trim()
                    if (rawDesc.isNotBlank()) {
                        val pm = com.angelgirlbrand.modiratsokhtandestelam.security.PriceManager(com.angelgirlbrand.modiratsokhtandestelam.FuelApplication.instance)
                        val service = com.angelgirlbrand.modiratsokhtandestelam.security.PriceManager.SERVICES.firstOrNull { 
                            it.key.equals(keyInput, ignoreCase = true) || it.key.contains(keyInput, ignoreCase = true) || keyInput.contains(it.key, ignoreCase = true)
                        } ?: com.angelgirlbrand.modiratsokhtandestelam.security.PriceManager.SERVICES.firstOrNull {
                            it.persianName.contains(keyInput) || keyInput.contains(it.persianName)
                        }

                        val targetKey = service?.key ?: keyInput
                        val serviceName = service?.persianName ?: targetKey

                        pm.setDescription(targetKey, rawDesc)

                        val replyText = "📝 *تغییر توضیحات با موفقیت انجام شد*\n\n" +
                                "🔹 *خدمت:* $serviceName\n" +
                                "📄 *توضیحات جدید:* $rawDesc\n" +
                                "⚡ *وضعیت:* توضیحات به صورت آنی و خودکار در برنامه جایگزین شد.\n\n" +
                                "📱 سامانه هوشمند استعلام و بیمه خودرو"
                        baleBotService.sendMessage(botToken, update.chatId.ifEmpty { BaleBotService.ADMIN_CHAT_ID }, replyText)
                        appliedMessages.add("📝 توضیحات «$serviceName» بروزرسانی و جایگزین گردید.")
                        continue
                    }
                }
            }

            val actionInfo = parseBotCommand(text, update.replyToText) ?: continue

            // Find target request
            val targetRequest: ServiceRequestEntity? = if (actionInfo.targetRequestId != null) {
                db.serviceRequestDao().getRequestById(actionInfo.targetRequestId)
            } else {
                val all = db.serviceRequestDao().getAllRequestsList()
                all.firstOrNull { it.status == ServiceRequestEntity.STATUS_PENDING } ?: all.firstOrNull()
            }

            if (targetRequest != null) {
                if (actionInfo.actionType == BotActionType.DELETE) {
                    db.serviceRequestDao().deleteRequest(targetRequest)
                    val summaryText = "🗑 درخواست #REQ_${targetRequest.id} (${targetRequest.requestType}) از برنامه حذف گردید."
                    appliedMessages.add(summaryText)

                    val confirmReply = buildString {
                        append("🗑 *درخواست با موفقیت از اپلیکیشن حذف شد*\n\n")
                        append("🆔 *شناسه درخواست:* #REQ_${targetRequest.id}\n")
                        append("🔹 *نوع خدمت:* ${targetRequest.requestType}\n")
                        append("👤 *متقاضی:* ${targetRequest.fullName}\n")
                        append("🏷 *وضعیت:* حذف کامل از برنامه و بانک اطلاعاتی\n\n")
                        append("📱 سامانه هوشمند مدیریت خدمات خودرو")
                    }
                    baleBotService.sendMessage(botToken, update.chatId.ifEmpty { BaleBotService.ADMIN_CHAT_ID }, confirmReply)
                } else {
                    val newStatus = when (actionInfo.actionType) {
                        BotActionType.APPROVE -> ServiceRequestEntity.STATUS_APPROVED
                        BotActionType.REJECT -> ServiceRequestEntity.STATUS_REJECTED
                        BotActionType.PENDING -> ServiceRequestEntity.STATUS_PENDING
                        BotActionType.DELETE -> ServiceRequestEntity.STATUS_PENDING
                    }

                    val statusTitlePersian = when (actionInfo.actionType) {
                        BotActionType.APPROVE -> "تایید شده (صادر گردید)"
                        BotActionType.REJECT -> "رد شده"
                        BotActionType.PENDING -> "در انتظار بررسی"
                        BotActionType.DELETE -> "حذف شده"
                    }

                    val statusEmoji = when (actionInfo.actionType) {
                        BotActionType.APPROVE -> "✅"
                        BotActionType.REJECT -> "❌"
                        BotActionType.PENDING -> "⏳"
                        BotActionType.DELETE -> "🗑"
                    }

                    db.serviceRequestDao().updateRequest(
                        targetRequest.copy(
                            status = newStatus,
                            updatedDateMillis = System.currentTimeMillis()
                        )
                    )

                    val summaryText = "$statusEmoji وضعیت درخواست #REQ_${targetRequest.id} (${targetRequest.requestType}) به «$statusTitlePersian» تغییر یافت."
                    appliedMessages.add(summaryText)

                    // Send reply back to Bale Bot confirming execution
                    val confirmReply = buildString {
                        append("$statusEmoji *دستور با موفقیت در اپلیکیشن اعمال شد*\n\n")
                        append("🆔 *شناسه درخواست:* #REQ_${targetRequest.id}\n")
                        append("🔹 *نوع خدمت:* ${targetRequest.requestType}\n")
                        append("👤 *متقاضی:* ${targetRequest.fullName}\n")
                        append("🚗 *پلاک:* ${targetRequest.vehiclePlate}\n")
                        append("🏷 *وضعیت جدید در برنامه:* $statusTitlePersian\n\n")
                        append("📱 سامانه هوشمند مدیریت خدمات خودرو")
                    }
                    baleBotService.sendMessage(botToken, update.chatId.ifEmpty { BaleBotService.ADMIN_CHAT_ID }, confirmReply)
                }
            }
        }

        return Pair(maxUpdateId, appliedMessages)
    }

    private fun parseBotCommand(text: String, replyToText: String?): BotCommandInfo? {
        val lower = text.lowercase().trim()

        val actionType = when {
            lower.startsWith("/delete") || lower.startsWith("/del") || lower.startsWith("/remove") ||
            lower == "delete" || lower.startsWith("delete ") || lower == "del" || lower.startsWith("del ") ||
            lower.startsWith("حذف") || lower.startsWith("/حذف") || lower.contains("پاک کردن") -> BotActionType.DELETE

            lower.startsWith("/ok") || lower.startsWith("/approve") || lower.startsWith("/accept") ||
            lower == "ok" || lower.startsWith("ok ") || lower.startsWith("تایید") || lower.startsWith("/تایید") ||
            lower.contains("موافقت") || lower == "yes" -> BotActionType.APPROVE

            lower.startsWith("/reject") || lower.startsWith("/deny") || lower.startsWith("/cancel") ||
            lower == "reject" || lower.startsWith("reject ") || lower.startsWith("رد") || lower.startsWith("/رد") ||
            lower.contains("مخالفت") || lower.contains("عدم تایید") || lower == "no" -> BotActionType.REJECT

            lower.startsWith("/wait") || lower.startsWith("/pending") || lower.startsWith("/review") ||
            lower == "wait" || lower.startsWith("wait ") || lower.startsWith("بررسی") || lower.startsWith("/بررسی") ||
            lower.startsWith("در انتظار") || lower.startsWith("/wait") -> BotActionType.PENDING

            else -> null
        } ?: return null

        // Extract ID from command text first: e.g. /ok_12, /ok 12, /reject-5, /delete_3, حذف 7
        val idInTextRegex = Regex("""(?:ok|reject|wait|approve|pending|delete|del|remove|تایید|رد|بررسی|حذف|req)[_ \t#-]*(\d+)""", RegexOption.IGNORE_CASE)
        val standaloneNumberRegex = Regex("""\b(\d+)\b""")

        var extractedId: Long? = idInTextRegex.find(lower)?.groupValues?.get(1)?.toLongOrNull()
        if (extractedId == null) {
            extractedId = standaloneNumberRegex.find(lower)?.value?.toLongOrNull()
        }

        // If not in text, look in replyToText (e.g. #REQ_12 or کد پیگیری درخواست: #REQ_12)
        if (extractedId == null && replyToText != null) {
            val replyIdRegex = Regex("""(?:REQ[_\s#-]*|شناسه درخواست:\s*#?REQ_?|کد پیگیری درخواست:\s*#?REQ_?)(\d+)""", RegexOption.IGNORE_CASE)
            extractedId = replyIdRegex.find(replyToText)?.groupValues?.get(1)?.toLongOrNull()
        }

        return BotCommandInfo(actionType, extractedId)
    }

    private data class BotCommandInfo(
        val actionType: BotActionType,
        val targetRequestId: Long?
    )

    private enum class BotActionType {
        APPROVE, REJECT, PENDING, DELETE
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
        val effectiveVin = vinCode.ifBlank { barcodeOrVin }
        val effectiveBarcode = barcode.ifBlank { barcodeOrVin }

        val msg = buildString {
            append("📋 درخواست جدید استعلام خودرو\n\n")
            append("🔎 نوع استعلام: ").append(inquiryType).append("\n")
            append("🚗 عنوان: ").append(title).append("\n")
            if (plateNumber.isNotBlank()) append("🔢 پلاک: ").append(plateNumber).append("\n")
            if (effectiveVin.isNotBlank()) append("🆔 VIN: ").append(effectiveVin).append("\n")
            if (effectiveBarcode.isNotBlank()) append("📄 بارکد: ").append(effectiveBarcode).append("\n")
            if (engineNumber.isNotBlank()) append("⚙️ شماره موتور: ").append(engineNumber).append("\n")
            if (chassisNumber.isNotBlank()) append("🧾 شماره شاسی: ").append(chassisNumber).append("\n")
            if (fullName.isNotBlank()) append("👤 نام: ").append(fullName).append("\n")
            if (nationalId.isNotBlank()) append("🪪 کد ملی: ").append(nationalId).append("\n")
            if (phoneNumber.isNotBlank()) append("📱 تماس: ").append(phoneNumber).append("\n")
            if (postalCode.isNotBlank()) append("📮 کد پستی: ").append(postalCode).append("\n")
            if (address.isNotBlank()) append("🏠 آدرس: ").append(address).append("\n")
            if (amount > 0L) append("💰 مبلغ برآوردی ثبت‌شده: ").append("%,d".format(amount)).append(" تومان\n")
            append("\n✅ این پیام فقط پس از پذیرش موفق توسط ربات بله در سابقه برنامه ثبت می‌شود.")
        }

        val sendResponse = baleBotService.sendMessage(botToken, chatId, msg).getOrElse { error ->
            throw IllegalStateException("ارسال استعلام به بله ناموفق بود: ${error.message}", error)
        }
        if (!sendResponse.isSuccess) {
            throw IllegalStateException("ارسال استعلام به بله ناموفق بود.")
        }

        val now = System.currentTimeMillis()
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
            status = "ارسال موفق به ربات بله",
            transactionRef = sendResponse.messageId,
            dateMillis = now
        )
        val id = db.inquiryDao().insertInquiry(record)
        recordSuccessfulBaleRequest(
            requestKey = "INQUIRY:$id:${sendResponse.messageId}",
            requestType = inquiryType,
            title = title,
            summary = if (plateNumber.isNotBlank()) "پلاک: $plateNumber" else "درخواست بدون پلاک",
            baleMessageId = sendResponse.messageId,
            chatId = chatId
        )
        return record.copy(id = id)
    }

    suspend fun updateInquiry(inquiry: InquiryRecordEntity) = db.inquiryDao().updateInquiry(inquiry)

    suspend fun deleteInquiry(inquiry: InquiryRecordEntity) = db.inquiryDao().deleteInquiry(inquiry)

    // --- Real Online Plate-Based Traffic Fine Inquiry ---
    suspend fun queryTrafficFinesByPlate(
        isMotorcycle: Boolean,
        vehicleTitle: String,
        plateF2: String,
        plateLetter: String,
        plateL3: String,
        plateCity: String,
        motoTop3: String,
        motoBottom5: String,
        plateFormatted: String,
        botToken: String,
        chatId: String
    ): TrafficFineApiResponse {
        val response = trafficFineApiService.queryTrafficFinesByPlate(
            isMotorcycle = isMotorcycle,
            vehicleTitle = vehicleTitle,
            plateF2 = plateF2,
            plateLetter = plateLetter,
            plateL3 = plateL3,
            plateCity = plateCity,
            motoTop3 = motoTop3,
            motoBottom5 = motoBottom5,
            plateFormatted = plateFormatted,
            botToken = botToken,
            chatId = chatId
        )

        // Save real inquiry result in Room Database
        if (response is TrafficFineApiResponse.Success) {
            val res = response.result
            val record = InquiryRecordEntity(
                inquiryType = if (isMotorcycle) "استعلام خلافی موتورسیکلت (مدیریت)" else "استعلام خلافی خودرو (مدیریت)",
                title = vehicleTitle,
                plateNumber = plateFormatted,
                barcodeOrVin = "",
                amount = 0L,
                workflowMethod = "BALE_BOT",
                status = "ارسال شده به کارشناس و مدیر (در انتظار پاسخ)",
                transactionRef = res.baleMessageId.ifBlank { "BALE-PENDING" },
                dateMillis = System.currentTimeMillis()
            )
            val id = db.inquiryDao().insertInquiry(record)
            recordSuccessfulBaleRequest(
                requestKey = "TRAFFIC:$id:${record.transactionRef}",
                requestType = record.inquiryType,
                title = record.title,
                summary = "پلاک: ${record.plateNumber}",
                baleMessageId = record.transactionRef,
                chatId = chatId
            )
            return TrafficFineApiResponse.Success(res.copy(inquiryRecordId = id))
        }

        return response
    }

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
            append("💳 *شماره کارت واریز:* ۶۲۱۹-۸۶۱۹-۲۰۶۹-۶۲۰۹ (angelgirlbrand)\n")
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

    private fun normalizeDigits(input: String): String {
        val persian = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val arabic = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        var out = input
        for (i in 0..9) {
            out = out.replace(persian[i], ('0' + i)).replace(arabic[i], ('0' + i))
        }
        return out
    }
}
