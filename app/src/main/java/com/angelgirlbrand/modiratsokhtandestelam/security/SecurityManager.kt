package com.angelgirlbrand.modiratsokhtandestelam.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

enum class AppThemeColor(val title: String, val primaryHex: Long) {
    SKY_BLUE("آبی آسمانی", 0xFF0284C7),
    RED("قرمز", 0xFFDC2626),
    PURPLE("بنفش", 0xFF7C3AED),
    EMERALD("سبز زمردی", 0xFF059669)
}

enum class DarkModePref(val title: String) {
    SYSTEM("خودکار (سیستم)"),
    LIGHT("روشن (لایت)"),
    DARK("تاریک (دارک)")
}

class SecurityManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("fuel_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_ENABLED = "pin_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_FIRST_RUN_TUTORIAL = "tutorial_seen"
        private const val KEY_RAILWAY_URL = "railway_url"
        private const val KEY_THEME_COLOR = "selected_theme_color"
        private const val KEY_DARK_MODE_PREF = "dark_mode_pref"
        private const val SALT = "AngelGirlBrandFuelSecuritySalt#2026"
        const val FIXED_BALE_BOT_TOKEN = "1882791239:LbdEo9wCRmyaYo0_mQCSR_XtCUc0RDobd6g"
        const val FIXED_BALE_ADMIN_CHAT_ID = "116268751"
    }

    var isAppLocked: Boolean = isPinEnabled()

    fun getSelectedThemeColor(): AppThemeColor {
        val name = prefs.getString(KEY_THEME_COLOR, AppThemeColor.SKY_BLUE.name)
        return try {
            AppThemeColor.valueOf(name ?: AppThemeColor.SKY_BLUE.name)
        } catch (_: Exception) {
            AppThemeColor.SKY_BLUE
        }
    }

    fun setSelectedThemeColor(theme: AppThemeColor) {
        prefs.edit().putString(KEY_THEME_COLOR, theme.name).apply()
    }

    fun getDarkModePref(): DarkModePref {
        val name = prefs.getString(KEY_DARK_MODE_PREF, DarkModePref.SYSTEM.name)
        return try {
            DarkModePref.valueOf(name ?: DarkModePref.SYSTEM.name)
        } catch (_: Exception) {
            DarkModePref.SYSTEM
        }
    }

    fun setDarkModePref(pref: DarkModePref) {
        prefs.edit().putString(KEY_DARK_MODE_PREF, pref.name).apply()
    }

    fun isPinEnabled(): Boolean = prefs.getBoolean(KEY_PIN_ENABLED, false)

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isTutorialSeen(): Boolean = prefs.getBoolean(KEY_FIRST_RUN_TUTORIAL, false)

    fun setTutorialSeen(seen: Boolean) {
        prefs.edit().putBoolean(KEY_FIRST_RUN_TUTORIAL, seen).apply()
    }

    fun isOnboardingSeen(): Boolean = prefs.getBoolean("app_onboarding_completed", false)

    fun setOnboardingSeen(seen: Boolean) {
        prefs.edit().putBoolean("app_onboarding_completed", seen).apply()
    }

    fun isSectionGuideSeen(sectionKey: String): Boolean = prefs.getBoolean("guide_seen_$sectionKey", false)

    fun setSectionGuideSeen(sectionKey: String, seen: Boolean) {
        prefs.edit().putBoolean("guide_seen_$sectionKey", seen).apply()
    }

    fun resetAllGuidesAndOnboarding() {
        val editor = prefs.edit()
        prefs.all.keys.filter { it.startsWith("guide_seen_") || it == "app_onboarding_completed" }.forEach {
            editor.remove(it)
        }
        editor.apply()
    }

    fun getBaleBotToken(): String = FIXED_BALE_BOT_TOKEN

    fun getBaleChatId(): String = FIXED_BALE_ADMIN_CHAT_ID

    fun getRailwayUrl(): String =
        prefs.getString(KEY_RAILWAY_URL, "https://sokhtvamodiriat-production.up.railway.app").orEmpty()

    fun setRailwayUrl(url: String) {
        prefs.edit().putString(KEY_RAILWAY_URL, url).apply()
    }

    fun setPin(pin: String) {
        val hash = hashPin(pin)
        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putBoolean(KEY_PIN_ENABLED, true)
            .apply()
        isAppLocked = false
    }

    fun removePin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_PIN_ENABLED, false)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
        isAppLocked = false
    }

    fun verifyPin(inputPin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return true
        val inputHash = hashPin(inputPin)
        val matches = storedHash == inputHash
        if (matches) {
            isAppLocked = false
        }
        return matches
    }

    fun unlockViaBiometric(): Boolean {
        if (isBiometricEnabled()) {
            isAppLocked = false
            return true
        }
        return false
    }

    private fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest((pin + SALT).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // AES-256 GCM encryption for sensitive user files & database exports
    fun encryptText(plainText: String, secretKeyPass: String = "FuelSecurePass2026"): String {
        return try {
            val keyBytes = MessageDigest.getInstance("SHA-256").digest(secretKeyPass.toByteArray())
            val secretKey = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = iv + encrypted
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            plainText
        }
    }

    fun decryptText(cipherText: String, secretKeyPass: String = "FuelSecurePass2026"): String {
        return try {
            val keyBytes = MessageDigest.getInstance("SHA-256").digest(secretKeyPass.toByteArray())
            val secretKey = SecretKeySpec(keyBytes, "AES")
            val combined = Base64.decode(cipherText, Base64.NO_WRAP)
            val iv = combined.copyOfRange(0, 12)
            val encrypted = combined.copyOfRange(12, combined.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val decrypted = cipher.doFinal(encrypted)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            cipherText
        }
    }
}
