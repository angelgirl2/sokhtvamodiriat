package com.angelgirlbrand.modiratsokhtandestelam.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.angelgirlbrand.modiratsokhtandestelam.R
import com.angelgirlbrand.modiratsokhtandestelam.security.SecurityManager
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    securityManager: SecurityManager,
    onNavigateToNext: () -> Unit
) {
    // Dynamic animations: Floating & Ambient Halo Glow
    val infiniteTransition = rememberInfiniteTransition(label = "splashAnimations")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloPulse"
    )

    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    var targetProgress by remember { mutableStateOf(0.12f) }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 380, easing = LinearOutSlowInEasing),
        label = "animatedProgress"
    )

    // Smooth exit transition state
    var isExiting by remember { mutableStateOf(false) }
    val exitAlpha by animateFloatAsState(
        targetValue = if (isExiting) 0f else 1f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "exitAlpha"
    )
    val exitScale by animateFloatAsState(
        targetValue = if (isExiting) 1.06f else 1f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "exitScale"
    )

    // Brand-new, distinct and diverse Persian loading status messages
    val statusMessage = remember(targetProgress) {
        when {
            targetProgress < 0.28f -> "راه‌اندازی پایگاه داده و بارگذاری پروفایل خودرو..."
            targetProgress < 0.55f -> "محاسبه دقیق مانده سهمیه بنزین ماهانه و نرخ آزاد..."
            targetProgress < 0.78f -> "ارتباط با درگاه یکپارچه استعلام تخلفات و عوارض..."
            targetProgress < 0.95f -> "آماده‌سازی پایش مصرف، یادآورهای نگهداری و بیمه..."
            else -> "تکمیل بارگذاری سیستم؛ در حال انتقال به برنامه..."
        }
    }

    LaunchedEffect(Unit) {
        delay(200)
        targetProgress = 0.28f
        delay(420)
        targetProgress = 0.52f
        delay(450)
        targetProgress = 0.76f
        delay(480)
        targetProgress = 0.92f
        delay(400)
        targetProgress = 1.0f
        delay(350)

        // Smooth exit animation
        isExiting = true
        delay(380)

        // Proceed to next destination (Onboarding if first run, otherwise Main Dashboard)
        onNavigateToNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0C2138), // Midnight Navy
                        Color(0xFF071220),
                        Color(0xFF030710)  // Obsidian Black
                    ),
                    center = Offset(500f, 650f),
                    radius = 1200f
                )
            )
            .graphicsLayer {
                alpha = exitAlpha
                scaleX = exitScale
                scaleY = exitScale
            }
            .padding(24.dp)
    ) {
        // Decorative ambient glow on Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.38f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF0284C7).copy(alpha = 0.22f), Color.Transparent),
                    center = center,
                    radius = size.width * 0.65f * haloPulse
                ),
                center = center,
                radius = size.width * 0.65f * haloPulse
            )
        }

        // Center Content: Hero Logo, Title, Badge & Dynamic Progress
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Logo with rotating neon accent ring & floating animation
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(y = floatOffset.dp)
                    .size(175.dp)
            ) {
                // Outer rotating gradient track ring
                Canvas(
                    modifier = Modifier
                        .size(170.dp)
                        .rotate(ringRotation)
                ) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFF38BDF8),
                                Color.Transparent,
                                Color(0xFF0D9488),
                                Color.Transparent,
                                Color(0xFF38BDF8)
                            )
                        ),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Glowing Halo Backdrop
                Box(
                    modifier = Modifier
                        .size(145.dp)
                        .scale(haloPulse)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF0284C7).copy(alpha = 0.38f), Color.Transparent)
                            )
                        )
                )

                // Main App Icon Container with Glassmorphic Border
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = Color(0xFF0B192C),
                    border = BorderStroke(
                        1.8.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF38BDF8).copy(alpha = 0.85f),
                                Color(0xFF0284C7).copy(alpha = 0.35f),
                                Color(0xFF0D9488).copy(alpha = 0.70f)
                            )
                        )
                    ),
                    shadowElevation = 20.dp,
                    modifier = Modifier.size(130.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "لوگوی سامانه",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(22.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 2. High-Tech Category Pill Badge (New Sentence)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0284C7).copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Text(
                        text = "سامانه پایش هوشمند سهمیه و تردد خودرو",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7DD3FC),
                        letterSpacing = 0.3.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. App Title (New Sentence)
            Text(
                text = "مدیریت جامع سوخت و تردد",
                fontSize = 25.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Subtitle / Features hint (New Sentence)
            Text(
                text = "کارت سوخت دیجیتال • ثبت سوخت‌گیری • استعلام جریمه و خدمات",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(38.dp))

            // 5. Redesigned Futuristic Loading & Progress Section
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .clip(RoundedCornerShape(20.dp)),
                color = Color(0xFF0B1728).copy(alpha = 0.90f),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top row: Status message & Persian percentage indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier
                                    .size(15.dp)
                                    .rotate(ringRotation * 2)
                            )
                            Text(
                                text = statusMessage,
                                fontSize = 11.sp,
                                color = Color(0xFFE2E8F0),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }

                        // Percentage badge in Persian
                        val percentageInt = (animatedProgress * 100).toInt().coerceIn(0, 100)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0284C7).copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "${toPersianDigits(percentageInt.toString())}٪",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Dual-color High-tech Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E293B))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF0284C7),
                                            Color(0xFF38BDF8),
                                            Color(0xFF10B981)
                                        )
                                    )
                                )
                        )
                    }

                    // 4 Step Indicator Dots (New distinct labels)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepIndicatorDot(step = 1, currentProgress = animatedProgress, targetFraction = 0.25f, label = "اطلاعات")
                        StepIndicatorDot(step = 2, currentProgress = animatedProgress, targetFraction = 0.50f, label = "سهمیه")
                        StepIndicatorDot(step = 3, currentProgress = animatedProgress, targetFraction = 0.75f, label = "خدمات")
                        StepIndicatorDot(step = 4, currentProgress = animatedProgress, targetFraction = 1.0f, label = "آماده")
                    }
                }
            }
        }

        // Bottom: Developer Signature & Version Badge (Refined)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.05f),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "angelgirlbrand",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "نسخه ۳.۲ • ذخیره‌سازی امن محلی و شبیه‌ساز استاندارد",
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun StepIndicatorDot(step: Int, currentProgress: Float, targetFraction: Float, label: String) {
    val isCompleted = currentProgress >= targetFraction
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isCompleted) Color(0xFF10B981) else Color(0xFF475569))
        )
        Text(
            text = label,
            fontSize = 9.5.sp,
            color = if (isCompleted) Color(0xFF93C5FD) else Color(0xFF64748B),
            fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun toPersianDigits(input: String): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return input.map { ch ->
        if (ch in '0'..'9') persianDigits[ch - '0'] else ch
    }.joinToString("")
}
