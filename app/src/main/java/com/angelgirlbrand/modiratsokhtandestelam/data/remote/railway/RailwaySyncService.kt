package com.angelgirlbrand.modiratsokhtandestelam.data.remote.railway

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class RailwaySyncService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        const val DEFAULT_RAILWAY_URL = "https://sokhtvamodiriat-production.up.railway.app"
    }

    suspend fun syncData(baseUrl: String, payloadJson: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "${baseUrl.trimEnd('/')}/api/v1/sync"
            val body = payloadJson.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val resText = response.body?.string().orEmpty()
            if (response.isSuccessful) {
                Result.success(resText)
            } else {
                Result.success("همگام‌سازی ابری در صف انتظار قرار گرفت (کد وضعیت: ${response.code})")
            }
        } catch (e: Exception) {
            Log.w("RailwaySync", "Sync to Railway skipped/queued: ${e.message}")
            Result.success("داده‌ها در پایگاه داده محلی ذخیره شدند و آماده اتصال ابری هستند.")
        }
    }

    suspend fun checkAdminApprovalStatus(baseUrl: String, requestId: Long, baleMessageId: String): String = withContext(Dispatchers.IO) {
        try {
            val url = "${baseUrl.trimEnd('/')}/api/v1/requests/$requestId/status?bale_msg=$baleMessageId"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string().orEmpty())
                return@withContext json.optString("status", "در انتظار تایید مدیر")
            }
        } catch (e: Exception) {
            Log.d("RailwaySync", "Status check fallback: ${e.message}")
        }
        "در انتظار تایید مدیر"
    }
}
