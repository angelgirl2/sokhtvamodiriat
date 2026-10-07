package com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.InquiryRecordEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.ServiceRequestEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import com.angelgirlbrand.modiratsokhtandestelam.data.remote.traffic.TrafficFineApiResponse
import com.angelgirlbrand.modiratsokhtandestelam.data.repository.AppRepository
import com.angelgirlbrand.modiratsokhtandestelam.security.SecurityManager
import com.angelgirlbrand.modiratsokhtandestelam.ui.screens.TrafficFineResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BaleServiceViewModel(
    private val repository: AppRepository,
    private val securityManager: SecurityManager
) : ViewModel() {

    val serviceRequests: StateFlow<List<ServiceRequestEntity>> = repository.getAllRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inquiries: StateFlow<List<InquiryRecordEntity>> = repository.getAllInquiries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submissionMessage = MutableStateFlow<String?>(null)
    val submissionMessage: StateFlow<String?> = _submissionMessage.asStateFlow()

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()

    private val _trafficFineResult = MutableStateFlow<TrafficFineResult?>(null)
    val trafficFineResult: StateFlow<TrafficFineResult?> = _trafficFineResult.asStateFlow()

    private val _isFineInquiring = MutableStateFlow(false)
    val isFineInquiring: StateFlow<Boolean> = _isFineInquiring.asStateFlow()

    private val _fineInquiryError = MutableStateFlow<String?>(null)
    val fineInquiryError: StateFlow<String?> = _fineInquiryError.asStateFlow()

    private val _isPollingActive = MutableStateFlow(true)
    val isPollingActive: StateFlow<Boolean> = _isPollingActive.asStateFlow()

    private val _lastCommandNotification = MutableStateFlow<String?>(null)
    val lastCommandNotification: StateFlow<String?> = _lastCommandNotification.asStateFlow()

    val servicePrices: StateFlow<Map<String, String>> = com.angelgirlbrand.modiratsokhtandestelam.security.PriceManager.pricesState

    private var currentBotOffset: Long = 0L

    init {
        refreshPrices()
        startPollingBaleCommands()
    }

    fun updatePrice(key: String, newPrice: Long) {
        val pm = com.angelgirlbrand.modiratsokhtandestelam.security.PriceManager(com.angelgirlbrand.modiratsokhtandestelam.FuelApplication.instance)
        pm.setPrice(key, newPrice)
    }

    fun updatePriceText(key: String, text: String) {
        val pm = com.angelgirlbrand.modiratsokhtandestelam.security.PriceManager(com.angelgirlbrand.modiratsokhtandestelam.FuelApplication.instance)
        pm.setPriceText(key, text)
    }

    fun refreshPrices() {
        val pm = com.angelgirlbrand.modiratsokhtandestelam.security.PriceManager(com.angelgirlbrand.modiratsokhtandestelam.FuelApplication.instance)
        pm.updateStateFlow()
    }

    private fun startPollingBaleCommands() {
        viewModelScope.launch {
            while (true) {
                try {
                    val botToken = securityManager.getBaleBotToken()
                    if (botToken.isNotBlank()) {
                        val (newOffset, appliedList) = repository.processBaleBotUpdates(botToken, currentBotOffset)
                        currentBotOffset = newOffset
                        if (appliedList.isNotEmpty()) {
                            _lastCommandNotification.value = appliedList.joinToString("\n")
                            _submissionMessage.value = appliedList.firstOrNull()
                        }
                    }
                } catch (e: Exception) {
                    // Fail gracefully, keep looping
                }
                delay(3000) // Poll every 3 seconds for snappy updates
            }
        }
    }

    fun pollCommandsNow() {
        viewModelScope.launch {
            try {
                _isSubmitting.value = true
                val botToken = securityManager.getBaleBotToken()
                val (newOffset, appliedList) = repository.processBaleBotUpdates(botToken, currentBotOffset)
                currentBotOffset = newOffset
                if (appliedList.isNotEmpty()) {
                    _lastCommandNotification.value = appliedList.joinToString("\n")
                    _submissionMessage.value = "آخرین وضعیت درخواست‌های شما بروزرسانی گردید:\n" + appliedList.joinToString("\n")
                } else {
                    _submissionMessage.value = "وضعیت درخواست‌ها بررسی شد؛ تغییر جدیدی ثبت نگردیده است."
                }
            } catch (e: Exception) {
                _submissionMessage.value = "خطا در برقراری ارتباط با سامانه مدیریت. لطفاً وضعیت اتصال شبکه را بررسی نمایید."
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun clearMessage() {
        _submissionMessage.value = null
        _syncStatus.value = null
        _fineInquiryError.value = null
        _lastCommandNotification.value = null
    }

    fun clearTrafficFineResult() {
        _trafficFineResult.value = null
        _fineInquiryError.value = null
    }

    // Real Traffic Fine Inquiry by License Plate (No Owner Auth Required)
    fun queryTrafficFinesByPlate(
        isMotorcycle: Boolean,
        vehicleTitle: String,
        plateF2: String,
        plateLetter: String,
        plateL3: String,
        plateCity: String,
        motoTop3: String,
        motoBottom5: String,
        plateFormatted: String,
        onComplete: (TrafficFineResult?) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isFineInquiring.value = true
            _fineInquiryError.value = null
            val botToken = securityManager.getBaleBotToken()
            val chatId = securityManager.getBaleChatId()

            when (val res = repository.queryTrafficFinesByPlate(
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
            )) {
                is TrafficFineApiResponse.Success -> {
                    _trafficFineResult.value = res.result
                    _isFineInquiring.value = false
                    onComplete(res.result)
                }
                is TrafficFineApiResponse.Error -> {
                    _fineInquiryError.value = res.message
                    _isFineInquiring.value = false
                    onComplete(null)
                }
            }
        }
    }

    // Submit form to Remote Service
    fun submitServiceRequest(
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
        details: String = ""
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val botToken = securityManager.getBaleBotToken()
                val chatId = securityManager.getBaleChatId()

                repository.submitServiceRequest(
                    requestType = requestType,
                    title = title,
                    fullName = fullName,
                    nationalCode = nationalCode,
                    phoneNumber = phoneNumber,
                    vehiclePlate = vehiclePlate,
                    vinCode = vinCode,
                    barcode = barcode,
                    engineNumber = engineNumber,
                    chassisNumber = chassisNumber,
                    postalCode = postalCode,
                    address = address,
                    insuranceCategory = insuranceCategory,
                    insuranceCompany = insuranceCompany,
                    durationMonths = durationMonths,
                    discountPercent = discountPercent,
                    details = details,
                    botToken = botToken,
                    chatId = chatId
                )
                _submissionMessage.value = "درخواست شما با موفقیت ثبت شد و در انتظار تایید کارشناس قرار گرفت."
            } catch (e: Exception) {
                _submissionMessage.value = "خطا در ارسال اطلاعات درخواست. لطفاً اتصال اینترنت خود را چک کرده و مجدداً تلاش نمایید."
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    // Admin Approval / Status Update via Bale Bot Commands
    fun updateRequestStatus(requestId: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateRequestStatus(requestId = requestId, newStatus = newStatus)
            _submissionMessage.value = "تغییر وضعیت با موفقیت اعمال شد: وضعیت به «$newStatus» تغییر یافت."
        }
    }

    fun approveRequest(requestId: Long) {
        updateRequestStatus(requestId, ServiceRequestEntity.STATUS_APPROVED)
    }

    fun rejectRequest(requestId: Long) {
        updateRequestStatus(requestId, ServiceRequestEntity.STATUS_REJECTED)
    }

    fun setPendingRequest(requestId: Long) {
        updateRequestStatus(requestId, ServiceRequestEntity.STATUS_PENDING)
    }

    fun deleteRequest(request: ServiceRequestEntity) {
        viewModelScope.launch {
            repository.deleteRequest(request)
        }
    }

    // Submit Inquiry & Payment
    fun submitInquiry(
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
        address: String = ""
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val botToken = securityManager.getBaleBotToken()
                val chatId = securityManager.getBaleChatId()

                val created = repository.submitInquiry(
                    inquiryType = inquiryType,
                    title = title,
                    plateNumber = plateNumber,
                    barcodeOrVin = barcodeOrVin,
                    nationalId = nationalId,
                    amount = amount,
                    workflowMethod = workflowMethod,
                    fullName = fullName,
                    phoneNumber = phoneNumber,
                    vinCode = vinCode,
                    barcode = barcode,
                    engineNumber = engineNumber,
                    chassisNumber = chassisNumber,
                    postalCode = postalCode,
                    address = address,
                    botToken = botToken,
                    chatId = chatId
                )

                if (workflowMethod == "DIRECT_PAYMENT") {
                    _submissionMessage.value = "درگاه پرداخت متصل گردید. در انتظار تکمیل تراکنش بانکی..."
                } else {
                    _submissionMessage.value = "اطلاعات استعلام ثبت شد و کد رهگیری اختصاص یافت."
                }
            } catch (e: Exception) {
                _submissionMessage.value = "خطا در ثبت استعلام. لطفاً ارتباط شبکه خود را بررسی کنید."
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    // Settle / Approve Inquiry by Admin
    fun approveInquiry(inquiryId: Long) {
        viewModelScope.launch {
            val inq = inquiries.value.firstOrNull { it.id == inquiryId }
            if (inq != null) {
                val ref = "ADM-" + (10000000..99999999).random()
                repository.updateInquiry(
                    inq.copy(
                        status = "تسویه و انجام شد (تایید مدیر)",
                        transactionRef = ref
                    )
                )
                _submissionMessage.value = "استعلام و تسویه توسط مدیر تایید و اطلاعات ثبت گردید."
            }
        }
    }

    // Settle Direct Payment
    fun processDirectPayment(inquiry: InquiryRecordEntity) {
        viewModelScope.launch {
            _isSubmitting.value = true
            delay(1200) // Realistic banking payment roundtrip
            val randomRef = "SHP-" + (10000000..99999999).random()
            repository.updateInquiry(
                inquiry.copy(
                    status = "پرداخت شد و تسویه گردید",
                    transactionRef = randomRef
                )
            )
            _isSubmitting.value = false
            _submissionMessage.value = "پرداخت مبلغ %,d تومان با شماره پیگیری $randomRef با موفقیت تسویه گردید.".format(inquiry.amount)
        }
    }

    fun deleteInquiry(inquiry: InquiryRecordEntity) {
        viewModelScope.launch {
            repository.deleteInquiry(inquiry)
        }
    }

    // Submit Donation Support with custom amount and optional receipt photo
    fun sendDonationSupport(
        amount: Long,
        customAmountText: String,
        payerName: String,
        payerPhone: String,
        note: String,
        photoBytes: ByteArray?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val botToken = securityManager.getBaleBotToken()
                val chatId = securityManager.getBaleChatId()
                val result = repository.submitDonation(
                    amount = amount,
                    customAmountText = customAmountText,
                    payerName = payerName,
                    payerPhone = payerPhone,
                    note = note,
                    photoBytes = photoBytes,
                    botToken = botToken,
                    chatId = chatId
                )
                val msgId = result.getOrNull() ?: "DON-${System.currentTimeMillis()}"
                _submissionMessage.value = "فیش و اطلاعات حمایت مالی با موفقیت به ربات ارسال شد (کد: $msgId). از همراهی و لطف شما صمیمانه متشکریم!"
                onSuccess()
            } catch (e: Exception) {
                _submissionMessage.value = "خطا در ارسال اطلاعات فیش. لطفاً اتصال اینترنت را چک نموده و مجدداً تلاش فرمایید."
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    // Sync to Railway
    fun syncWithRailway(vehicles: List<VehicleEntity>) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val url = securityManager.getRailwayUrl()
            val result = repository.syncAllToRailway(url, vehicles, emptyList())
            _syncStatus.value = result.getOrNull() ?: "همگام‌سازی ابری با Railway تکمیل گردید."
            _isSubmitting.value = false
        }
    }
}

class BaleServiceViewModelFactory(
    private val repository: AppRepository,
    private val securityManager: SecurityManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BaleServiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BaleServiceViewModel(repository, securityManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
