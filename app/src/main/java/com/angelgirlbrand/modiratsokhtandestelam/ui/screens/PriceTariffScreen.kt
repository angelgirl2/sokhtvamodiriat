package com.angelgirlbrand.modiratsokhtandestelam.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.angelgirlbrand.modiratsokhtandestelam.FuelApplication
import com.angelgirlbrand.modiratsokhtandestelam.security.PriceCategory
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.DynamicServiceTariffDataTable
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.BaleServiceViewModel
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.TariffViewModel
import com.angelgirlbrand.modiratsokhtandestelam.ui.viewmodel.TariffViewModelFactory
import com.angelgirlbrand.modiratsokhtandestelam.util.AppToast

@Composable
fun PriceTariffScreen(
    baleViewModel: BaleServiceViewModel? = null,
    tariffViewModel: TariffViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = TariffViewModelFactory(
            FuelApplication.instance.repository,
            FuelApplication.instance.securityManager
        )
    ),
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val isWideScreen = screenWidth > 600.dp
        val uiState by tariffViewModel.uiState.collectAsState()

        LaunchedEffect(uiState.syncStatusMessage) {
            uiState.syncStatusMessage?.let { msg ->
                AppToast.show(msg)
                tariffViewModel.clearStatusMessage()
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isWideScreen) 32.dp else 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        // --- 1. Header Banner ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tariff_header_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.2.dp, Color(0xFF10B981).copy(alpha = 0.45f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF064E3B),
                                    Color(0xFF0F172A),
                                    Color(0xFF1E293B)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF10B981).copy(alpha = 0.25f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = Color(0xFF34D399),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "هزینه استعلام‌ها و خرید بیمه‌نامه",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "بروزرسانی خودکار و درجا نرخ‌ها و توضیحات از ربات بله",
                                        fontSize = 11.sp,
                                        color = Color(0xFF6EE7B7)
                                    )
                                }
                            }

                            // Refresh Button: Equal size (44.dp), ONLY ICON!
                            FilledIconButton(
                                onClick = {
                                    tariffViewModel.refreshFromExternalSource()
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("tariff_refresh_button"),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color(0xFF059669),
                                    contentColor = Color.White
                                ),
                                enabled = !uiState.isSyncing
                            ) {
                                if (uiState.isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = "بروزرسانی",
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        // Live status indicator bar
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.8f),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Text(
                                        text = "اتصال برخط و همگام با ربات بله فعال است",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFA7F3D0)
                                    )
                                }

                                Text(
                                    text = "${uiState.items.size} عنوان خدمت",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2. Search & Category Filters ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { tariffViewModel.setSearchQuery(it) },
                    placeholder = { Text("جستجوی نام استعلام، بیمه یا توضیحات...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { tariffViewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "پاک کردن", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tariff_search_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val categories = listOf(PriceCategory.ALL, PriceCategory.INQUIRY, PriceCategory.INSURANCE)
                    items(categories) { category ->
                        val isSelected = uiState.selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { tariffViewModel.setCategory(category) },
                            label = {
                                Text(
                                    category.title,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF059669),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // --- 3. Unified Single-Form Tariff List ---
        item {
            DynamicServiceTariffDataTable(
                items = uiState.items,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
}
