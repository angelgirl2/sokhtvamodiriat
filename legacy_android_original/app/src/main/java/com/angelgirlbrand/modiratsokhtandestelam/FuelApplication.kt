package com.angelgirlbrand.modiratsokhtandestelam

import android.app.Application
import com.angelgirlbrand.modiratsokhtandestelam.data.local.AppDatabase
import com.angelgirlbrand.modiratsokhtandestelam.data.remote.bale.BaleBotService
import com.angelgirlbrand.modiratsokhtandestelam.data.remote.railway.RailwaySyncService
import com.angelgirlbrand.modiratsokhtandestelam.data.repository.AppRepository
import com.angelgirlbrand.modiratsokhtandestelam.notification.NotificationHelper
import com.angelgirlbrand.modiratsokhtandestelam.security.SecurityManager

class FuelApplication : Application() {
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
        database = AppDatabase.getDatabase(this)
        val baleService = BaleBotService()
        val railwayService = RailwaySyncService()
        repository = AppRepository(database, baleService, railwayService)
        securityManager = SecurityManager(this)
        notificationHelper = NotificationHelper(this)
    }
}
