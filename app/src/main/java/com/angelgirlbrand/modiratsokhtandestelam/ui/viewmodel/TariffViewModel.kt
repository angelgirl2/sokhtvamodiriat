package com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.angelgirlbrand.modiratsokhtandestelam.FuelApplication
import com.angelgirlbrand.modiratsokhtandestelam.data.repository.AppRepository
import com.angelgirlbrand.modiratsokhtandestelam.security.PriceCategory
import com.angelgirlbrand.modiratsokhtandestelam.security.PriceManager
import com.angelgirlbrand.modiratsokhtandestelam.security.SecurityManager
import com.angelgirlbrand.modiratsokhtandestelam.security.TariffServiceData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TariffTableItem(
    val key: String,
    val name: String,
    val category: PriceCategory,
    val priceText: String,
    val description: String,
    val lastUpdatedTime: Long = System.currentTimeMillis(),
    val isRecentlyUpdated: Boolean = false
)

enum class TariffSortOption(val title: String) {
    DEFAULT("پیش‌فرض"),
    NAME("نام خدمت"),
    CATEGORY("دسته‌بندی")
}

data class TariffUiState(
    val items: List<TariffTableItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: PriceCategory = PriceCategory.ALL,
    val sortOption: TariffSortOption = TariffSortOption.DEFAULT,
    val isSyncing: Boolean = false,
    val syncStatusMessage: String? = null,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)

class TariffViewModel(
    private val repository: AppRepository,
    private val securityManager: SecurityManager
) : ViewModel() {

    private val priceManager = PriceManager(FuelApplication.instance)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(PriceCategory.ALL)
    val selectedCategory: StateFlow<PriceCategory> = _selectedCategory.asStateFlow()

    private val _sortOption = MutableStateFlow(TariffSortOption.DEFAULT)
    val sortOption: StateFlow<TariffSortOption> = _sortOption.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncStatusMessage = MutableStateFlow<String?>(null)
    val syncStatusMessage: StateFlow<String?> = _syncStatusMessage.asStateFlow()

    private val _recentlyUpdatedKeys = MutableStateFlow<Set<String>>(emptySet())

    private var currentBotOffset: Long = 0L

    // Combines real-time tariff data from PriceManager with search & category filters
    val uiState: StateFlow<TariffUiState> = combine(
        PriceManager.tariffDataState,
        _searchQuery,
        _selectedCategory,
        _sortOption,
        _isSyncing,
        _syncStatusMessage,
        _recentlyUpdatedKeys
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val tariffDataMap = args[0] as Map<String, TariffServiceData>
        val query = args[1] as String
        val category = args[2] as PriceCategory
        val sort = args[3] as TariffSortOption
        val syncing = args[4] as Boolean
        val message = args[5] as String?
        val recentlyUpdated = args[6] as Set<String>

        val allItems = PriceManager.SERVICES.map { service ->
            val liveData = tariffDataMap[service.key]
            val priceVal = liveData?.priceText ?: service.defaultPriceText
            val descVal = liveData?.description ?: service.defaultDescription
            TariffTableItem(
                key = service.key,
                name = service.persianName,
                category = service.category,
                priceText = priceVal,
                description = descVal,
                isRecentlyUpdated = recentlyUpdated.contains(service.key)
            )
        }

        // Filter by category and search
        val filtered = allItems.filter { item ->
            val matchesCategory = (category == PriceCategory.ALL || item.category == category)
            val matchesQuery = query.isBlank() ||
                    item.name.contains(query.trim(), ignoreCase = true) ||
                    item.description.contains(query.trim(), ignoreCase = true) ||
                    item.priceText.contains(query.trim(), ignoreCase = true)
            matchesCategory && matchesQuery
        }

        val sorted = when (sort) {
            TariffSortOption.NAME -> filtered.sortedBy { it.name }
            TariffSortOption.CATEGORY -> filtered.sortedBy { it.category.title }
            TariffSortOption.DEFAULT -> filtered
        }

        TariffUiState(
            items = sorted,
            searchQuery = query,
            selectedCategory = category,
            sortOption = sort,
            isSyncing = syncing,
            syncStatusMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TariffUiState()
    )

    init {
        priceManager.updateStateFlow()
        startLiveExternalSync()
    }

    /**
     * Polls the external Bale bot in the background to capture price & description updates
     */
    private fun startLiveExternalSync() {
        viewModelScope.launch {
            while (true) {
                try {
                    val botToken = securityManager.getBaleBotToken()
                    if (botToken.isNotBlank()) {
                        val (newOffset, appliedList) = repository.processBaleBotUpdates(botToken, currentBotOffset)
                        currentBotOffset = newOffset
                        if (appliedList.isNotEmpty()) {
                            priceManager.updateStateFlow()
                            _syncStatusMessage.value = appliedList.firstOrNull()
                        }
                    }
                } catch (_: Exception) {
                    // Fail gracefully
                }
                delay(3000)
            }
        }
    }

    /**
     * Ingests and parses raw text from external streams
     */
    fun processExternalStringUpdate(rawInput: String): Boolean {
        if (rawInput.isBlank()) return false
        var updatedAny = false
        val lines = rawInput.lines()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isBlank()) continue

            // 1. Description update: /setdesc or /توضیحات or key.desc=...
            if (trimmed.startsWith("/setdesc") || trimmed.startsWith("/توضیحات") || trimmed.startsWith("/desc") || trimmed.contains(".desc=")) {
                val clean = trimmed.replace("/setdesc", "").replace("/توضیحات", "").replace("/desc", "").trim()
                val key = if (clean.contains(".desc=")) clean.substringBefore(".desc=").trim() else clean.substringBefore(" ").trim()
                val desc = if (clean.contains(".desc=")) clean.substringAfter(".desc=").trim() else clean.substringAfter(" ").trim()
                if (key.isNotBlank() && desc.isNotBlank()) {
                    updateDescription(key, desc)
                    updatedAny = true
                    continue
                }
            }

            // 2. Price update: key=value or /setprice [key] [value]
            if (trimmed.contains("=") && !trimmed.contains(".desc=")) {
                val key = trimmed.substringBefore("=").trim()
                val value = trimmed.substringAfter("=").trim()
                if (key.isNotBlank() && value.isNotBlank()) {
                    updatePriceText(key, value)
                    updatedAny = true
                    continue
                }
            }

            val lower = trimmed.lowercase()
            if (lower.startsWith("/setprice") || lower.startsWith("/قیمت")) {
                val clean = lower.replace("/setprice", "").replace("/قیمت", "").trim()
                val parts = clean.split(Regex("\\s+"))
                if (parts.size >= 2) {
                    val key = parts[0]
                    val value = clean.substringAfter(key).trim()
                    if (value.isNotBlank()) {
                        updatePriceText(key, value)
                        updatedAny = true
                        continue
                    }
                }
            }
        }
        return updatedAny
    }

    /**
     * Directly updates a price/text item and triggers real-time in-place UI animation
     */
    fun updatePriceText(key: String, newText: String) {
        val targetService = PriceManager.SERVICES.firstOrNull {
            it.key.equals(key, ignoreCase = true) || it.persianName.contains(key) || key.contains(it.persianName)
        }
        val targetKey = targetService?.key ?: key
        priceManager.setPriceText(targetKey, newText)

        viewModelScope.launch {
            _recentlyUpdatedKeys.value = _recentlyUpdatedKeys.value + targetKey
            delay(3500)
            _recentlyUpdatedKeys.value = _recentlyUpdatedKeys.value - targetKey
        }
    }

    /**
     * Directly updates a service description and triggers real-time in-place UI animation
     */
    fun updateDescription(key: String, newDescription: String) {
        val targetService = PriceManager.SERVICES.firstOrNull {
            it.key.equals(key, ignoreCase = true) || it.persianName.contains(key) || key.contains(it.persianName)
        }
        val targetKey = targetService?.key ?: key
        priceManager.setDescription(targetKey, newDescription)

        viewModelScope.launch {
            _recentlyUpdatedKeys.value = _recentlyUpdatedKeys.value + targetKey
            delay(3500)
            _recentlyUpdatedKeys.value = _recentlyUpdatedKeys.value - targetKey
        }
    }

    fun refreshFromExternalSource() {
        viewModelScope.launch {
            try {
                _isSyncing.value = true
                val botToken = securityManager.getBaleBotToken()
                val (newOffset, appliedList) = repository.processBaleBotUpdates(botToken, currentBotOffset)
                currentBotOffset = newOffset
                priceManager.updateStateFlow()
                if (appliedList.isNotEmpty()) {
                    _syncStatusMessage.value = "تعرفه‌ها و توضیحات با موفقیت بروزرسانی شدند."
                } else {
                    _syncStatusMessage.value = "اطلاعات با سرور همگام است."
                }
            } catch (e: Exception) {
                _syncStatusMessage.value = "بررسی اتصال انجام شد."
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: PriceCategory) {
        _selectedCategory.value = category
    }

    fun setSortOption(sort: TariffSortOption) {
        _sortOption.value = sort
    }

    fun clearStatusMessage() {
        _syncStatusMessage.value = null
    }
}

class TariffViewModelFactory(
    private val repository: AppRepository,
    private val securityManager: SecurityManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TariffViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TariffViewModel(repository, securityManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
