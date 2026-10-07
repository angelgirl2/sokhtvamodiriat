package com.angelgirlbrand.modiratsokhtandestelam.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import com.angelgirlbrand.modiratsokhtandestelam.R
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity

@Composable
fun SmartFuelCard(
    vehicle: VehicleEntity?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("fuel_card_prefs", Context.MODE_PRIVATE) }

    var hasFuelCard by remember {
        mutableStateOf(prefs.getBoolean("has_fuel_card", false))
    }

    // Live State variables bound to SharedPreferences with prefill defaults from the active vehicle
    var ownerName by remember(vehicle) {
        mutableStateOf(prefs.getString("card_owner", "") ?: "")
    }
    var plateNumber by remember(vehicle) {
        mutableStateOf(prefs.getString("card_plate", "") ?: "")
    }
    var vehicleType by remember(vehicle) {
        mutableStateOf(prefs.getString("card_vehicle_type", "") ?: "")
    }
    var vehicleModel by remember(vehicle) {
        mutableStateOf(prefs.getString("card_model", "") ?: "")
    }
    var vinCode by remember(vehicle) {
        mutableStateOf(prefs.getString("card_vin", "") ?: "")
    }
    var serialNumber by remember(vehicle) {
        mutableStateOf(prefs.getString("card_serial", "") ?: "")
    }

    // Fallbacks if user hasn't edited anything yet
    val displayOwner = ownerName.ifEmpty { "مالک خودرو" }
    val displayPlate = plateNumber.ifEmpty { vehicle?.formattedPlate ?: "۱۲ ب ۳۴۵ ایران ۱۱" }
    val displayType = vehicleType.ifEmpty {
        when {
            vehicle?.isMotorcycle == true -> "موتورسیکلت"
            vehicle?.plateLetter == "ت" -> "تاکسی"
            else -> "شخصی"
        }
    }
    val displayModel = vehicleModel.ifEmpty { vehicle?.title ?: "سمند LX" }
    val displayVin = vinCode.ifEmpty { "IR92345678012345" }
    val displaySerial = serialNumber.ifEmpty { "۱۰۹۵۴۸۷۲۳" }

    var isFlipped by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Smooth 3D Flip Animation (0 to 180 degrees)
    val rotationY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fuelCardFlip"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section Header Row with Edit & Delete Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEAB308).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = Color(0xFFFACC15),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "کارت سوخت من",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (hasFuelCard) "برای چرخاندن کارت ضربه بزنید • مدیریت اطلاعات 🔄" else "هنوز هیچ کارت سوختی ثبت نشده است 💳",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Edit & Delete button Row
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (hasFuelCard) {
                    FilledTonalIconButton(
                        onClick = {
                            prefs.edit().putBoolean("has_fuel_card", false).apply()
                            hasFuelCard = false
                            com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("کارت سوخت با موفقیت حذف شد 🗑️")
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color(0xFFEF4444)
                        ),
                        modifier = Modifier.size(36.dp).testTag("delete_fuel_card_button")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف کارت سوخت",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Edit button (Pencil)
                FilledTonalIconButton(
                    onClick = { showEditDialog = true },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color(0xFF38BDF8)
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "ویرایش اطلاعات کارت سوخت",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (!hasFuelCard) {
            // Placeholder Empty State for Fuel Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .testTag("fuel_card_placeholder"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.2.dp, Color(0xFFEA580C).copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEA580C).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "هنوز هیچ کارت سوختی ثبت نشده است",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "مشخصات کارت سوخت خود را وارد کنید تا شبیه‌ساز هوشمند آن فعال شود. تصاویر کارت عینا شبیه‌سازی خواهند شد.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showEditDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ثبت و ایجاد کارت سوخت", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        } else {
            // The 3D Flipping Card Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .graphicsLayer {
                        this.rotationY = rotationY
                        this.cameraDistance = 15f * density
                    }
                    .clickable { isFlipped = !isFlipped }
                    .testTag("fuel_card_3d_container")
            ) {
                if (rotationY <= 90f) {
                    // FRONT OF CARD
                    FuelCardFrontView(
                        plate = displayPlate,
                        owner = displayOwner,
                        type = displayType,
                        model = displayModel,
                        vin = displayVin,
                        serial = displaySerial,
                        quotaText = "سهمیه بنزین: ۶۰ لیتر"
                    )
                } else {
                    // BACK OF CARD
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                this.rotationY = 180f
                            }
                    ) {
                        FuelCardBackView(
                            plate = displayPlate,
                            owner = displayOwner,
                            type = displayType,
                            model = displayModel,
                            vin = displayVin,
                            serial = displaySerial,
                            quotaText = "سهمیه بنزین: ۶۰ لیتر"
                        )
                    }
                }
            }
        }
    }

    // --- Custom Dialog to Edit Fuel Card Information ---
    if (showEditDialog) {
        var inputOwner by remember { mutableStateOf(ownerName.ifEmpty { displayOwner }) }
        var inputPlate by remember { mutableStateOf(plateNumber.ifEmpty { displayPlate }) }
        var inputType by remember { mutableStateOf(vehicleType.ifEmpty { displayType }) }
        var inputModel by remember { mutableStateOf(vehicleModel.ifEmpty { displayModel }) }
        var inputVin by remember { mutableStateOf(vinCode.ifEmpty { displayVin }) }
        var inputSerial by remember { mutableStateOf(serialNumber.ifEmpty { displaySerial }) }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showEditDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .widthIn(max = 480.dp)
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.2.dp, Color(0xFF0284C7).copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                                }
                                Column {
                                    Text(
                                        text = "ویرایش اطلاعات کارت سوخت",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "مشخصات درج‌شده روی بدنه کارت هوشمند سوخت",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showEditDialog = false },
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E293B))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "بستن", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                            }
                        }

                        Divider(color = Color(0xFF334155))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 340.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = inputPlate,
                                onValueChange = { inputPlate = it },
                                label = { Text("ردیف ۱: پلاک ماشین") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = inputOwner,
                                onValueChange = { inputOwner = it },
                                label = { Text("ردیف ۲: نام صاحب کارت") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = inputType,
                                onValueChange = { inputType = it },
                                label = { Text("ردیف ۳: نوع خودرو (شخصی، تاکسی، ...)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = inputModel,
                                onValueChange = { inputModel = it },
                                label = { Text("ردیف ۴: مدل ماشین") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = inputVin,
                                onValueChange = { inputVin = it },
                                label = { Text("ردیف ۵: VIN خودرو (شروع با iR)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = inputSerial,
                                onValueChange = { inputSerial = it },
                                label = { Text("ردیف ۶: شماره سریال کارت") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showEditDialog = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF475569))
                            ) {
                                Text("انصراف", color = Color(0xFFCBD5E1), fontSize = 12.5.sp)
                            }

                            Button(
                                onClick = {
                                    ownerName = inputOwner
                                    plateNumber = inputPlate
                                    vehicleType = inputType
                                    vehicleModel = inputModel
                                    vinCode = inputVin
                                    serialNumber = inputSerial

                                    prefs.edit()
                                        .putString("card_owner", inputOwner)
                                        .putString("card_plate", inputPlate)
                                        .putString("card_vehicle_type", inputType)
                                        .putString("card_model", inputModel)
                                        .putString("card_vin", inputVin)
                                        .putString("card_serial", inputSerial)
                                        .putBoolean("has_fuel_card", true)
                                        .apply()

                                    hasFuelCard = true
                                    showEditDialog = false
                                    com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("مشخصات کارت سوخت شما به‌روزرسانی و ثبت شد ✅")
                                },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ذخیره و ثبت روی کارت", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// FRONT VIEW: Flat M3 Dark Theme Fuel Card (No Gradients, Soft Shadows)
// =============================================================================
@Composable
fun FuelCardFrontView(
    plate: String,
    owner: String,
    type: String,
    model: String,
    vin: String,
    serial: String,
    quotaText: String = "سهمیه بنزین: ۶۰ لیتر",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.2.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Smart Chip + Card Title & Flame Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Smart Chip Representation (Flat Gold)
                Surface(
                    modifier = Modifier.size(44.dp, 32.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEAB308),
                    border = BorderStroke(1.dp, Color(0xFFA16207))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        drawLine(Color(0xFFA16207), Offset(w * 0.33f, 0f), Offset(w * 0.33f, h), 1f)
                        drawLine(Color(0xFFA16207), Offset(w * 0.66f, 0f), Offset(w * 0.66f, h), 1f)
                        drawLine(Color(0xFFA16207), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), 1f)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "کارت هوشمند سوخت",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEA580C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Whatshot,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Middle: License Plate Badge & Primary Specs
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Standard Official Iranian National Plate (Blue band on left, Iran & city code on right)
                val parsedPlate = remember(plate) { parsePlateString(plate) }
                CompactNationalPlateView(
                    first2 = parsedPlate.first2,
                    letter = parsedPlate.letter,
                    last3 = parsedPlate.last3,
                    cityCode = parsedPlate.cityCode,
                    isLarge = true
                )

                // Grid of Primary Owner Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("نام مالک:", fontSize = 9.sp, color = Color(0xFF94A3B8))
                            Text(owner, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("مدل خودرو:", fontSize = 9.sp, color = Color(0xFF94A3B8))
                            Text(model, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Bottom Bar: Usage Type, Card Serial & Flip Hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF38BDF8).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "نوع کاربری: $type",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Sync,
                        contentDescription = "چرخش کارت",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "لمس برای چرخش 🔄",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

// =============================================================================
// BACK VIEW: Flat M3 Dark Theme Fuel Card (Complete 6-Row Data Display)
// =============================================================================
@Composable
fun FuelCardBackView(
    plate: String,
    owner: String,
    type: String,
    model: String,
    vin: String,
    serial: String,
    quotaText: String = "سهمیه بنزین: ۶۰ لیتر",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.2.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Card Back Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Badge,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "مشخصات فنی و شناسایی کارت سوخت",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF38BDF8)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = quotaText,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // 6 Complete Data Rows (Flat surface, solid background)
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                VehicleDataRow("۱. پلاک خودرو", plate)
                VehicleDataRow("۲. مشخصات صاحب کارت", owner)
                VehicleDataRow("۳. نوع کاربری خودرو", type)
                VehicleDataRow("۴. مدل وسیله نقلیه", model)
                VehicleDataRow("۵. شناسه کارت (VIN)", vin)
                VehicleDataRow("۶. شماره سریال کارت", serial)
            }

            // Footer Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "شرکت ملی پخش فرآورده‌های نفتی ایران",
                    fontSize = 9.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "🔄 جهت چرخاندن رو کلیک کنید",
                    fontSize = 9.5.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun VehicleDataRow(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF1E293B)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 9.5.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun BackDataRow(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    )
}

data class ParsedPlate(val first2: String, val letter: String, val last3: String, val cityCode: String)

fun parsePlateString(plateStr: String): ParsedPlate {
    val parts = plateStr.trim().split(Regex("[\\s\\-]+"))
    return if (parts.size >= 4) {
        ParsedPlate(parts[0], parts[1], parts[2], parts.last())
    } else {
        ParsedPlate("12", "ب", "345", "11")
    }
}


