package com.angelgirlbrand.modiratsokhtandestelam

import android.app.Application
import com.angelgirlbrand.modiratsokhtandestelam.data.local.AppDatabase
import com.angelgirlbrand.modiratsokhtandestelam.data.remote.bale.BaleBotService
import com.angelgirlbrand.modiratsokhtandestelam.data.remote.railway.RailwaySyncService
import com.angelgirlbrand.modiratsokhtandestelam.data.repository.AppRepository
import com.angelgirlbrand.modiratsokhtandestelam.notification.NotificationHelper
import com.angelgirlbrand.modiratsokhtandestelam.security.SecurityManager

class FuelApplication : Application() {
    companion object {
        lateinit var instance: FuelApplication
            private set
    }

    lateinit var database: AppDatabase
        private set
    lateinit var repository: AppRepository
        private set
    lateinit var securityManager: SecurityManager
        private set
    lateinit var notificationHelper: NotificationHelper
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        checkVersionUpdateAndClearOldCache()
        database = AppDatabase.getDatabase(this)
        val baleService = BaleBotService()
        val railwayService = RailwaySyncService()
        repository = AppRepository(database, baleService, railwayService)
        securityManager = SecurityManager(this)
        notificationHelper = NotificationHelper(this)
    }

    /**
     * Checks if the app was updated from an older version.
     * When an update is detected, clears the previous version's cache directory
     * and refreshes the cache state.
     */
    private fun checkVersionUpdateAndClearOldCache() {
        try {
            val prefs = getSharedPreferences("app_version_management", MODE_PRIVATE)
            val lastVersionCode = prefs.getLong("last_installed_version_code", -1L)
            val lastVersionName = prefs.getString("last_installed_version_name", null)

            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }

            val currentVersionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            val currentVersionName = packageInfo.versionName ?: "1.0"

            if (lastVersionCode != -1L && (lastVersionCode < currentVersionCode || lastVersionName != currentVersionName)) {
                // Application update detected! Clear cache of previous version
                android.util.Log.i("FuelApplication", "Update detected: from $lastVersionName ($lastVersionCode) to $currentVersionName ($currentVersionCode). Clearing old cache.")
                clearAppCache()
            }

            // Save new version
            prefs.edit()
                .putLong("last_installed_version_code", currentVersionCode)
                .putString("last_installed_version_name", currentVersionName)
                .apply()
        } catch (e: Exception) {
            android.util.Log.e("FuelApplication", "Error checking app version update", e)
        }
    }

    fun clearAppCache() {
        try {
            cacheDir?.deleteRecursively()
            codeCacheDir?.deleteRecursively()
            externalCacheDir?.deleteRecursively()
            android.util.Log.i("FuelApplication", "Cache successfully deleted.")
        } catch (e: Exception) {
            android.util.Log.e("FuelApplication", "Error deleting cache", e)
        }
    }
}
