package com.angelgirlbrand.modiratsokhtandestelam.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.CompactNationalPlateView
import com.angelgirlbrand.modiratsokhtandestelam.ui.components.IranianMotorcyclePlateView
import com.angelgirlbrand.modiratsokhtandestelam.util.ScannedVehicleInfo
import com.angelgirlbrand.modiratsokhtandestelam.util.VehicleQrParser
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleQrScannerScreen(
    onNavigateBack: () -> Unit,
    onVehicleScannedAndConfirmed: (VehicleEntity) -> Unit,
    onEditManually: (ScannedVehicleInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("دسترسی دوربین برای اسکن بارکد الزامی است")
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    BackHandler {
        onNavigateBack()
    }

    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var scannedResult by remember { mutableStateOf<ScannedVehicleInfo?>(null) }
    var isProcessingScan by remember { mutableStateOf(false) }

    // Haptic feedback trigger
    fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(70)
            }
        } catch (_: Exception) {}
    }

    // Laser Animation for Reticle
    val infiniteTransition = rememberInfiniteTransition(label = "scannerLaser")
    val laserYFraction by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserY"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "اسکن بارکد و QR کد کارت خودرو",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "شناسایی هوشمند مشخصات و سهمیه کارت",
                            fontSize = 10.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Flashlight / Torch Toggle Button
                    IconButton(
                        onClick = {
                            val newTorchState = !isTorchOn
                            cameraControl?.enableTorch(newTorchState)
                            isTorchOn = newTorchState
                        }
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "چراغ قوه",
                            tint = if (isTorchOn) Color(0xFFFBBF24) else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF090D16).copy(alpha = 0.90f)
                )
            )
        },
        containerColor = Color(0xFF030712),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (hasCameraPermission) {
                // 1. Live CameraX Preview & ML Kit Analyzer
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        val cameraExecutor = Executors.newSingleThreadExecutor()

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val options = BarcodeScannerOptions.Builder()
                                .setBarcodeFormats(
                                    Barcode.FORMAT_QR_CODE,
                                    Barcode.FORMAT_DATA_MATRIX,
                                    Barcode.FORMAT_CODE_128,
                                    Barcode.FORMAT_CODE_39
                                )
                                .build()
                            val barcodeScanner = BarcodeScanning.getClient(options)

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                @androidx.annotation.OptIn(ExperimentalGetImage::class)
                                val mediaImage = imageProxy.image
                                if (mediaImage != null && !isProcessingScan) {
                                    val inputImage = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    barcodeScanner.process(inputImage)
                                        .addOnSuccessListener { barcodes ->
                                            if (barcodes.isNotEmpty() && !isProcessingScan) {
                                                val barcode = barcodes.first()
                                                val rawValue = barcode.rawValue
                                                if (!rawValue.isNullOrBlank()) {
                                                    isProcessingScan = true
                                                    triggerHaptic()
                                                    val parsedInfo = VehicleQrParser.parse(rawValue)
                                                    scannedResult = parsedInfo
                                                }
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            try {
                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                                cameraControl = camera.cameraControl
                            } catch (exc: Exception) {
                                exc.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    }
                )

                // 2. Viewfinder Dark Overlay with Target Frame
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val frameSize = size.width * 0.76f
                    val left = (size.width - frameSize) / 2f
                    val top = (size.height - frameSize) / 2.4f

                    // Semi-transparent background mask
                    drawRect(
                        color = Color.Black.copy(alpha = 0.58f)
                    )

                    // Clear center cutout for scanner
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(left, top),
                        size = Size(frameSize, frameSize),
                        cornerRadius = CornerRadius(28f, 28f),
                        blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                    )

                    // Frame outer border
                    drawRoundRect(
                        color = Color(0xFF38BDF8).copy(alpha = 0.8f),
                        topLeft = Offset(left, top),
                        size = Size(frameSize, frameSize),
                        cornerRadius = CornerRadius(28f, 28f),
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    // Laser scanline animation
                    val laserY = top + (frameSize * laserYFraction)
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFF10B981),
                                Color(0xFF38BDF8),
                                Color(0xFF10B981),
                                Color.Transparent
                            )
                        ),
                        start = Offset(left + 10f, laserY),
                        end = Offset(left + frameSize - 10f, laserY),
                        strokeWidth = 3.5.dp.toPx()
                    )
                }

                // 3. Scanner Instructions & Actions Overlay
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Hint Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "بارکد یا QR کد کارت یا برگ سبز را در کادر قرار دهید",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    // Bottom Controls: Manual Entry
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        // Fallback manual entry
                        OutlinedButton(
                            onClick = {
                                onEditManually(
                                    ScannedVehicleInfo(
                                        title = "خودرو جدید",
                                        plateFirst2 = "24",
                                        plateLetter = "ب",
                                        plateLast3 = "852",
                                        plateCityCode = "68"
                                    )
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ورود دستی مشخصات خودرو بدون اسکن",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            } else {
                // Camera Permission Denied View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "نیاز به دسترسی دوربین",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "جهت اسکن خودکار QR کد و بارکد کارت خودرو، دسترسی به دوربین الزامی است.",
                        fontSize = 12.5.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Icon(imageVector = Icons.Default.LockOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "اعطای دسترسی دوربین", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 4. Scanned Vehicle Confirmation Custom Dialog
            if (scannedResult != null) {
                val scanned = scannedResult!!
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = {
                        scannedResult = null
                        isProcessingScan = false
                    },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
                    ) {
                        Card(
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .padding(vertical = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Header
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF10B981), Color(0xFF059669))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "اطلاعات کارت خودرو شناسایی شد",
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "مشخصات خودرو را بررسی و تایید نمایید",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.6f))

                                // Vehicle Title & Type Badge
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, Color(0xFF334155)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = scanned.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "ظرفیت باک: ${scanned.tankCapacity.toInt()} لیتر • سوخت: ${scanned.fuelType}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF7DD3FC)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF0284C7).copy(alpha = 0.25f),
                                            border = BorderStroke(0.6.dp, Color(0xFF0284C7).copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = scanned.vehicleType,
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF38BDF8),
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                // Standard Iranian Plate Display
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (scanned.vehicleType == VehicleEntity.TYPE_MOTORCYCLE) {
                                        IranianMotorcyclePlateView(
                                            top3 = scanned.plateFirst2,
                                            bottom5 = scanned.plateLast3
                                        )
                                    } else {
                                        CompactNationalPlateView(
                                            first2 = scanned.plateFirst2,
                                            letter = scanned.plateLetter,
                                            last3 = scanned.plateLast3,
                                            cityCode = scanned.plateCityCode,
                                            isLarge = true
                                        )
                                    }
                                }

                                // VIN if available
                                if (scanned.vin.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF1E293B).copy(alpha = 0.7f),
                                        border = BorderStroke(0.6.dp, Color(0xFF334155)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "شماره VIN:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                            Text(text = scanned.vin, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            scannedResult = null
                                            isProcessingScan = false
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFF475569)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = "اسکن مجدد", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            val vehicleEntity = scanned.toVehicleEntity()
                                            onVehicleScannedAndConfirmed(vehicleEntity)
                                            com.angelgirlbrand.modiratsokhtandestelam.util.AppToast.show("✅ وسیله نقلیه «${vehicleEntity.title}» افزوده شد")
                                            scannedResult = null
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        modifier = Modifier.weight(1.4f)
                                    ) {
                                        Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "تایید و ثبت خودرو", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
