package com.angelgirlbrand.modiratsokhtandestelam.data.remote.traffic

import android.util.Log
import com.angelgirlbrand.modiratsokhtandestelam.data.remote.bale.BaleBotService
import com.angelgirlbrand.modiratsokhtandestelam.ui.screens.FineViolationItem
import com.angelgirlbrand.modiratsokhtandestelam.ui.screens.TrafficFineResult
import com.angelgirlbrand.modiratsokhtandestelam.util.PersianDateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class TrafficFineApiResponse {
    data class Success(val result: TrafficFineResult) : TrafficFineApiResponse()
    data class Error(val message: String) : TrafficFineApiResponse()
}

class TrafficFineApiService(
    private val baleBotService: BaleBotService = BaleBotService()
) {
    companion object {
        private const val TAG = "TrafficFineApiService"
    }

    /**
     * Directly sends the plate-based traffic fine inquiry to the connected Bale Bot.
     */
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
        botToken: String = BaleBotService.BOT_TOKEN,
        chatId: String = BaleBotService.ADMIN_CHAT_ID
    ): TrafficFineApiResponse = withContext(Dispatchers.IO) {
        val nowShamsi = PersianDateHelper.toPersianDateTime(System.currentTimeMillis())
        val trackingRef = "BALE-PLT-" + (10000000..99999999).random()

        try {
            // Compose formatted Bale message
            val messageText = buildString {
                append("🚔 *درخواست جدید استعلام خلافی راهور (پلاک‌پایه)*\n\n")
                append("🚘 *وسیله نقلیه:* ").append(vehicleTitle).append("\n")
                append("🔢 *شماره پلاک:* ").append(plateFormatted).append("\n")
                append("🛵 *نوع وسیله:* ").append(if (isMotorcycle) "موتورسیکلت" else "خودرو سواری").append("\n")
                append("🕒 *تاریخ و زمان استعلام:* ").append(nowShamsi).append("\n")
                append("🆔 *کد رهگیری:* ").append(trackingRef).append("\n\n")
                append("📋 درخواست استعلام خلافی پلاک از طریق اپلیکیشن مستقیماً به کارشناس و مدیر ارسال گردید.")
            }

            Log.d(TAG, "Sending traffic fine inquiry to Bale Bot for plate: $plateFormatted")
            val sendResult = baleBotService.sendMessage(botToken, chatId, messageText)
            val response = sendResult.getOrElse { error ->
                return@withContext TrafficFineApiResponse.Error(
                    "ارسال درخواست به ربات بله انجام نشد: ${error.message ?: "خطای نامشخص"}"
                )
            }
            if (!response.isSuccess) {
                return@withContext TrafficFineApiResponse.Error("ربات بله درخواست را نپذیرفت.")
            }

            val result = TrafficFineResult(
                vehicleTitle = vehicleTitle,
                plateFormatted = plateFormatted,
                isMotorcycle = isMotorcycle,
                totalAmount = 0L,
                violationCount = 0,
                negativePoints = 0,
                judicialStatus = "درخواست واقعاً به بله ارسال شد؛ نتیجه خلافی هنوز دریافت نشده است.",
                inquiryDateTimeShamsi = nowShamsi,
                violations = emptyList(),
                inquiryRecordId = 0L,
                baleMessageId = response.messageId,
                isPaid = false
            )

            TrafficFineApiResponse.Success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending inquiry to Bale Bot: ${e.message}", e)
            TrafficFineApiResponse.Error(
                "خطا در ارسال درخواست به بله: ${e.message ?: "خطای نامشخص"}"
            )
        }
    }
}
