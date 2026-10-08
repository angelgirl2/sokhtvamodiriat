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
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val inquiries: StateFlow<List<InquiryRecordEntity>> = repository.getAllInquiries()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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
                        val (_, appliedList) = repository.processBaleBotUpdates(botToken)
                        if (appliedList.isNotEmpty()) {
                            val emojiRegex = Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF\\u2600-\\u26FF\\u2700-\\u27BF\\uFE00-\\uFE0F\\p{So}]")
                            val cleanList = appliedList.map { it.replace(emojiRegex, "").trim() }
                            _lastCommandNotification.value = cleanList.joinToString("\n")
                            _submissionMessage.value = cleanList.firstOrNull()
                            val notifHelper = com.angelgirlbrand.modiratsokhtandestelam.notification.NotificationHelper(com.angelgirlbrand.modiratsokhtandestelam.FuelApplication.instance)
                            notifHelper.showServiceAlert("بروزرسانی وضعیت از سوی مدیر", cleanList.first())
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
                val (_, appliedList) = repository.processBaleBotUpdates(botToken)
                if (appliedList.isNotEmpty()) {
                    val emojiRegex = Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF\\u2600-\\u26FF\\u2700-\\u27BF\\uFE00-\\uFE0F\\p{So}]")
                    val cleanList = appliedList.map { it.replace(emojiRegex, "").trim() }
                    _lastCommandNotification.value = cleanList.joinToString("\n")
                    _submissionMessage.value = "آخرین وضعیت درخواست‌های شما بروزرسانی گردید:\n" + cleanList.joinToString("\n")
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
        android.util.Log.d("BaleServiceViewModel", "Updating request $requestId to status: $newStatus")
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
        android.util.Log.d("BaleServiceViewModel", "Approving inquiry $inquiryId")
        viewModelScope.launch {
            val inq = inquiries.value.firstOrNull { it.id == inquiryId }
            if (inq != null) {
                val ref = "ADM-" + (10000000..99999999).random()
                repository.updateInquiry(
                    inq.copy(
                        status = InquiryRecordEntity.STATUS_APPROVED,
                        transactionRef = ref,
                        updatedDateMillis = System.currentTimeMillis()
                    )
                )
                _submissionMessage.value = "استعلام #${inquiryId} توسط مدیر تایید گردید و وضعیت آن بلافاصله سبز شد."
            }
        }
    }

    // Submit Payment Slip to Manager Card & Bale Bot
    fun submitPaymentSlip(
        inquiry: InquiryRecordEntity,
        receiptRef: String,
        payerName: String,
        bankName: String,
        senderCard: String,
        amountPaid: Long,
        imageUri: String
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val botToken = securityManager.getBaleBotToken()
                val chatId = securityManager.getBaleChatId()

                val updatedStatus = "در انتظار تایید فیش پرداختی توسط مدیر (رسید: $receiptRef)"
                val updatedInquiry = inquiry.copy(
                    amount = amountPaid,
                    status = updatedStatus,
                    transactionRef = receiptRef
                )
                repository.updateInquiry(updatedInquiry)

                val detailsText = buildString {
                    append("💳 شماره کارت مبدأ: ").append(senderCard.ifBlank { "ثبت نشده" }).append("\n")
                    append("💰 مبلغ واریزی: ").append(String.format("%,d", amountPaid)).append(" تومان\n")
                    append("🧾 شماره پیگیری فیش: ").append(receiptRef).append("\n")
                    append("🏦 بانک مبدأ: ").append(bankName.ifBlank { "نامشخص" }).append("\n")
                    if (imageUri.isNotBlank()) {
                        append("🖼 تصویر فیش پیوست شده: ").append(imageUri)
                    }
                }

                // Submit payment slip details to manager via Bale Bot
                repository.submitServiceRequest(
                    requestType = "فیش پرداختی به کارت مدیر",
                    title = inquiry.inquiryType,
                    fullName = if (payerName.isNotBlank()) payerName else "کاربر گرامی",
                    nationalCode = inquiry.nationalId,
                    phoneNumber = inquiry.phoneNumber,
                    vehiclePlate = inquiry.plateNumber,
                    vinCode = bankName,
                    details = detailsText,
                    botToken = botToken,
                    chatId = chatId
                )

                // Trigger Push Notification
                val notifHelper = com.angelgirlbrand.modiratsokhtandestelam.notification.NotificationHelper(com.angelgirlbrand.modiratsokhtandestelam.FuelApplication.instance)
                notifHelper.showServiceAlert("ارسال فیش پرداخت", "فیش پرداختی با شماره پیگیری $receiptRef برای مدیر ارسال شد.")

                _submissionMessage.value = "فیش پرداختی با شماره پیگیری $receiptRef با موفقیت به مدیر ارسال شد و در انتظار تایید قرار گرفت."
            } catch (e: Exception) {
                _submissionMessage.value = "خطا در ارسال فیش پرداختی. لطفاً اتصال اینترنت را بررسی نمایید."
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun deleteInquiry(inquiry: InquiryRecordEntity) {
        viewModelScope.launch {
            repository.deleteInquiry(inquiry)
        }
    }

    fun resendInquiryToBot(inquiry: InquiryRecordEntity) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val botToken = securityManager.getBaleBotToken()
                val chatId = securityManager.getBaleChatId()
                
                // Re-submit using the same data but this will trigger a new message
                repository.submitInquiry(
                    inquiryType = inquiry.inquiryType,
                    title = inquiry.title,
                    plateNumber = inquiry.plateNumber,
                    barcodeOrVin = inquiry.barcodeOrVin,
                    nationalId = inquiry.nationalId,
                    amount = inquiry.amount,
                    workflowMethod = inquiry.workflowMethod,
                    fullName = inquiry.fullName,
                    phoneNumber = inquiry.phoneNumber,
                    vinCode = inquiry.vinCode,
                    barcode = inquiry.barcode,
                    engineNumber = inquiry.engineNumber,
                    chassisNumber = inquiry.chassisNumber,
                    postalCode = inquiry.postalCode,
                    address = inquiry.address,
                    botToken = botToken,
                    chatId = chatId
                )
                _submissionMessage.value = "درخواست مجدداً به ربات بله ارسال گردید."
            } catch (e: Exception) {
                _submissionMessage.value = "خطا در ارسال مجدد. لطفاً اتصال اینترنت را بررسی نمایید."
            } finally {
                _isSubmitting.value = false
            }
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
