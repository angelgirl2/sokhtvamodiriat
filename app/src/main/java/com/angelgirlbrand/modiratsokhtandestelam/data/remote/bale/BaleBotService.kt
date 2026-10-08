package com.angelgirlbrand.modiratsokhtandestelam.data.remote.bale

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class BaleUpdate(
    val updateId: Long,
    val messageId: Long,
    val chatId: String,
    val text: String,
    val replyToText: String? = null,
    val replyToMessageId: Long? = null,
    val senderName: String = "",
    val date: Long = 0L
)

class BaleBotService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        const val BOT_TOKEN = "1882791239:LbdEo9wCRmyaYo0_mQCSR_XtCUc0RDobd6g"
        const val ADMIN_CHAT_ID = "116268751"
        const val BALE_API_BASE_URL = "https://tapi.bale.ai/bot"

        private const val DUPLICATE_WINDOW_MS = 12_000L
        private val recentSuccessfulMessages = ConcurrentHashMap<String, Long>()
    }

    suspend fun sendMessage(
        botToken: String,
        chatId: String,
        text: String
    ): Result<BaleSendResponse> = withContext(Dispatchers.IO) {
        val fingerprint = "$chatId\u0000$text"
        val now = System.currentTimeMillis()
        val previousSuccessAt = recentSuccessfulMessages[fingerprint]
        if (previousSuccessAt != null && now - previousSuccessAt < DUPLICATE_WINDOW_MS) {
            return@withContext Result.failure(
                IllegalStateException("این درخواست همین چند لحظه قبل با موفقیت به بله ارسال شده است.")
            )
        }

        try {
            val url = "$BALE_API_BASE_URL$botToken/sendMessage"
            val jsonObject = JSONObject().apply {
                put("chat_id", chatId)
                put("text", text)
            }

            val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()
            Log.d("BaleBotService", "Response code: ${response.code}, body: $responseBody")

            if (response.isSuccessful && responseBody.isNotBlank()) {
                val json = JSONObject(responseBody)
                val ok = json.optBoolean("ok", false)
                val resultObj = json.optJSONObject("result")
                val messageId = resultObj?.optLong("message_id") ?: 0L
                if (ok && messageId > 0L) {
                    recentSuccessfulMessages[fingerprint] = now
                    recentSuccessfulMessages.entries.removeIf { now - it.value > DUPLICATE_WINDOW_MS * 4 }
                    return@withContext Result.success(
                        BaleSendResponse(isSuccess = true, messageId = messageId.toString(), rawResponse = responseBody)
                    )
                }
                return@withContext Result.failure(
                    IllegalStateException("بله درخواست را نپذیرفت. پاسخ سرور معتبر نبود.")
                )
            }

            Result.failure(
                IllegalStateException("ارسال به بله انجام نشد (HTTP ${response.code}).")
            )
        } catch (e: Exception) {
            Log.e("BaleBotService", "Error sending request", e)
            Result.failure(e)
        }
    }

    suspend fun sendPhoto(
        botToken: String,
        chatId: String,
        caption: String,
        photoBytes: ByteArray?,
        fileName: String = "receipt.jpg"
    ): Result<BaleSendResponse> = withContext(Dispatchers.IO) {
        try {
            if (photoBytes != null && photoBytes.isNotEmpty()) {
                val url = "$BALE_API_BASE_URL$botToken/sendPhoto"
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("chat_id", chatId)
                    .addFormDataPart("caption", caption)
                    .addFormDataPart(
                        "photo",
                        fileName,
                        photoBytes.toRequestBody("image/jpeg".toMediaType())
                    )
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string().orEmpty()
                Log.d("BaleBotService", "sendPhoto response: ${response.code}, body: $responseBody")

                if (response.isSuccessful && responseBody.isNotBlank()) {
                    val json = JSONObject(responseBody)
                    val ok = json.optBoolean("ok", false)
                    val resultObj = json.optJSONObject("result")
                    val messageId = resultObj?.optLong("message_id") ?: 0L
                    if (ok && messageId > 0L) {
                        return@withContext Result.success(
                            BaleSendResponse(isSuccess = true, messageId = messageId.toString(), rawResponse = responseBody)
                        )
                    }
                    return@withContext Result.failure(IllegalStateException("بله عکس را نپذیرفت."))
                }
            }
            return@withContext Result.failure(IllegalArgumentException("فایلی برای ارسال وجود ندارد."))
        } catch (e: Exception) {
            Log.e("BaleBotService", "Error sending photo", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Poll updates from Bale Bot API to capture commands like /ok, /reject, /wait
     */
    suspend fun getUpdates(
        botToken: String,
        offset: Long? = null,
        limit: Int = 30
    ): Result<List<BaleUpdate>> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = StringBuilder("$BALE_API_BASE_URL$botToken/getUpdates?")
            if (offset != null && offset > 0) {
                urlBuilder.append("offset=$offset&")
            }
            urlBuilder.append("limit=$limit")

            val request = Request.Builder()
                .url(urlBuilder.toString())
                .get()
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()
            Log.d("BaleBotService", "getUpdates response code: ${response.code}")

            if (response.isSuccessful && responseBody.isNotBlank()) {
                val json = JSONObject(responseBody)
                if (json.optBoolean("ok", false)) {
                    val resultsArray = json.optJSONArray("result")
                    val updatesList = mutableListOf<BaleUpdate>()
                    if (resultsArray != null) {
                        for (i in 0 until resultsArray.length()) {
                            val item = resultsArray.optJSONObject(i) ?: continue
                            val updateId = item.optLong("update_id", 0L)
                            val messageObj = item.optJSONObject("message")
                                ?: item.optJSONObject("callback_query")?.optJSONObject("message")
                                ?: item.optJSONObject("channel_post")
                                ?: item.optJSONObject("edited_message")

                            if (messageObj != null) {
                                val msgId = messageObj.optLong("message_id", 0L)
                                val chatObj = messageObj.optJSONObject("chat")
                                val chatId = chatObj?.optString("id")
                                    ?: messageObj.optJSONObject("from")?.optString("id").orEmpty()
                                val text = messageObj.optString("text").ifEmpty {
                                    item.optJSONObject("callback_query")?.optString("data").orEmpty()
                                }
                                val replyObj = messageObj.optJSONObject("reply_to_message")
                                val replyText = replyObj?.optString("text")
                                val replyMsgId = replyObj?.optLong("message_id")
                                val fromObj = messageObj.optJSONObject("from")
                                val senderName = fromObj?.optString("first_name").orEmpty()
                                val date = messageObj.optLong("date", 0L)

                                updatesList.add(
                                    BaleUpdate(
                                        updateId = updateId,
                                        messageId = msgId,
                                        chatId = chatId,
                                        text = text,
                                        replyToText = replyText,
                                        replyToMessageId = replyMsgId,
                                        senderName = senderName,
                                        date = date
                                    )
                                )
                            }
                        }
                    }
                    return@withContext Result.success(updatesList)
                }
            }
            Result.success(emptyList())
        } catch (e: Exception) {
            Log.e("BaleBotService", "Error getting updates: ${e.message}")
            Result.failure(e)
        }
    }
}

data class BaleSendResponse(
    val isSuccess: Boolean,
    val messageId: String,
    val rawResponse: String
)

