package com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.FuelLogEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.ServiceHistoryEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.ServiceReminderEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.repository.AppRepository
import com.angelgirlbrand.modiratsokhtandestelam.export.ReportExporter
import com.angelgirlbrand.modiratsokhtandestelam.notification.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FuelViewModel(
    private val repository: AppRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    val vehicles: StateFlow<List<VehicleEntity>> = repository.getAllVehicles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedVehicleId = MutableStateFlow<Long?>(null)
    val selectedVehicleId: StateFlow<Long?> = _selectedVehicleId.asStateFlow()

    val fuelLogs: StateFlow<List<FuelLogEntity>> = repository.getAllFuelLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ServiceReminderEntity>> = repository.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val serviceHistory: StateFlow<List<ServiceHistoryEntity>> = repository.getAllServiceHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCost: StateFlow<Long> = repository.getTotalFuelCost()
        .map { it ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalLiters: StateFlow<Double> = repository.getTotalFuelLiters()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun selectVehicle(vehicleId: Long?) {
        _selectedVehicleId.value = vehicleId
    }

    fun addVehicle(
        title: String,
        plateFirst2: String,
        plateLetter: String,
        plateLast3: String,
        plateCityCode: String,
        fuelType: String,
        tankCapacity: Double,
        odometer: Int
    ) {
        viewModelScope.launch {
            val vehicle = VehicleEntity(
                title = title.trim(),
                plateFirst2 = plateFirst2.trim(),
                plateLetter = plateLetter.trim(),
                plateLast3 = plateLast3.trim(),
                plateCityCode = plateCityCode.trim(),
                fuelType = fuelType,
                tankCapacity = tankCapacity,
                currentOdometer = odometer
            )
            val id = repository.insertVehicle(vehicle)
            if (_selectedVehicleId.value == null) {
                _selectedVehicleId.value = id
            }
        }
    }

    fun updateVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch {
            repository.updateVehicle(vehicle)
        }
    }

    fun updateVehicleInsurance(
        vehicle: VehicleEntity,
        expiryMillis: Long,
        company: String,
        type: String
    ) {
        viewModelScope.launch {
            repository.updateVehicle(
                vehicle.copy(
                    insuranceExpiryMillis = expiryMillis,
                    insuranceCompany = company.trim(),
                    insuranceType = type.trim()
                )
            )
        }
    }

    fun updateVehicleInspection(
        vehicle: VehicleEntity,
        expiryMillis: Long,
        centerName: String,
        notifyDaysBefore: Int
    ) {
        viewModelScope.launch {
            val updated = vehicle.copy(
                inspectionExpiryMillis = expiryMillis,
                inspectionCenter = centerName.trim(),
                inspectionNotifyDaysBefore = notifyDaysBefore
            )
            repository.updateVehicle(updated)

            // Trigger notification alert if expiring soon or scheduled
            val daysRemaining = ((expiryMillis - System.currentTimeMillis()) / (24L * 3600 * 1000)).toInt()
            val message = if (daysRemaining < 0) {
                "معاینه فنی خودروی ${vehicle.title} (${vehicle.formattedPlate}) منقضی شده است! نسبت به تمدید آن اقدام فرمایید."
            } else if (daysRemaining <= notifyDaysBefore) {
                "هشدار: تنها $daysRemaining روز تا تاریخ انقضای معاینه فنی خودروی ${vehicle.title} باقی مانده است."
            } else {
                "یادآور تاریخ انقضای معاینه فنی خودروی ${vehicle.title} برای موعد مقرر با موفقیت تنظیم شد ($notifyDaysBefore روز قبل هشدار داده می‌شود)."
            }

            notificationHelper.showServiceAlert(
                title = "معاینه فنی خودرو ${vehicle.title}",
                message = message
            )
        }
    }

    fun deleteVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicle)
            if (_selectedVehicleId.value == vehicle.id) {
                _selectedVehicleId.value = null
            }
        }
    }

    fun addFuelLog(
        vehicleId: Long,
        odometer: Int,
        liters: Double,
        pricePerLiter: Long,
        stationName: String,
        isFullTank: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val totalCost = (liters * pricePerLiter).toLong()
            val log = FuelLogEntity(
                vehicleId = vehicleId,
                dateMillis = System.currentTimeMillis(),
                odometer = odometer,
                liters = liters,
                pricePerLiter = pricePerLiter,
                totalCost = totalCost,
                stationName = stationName.trim(),
                isFullTank = isFullTank,
                notes = notes.trim()
            )
            repository.insertFuelLog(log)
        }
    }

    fun deleteFuelLog(log: FuelLogEntity) {
        viewModelScope.launch {
            repository.deleteFuelLog(log)
        }
    }

    fun addServiceReminder(
        vehicleId: Long,
        serviceType: String,
        targetDateMillis: Long,
        targetOdometer: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val reminder = ServiceReminderEntity(
                vehicleId = vehicleId,
                serviceType = serviceType,
                targetDateMillis = targetDateMillis,
                targetOdometer = targetOdometer,
                notes = notes,
                isCompleted = false
            )
            repository.insertReminder(reminder)

            // Trigger notification
            notificationHelper.showServiceAlert(
                title = serviceType,
                message = "یادآور سرویس $serviceType برای کیلومتر $targetOdometer با موفقیت در سامانه فعال شد."
            )
        }
    }

    fun toggleReminderCompleted(reminder: ServiceReminderEntity) {
        viewModelScope.launch {
            repository.updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
        }
    }

    fun deleteReminder(reminder: ServiceReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    // --- Service History Management ---
    fun addServiceHistory(
        vehicleId: Long,
        serviceType: String,
        itemsChanged: String,
        odometer: Int,
        nextDueOdometer: Int,
        dateMillis: Long,
        cost: Long,
        mechanicOrShop: String,
        notes: String
    ) {
        viewModelScope.launch {
            val record = ServiceHistoryEntity(
                vehicleId = vehicleId,
                serviceType = serviceType.trim(),
                itemsChanged = itemsChanged.trim(),
                odometer = odometer,
                nextDueOdometer = nextDueOdometer,
                dateMillis = dateMillis,
                cost = cost,
                mechanicOrShop = mechanicOrShop.trim(),
                notes = notes.trim()
            )
            repository.insertServiceHistory(record)

            // Update vehicle current odometer if this service odometer is higher
            val vehicle = vehicles.value.firstOrNull { it.id == vehicleId }
            if (vehicle != null && odometer > vehicle.currentOdometer) {
                repository.updateVehicle(vehicle.copy(currentOdometer = odometer))
            }
        }
    }

    fun updateServiceHistory(history: ServiceHistoryEntity) {
        viewModelScope.launch {
            repository.updateServiceHistory(history)
        }
    }

    fun deleteServiceHistory(history: ServiceHistoryEntity) {
        viewModelScope.launch {
            repository.deleteServiceHistory(history)
        }
    }

    fun exportPdf(context: Context) {
        val file = ReportExporter.exportComprehensivePdf(
            context = context,
            vehicles = vehicles.value,
            logs = fuelLogs.value,
            serviceHistory = serviceHistory.value,
            reminders = reminders.value
        )
        if (file != null) {
            ReportExporter.shareFile(
                context,
                file,
                "application/pdf",
                "اشتراک‌گذاری گزارش جامع PDF خودرو و سوخت"
            )
        } else {
            android.widget.Toast.makeText(context, "خطا در ایجاد فایل PDF گزارش", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun exportExcel(context: Context) {
        val file = ReportExporter.exportToExcelCsv(context, vehicles.value, fuelLogs.value)
        if (file != null) {
            ReportExporter.shareFile(
                context,
                file,
                "text/csv",
                "اشتراک‌گذاری فایل اکسل مصرف سوخت"
            )
        } else {
            android.widget.Toast.makeText(context, "خطا در ایجاد فایل اکسل", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun shareReportText(context: Context) {
        val selectedId = _selectedVehicleId.value
        val activeVehicle = vehicles.value.firstOrNull { it.id == selectedId } ?: vehicles.value.firstOrNull()
        val vehicleLogs = if (activeVehicle != null) fuelLogs.value.filter { it.vehicleId == activeVehicle.id } else fuelLogs.value
        val vehicleHistory = if (activeVehicle != null) serviceHistory.value.filter { it.vehicleId == activeVehicle.id } else serviceHistory.value
        val vehicleReminders = if (activeVehicle != null) reminders.value.filter { it.vehicleId == activeVehicle.id } else reminders.value

        val textReport = ReportExporter.generateFullDossierTextReport(
            vehicle = activeVehicle,
            logs = vehicleLogs,
            serviceHistory = vehicleHistory,
            reminders = vehicleReminders
        )

        ReportExporter.shareText(
            context = context,
            text = textReport,
            title = "گزارش متنی مصرف سوخت و سرویس‌های خودرو"
        )
    }

    fun copyReportText(context: Context) {
        val selectedId = _selectedVehicleId.value
        val activeVehicle = vehicles.value.firstOrNull { it.id == selectedId } ?: vehicles.value.firstOrNull()
        val vehicleLogs = if (activeVehicle != null) fuelLogs.value.filter { it.vehicleId == activeVehicle.id } else fuelLogs.value
        val vehicleHistory = if (activeVehicle != null) serviceHistory.value.filter { it.vehicleId == activeVehicle.id } else serviceHistory.value
        val vehicleReminders = if (activeVehicle != null) reminders.value.filter { it.vehicleId == activeVehicle.id } else reminders.value

        val textReport = ReportExporter.generateFullDossierTextReport(
            vehicle = activeVehicle,
            logs = vehicleLogs,
            serviceHistory = vehicleHistory,
            reminders = vehicleReminders
        )

        ReportExporter.copyToClipboard(
            context = context,
            text = textReport,
            toastMessage = "گزارش متنی کامل خودرو در کلیپ‌بورد کپی شد"
        )
    }
}

class FuelViewModelFactory(
    private val repository: AppRepository,
    private val notificationHelper: NotificationHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FuelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FuelViewModel(repository, notificationHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
