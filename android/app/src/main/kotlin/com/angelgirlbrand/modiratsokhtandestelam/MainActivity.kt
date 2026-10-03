package com.angelgirlbrand.modiratsokhtandestelam

import android.content.Context
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    companion object {
        private const val CHANNEL = "sookht_man/legacy_security"
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "readLegacySecurityPrefs" -> {
                    val prefs = getSharedPreferences("fuel_security_prefs", Context.MODE_PRIVATE)
                    result.success(prefs.all.toMap())
                }
                else -> result.notImplemented()
            }
        }
    }
}
