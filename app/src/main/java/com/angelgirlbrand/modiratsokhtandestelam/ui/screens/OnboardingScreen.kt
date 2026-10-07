package com.angelgirlbrand.modiratsokhtandestelam.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.angelgirlbrand.modiratsokhtandestelam.R
import com.angelgirlbrand.modiratsokhtandestelam.security.SecurityManager

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val badge: String,
    val highlightPoints: List<String>
)

@Composable
fun OnboardingScreen(
    securityManager: SecurityManager,
    onFinish: () -> Unit
) {
    val steps = remember {
        listOf(
            OnboardingStep(
                title = "کارت هوشمند و سهمیه سوخت",
                subtitle = "شبیه‌ساز واقعی با پلاک ملی استاندارد و چرخش ۳بعدی",
                description = "مشاهده دقیق کارت سوخت با لمس و چرخش سه‌بعدی؛ نمایش پلاک رسمی با پرچم ایران و کد شهر، محاسبه هوشمند مانده سهمیه بنزین یارانه‌ای ۶۰ لیتری و آزاد.",
                icon = Icons.Default.CreditCard,
                accentColor = Color(0xFF0284C7),
                badge = "بخش ۱: کارت سوخت دیجیتال",
                highlightPoints = listOf(
                    "پلاک استاندارد ملی با پرچم ایران و تفکیک حروف",
                    "پشت و روی کارت با چرخش سه‌بعدی و نمایش VIN و سریال",
                    "پایش مانده سهمیه ماهانه خودرو و موتورسیکلت"
                )
            ),
            OnboardingStep(
                title = "پایش آنلاین حجم باک و مصرف",
                subtitle = "تحلیل دقیق درصد باک، هزینه‌ها و شعاع پیمایش",
                description = "با هر بار بنزین زدن، میزان سوخت باک به صورت درصد دقیق و اعشاری محاسبه می‌شود و نمودار گرافیکی، هشدار باک رزرو و کیلومتر تخمینی پیمایش را به شما ارائه می‌دهد.",
                icon = Icons.Default.LocalGasStation,
                accentColor = Color(0xFFEA580C),
                badge = "بخش ۲: پایش باک و سوخت",
                highlightPoints = listOf(
                    "نمودار پیشرفته Pie Chart حجم باک لحظه‌ای",
                    "هشدار ورود به حالت باک رزرو و کمبود سوخت",
                    "محاسبه شعاع پیمایش تا سوخت‌گیری بعدی و ثبت هزینه"
                )
            ),
            OnboardingStep(
                title = "استعلام خلافی و عوارض آزادراهی",
                subtitle = "استعلام با شماره پلاک و تسویه در درگاه شاپرک",
                description = "فقط با وارد کردن شماره پلاک، ریز خلافی و عوارض بزرگراهی خودرو یا موتورسیکلت خود را استعلام کرده و به صورت آنی با کد رهگیری تسویه نمایید.",
                icon = Icons.Default.ReceiptLong,
                accentColor = Color(0xFF0D9488),
                badge = "بخش ۳: سامانه استعلام و تسویه",
                highlightPoints = listOf(
                    "استعلام فوری فقط با پلاک رسمی خودرو و موتور",
                    "امکان انتخاب آسان پلاک از بین وسایل نقلیه ثبت‌شده",
                    "تسویه در درگاه امن بانکی و تاریخچه تراکنش‌ها"
                )
            ),
            OnboardingStep(
                title = "یادآور سرویس دوره‌ای و بیمه",
                subtitle = "نگهداری اصولی خودرو و پیشگیری از جرائم بیمه‌ای",
                description = "موعد تعویض روغن موتور، لنت ترمز، فیلترها، تسمه تایم و تاریخ انقضای بیمه‌نامه شخص ثالث را ثبت کنید تا قبل از فرارسیدن موعد، هشدارهای خودکار دریافت نمایید.",
                icon = Icons.Default.Build,
                accentColor = Color(0xFF8B5CF6),
                badge = "بخش ۴: سرویس و نگهداری",
                highlightPoints = listOf(
                    "محاسبه موعد سرویس بر اساس کیلومتر کارکرد واقعی",
                    "پایش روزشمار اعتبار بیمه‌نامه شخص ثالث و بدنه",
                    "آرشیو سوابق سرویس‌های انجام‌شده برای حفظ سلامت خودرو"
                )
            ),
            OnboardingStep(
                title = "راهنماهای تعاملی گام‌به‌گام",
                subtitle = "کارت‌های راهنمای اختصاصی در هر بخش از برنامه",
                description = "برای سادگی کار، در بالای هر بخش (کارت سوخت، استعلام، سرویس‌ها، تنظیمات) یک کارت راهنمای کوتاه قرار دارد که فقط یک‌بار نمایش داده شده و نکات کاربردی را آموزش می‌دهد.",
                icon = Icons.Default.Lightbulb,
                accentColor = Color(0xFF10B981),
                badge = "بخش ۵: راهنمای بخش‌ها",
                highlightPoints = listOf(
                    "نمایش هوشمند فقط برای بار اول در هر بخش",
                    "امکان بستن سریع با دکمه «متوجه شدم»",
                    "امکان بازنشانی و مشاهده مجدد در منوی تنظیمات"
                )
            )
        )
    }

    var currentStepIndex by remember { mutableStateOf(0) }
    val step = steps[currentStepIndex]
    val isLastStep = currentStepIndex == steps.size - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0B1728),
                        Color(0xFF070F1C),
                        Color(0xFF030712)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Brand Identifier, Skip button & Step indicator
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Brand Badge with mini logo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0284C7).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                        }
                        Text(
                            text = "راهنمای آشنایی با سامانه (angelgirlbrand)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    TextButton(
                        onClick = {
                            securityManager.setOnboardingSeen(true)
                            onFinish()
                        }
                    ) {
                        Text(
                            text = "رد شدن و ورود",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Step progress dots indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        steps.indices.forEach { index ->
                            Box(
                                modifier = Modifier
                                    .height(5.dp)
                                    .width(if (index == currentStepIndex) 28.dp else 10.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (index == currentStepIndex) step.accentColor
                                        else Color(0xFF334155)
                                    )
                            )
                        }
                    }
                }
            }

            // Animated Step Card Content
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    fadeIn(animationSpec = tween(280)) togetherWith fadeOut(animationSpec = tween(280))
                },
                label = "stepTransition",
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) { currentStep ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Spacer(modifier = Modifier.height(6.dp))

                    // Step Icon with glowing aura
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(105.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(currentStep.accentColor.copy(alpha = 0.22f))
                        )
                        Surface(
                            shape = RoundedCornerShape(26.dp),
                            color = Color(0xFF132034),
                            border = BorderStroke(1.5.dp, currentStep.accentColor.copy(alpha = 0.7f)),
                            shadowElevation = 14.dp,
                            modifier = Modifier.size(76.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = currentStep.icon,
                                    contentDescription = null,
                                    tint = currentStep.accentColor,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }
                    }

                    // Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = currentStep.accentColor.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, currentStep.accentColor.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = currentStep.badge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentStep.accentColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    // Title
                    Text(
                        text = currentStep.title,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    // Subtitle
                    Text(
                        text = currentStep.subtitle,
                        fontSize = 12.5.sp,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    // Description
                    Text(
                        text = currentStep.description,
                        fontSize = 12.5.sp,
                        color = Color(0xFFCBD5E1),
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Key highlights list
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF132034).copy(alpha = 0.85f)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            currentStep.highlightPoints.forEach { point ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = currentStep.accentColor,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Text(
                                        text = point,
                                        fontSize = 12.sp,
                                        color = Color(0xFFE2E8F0),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStepIndex > 0) {
                    OutlinedButton(
                        onClick = { currentStepIndex-- },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "قبلی",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "قبلی",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = {
                        if (isLastStep) {
                            securityManager.setOnboardingSeen(true)
                            onFinish()
                        } else {
                            currentStepIndex++
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = step.accentColor),
                    modifier = Modifier
                        .weight(if (currentStepIndex > 0) 1.6f else 1f)
                        .height(50.dp)
                ) {
                    Text(
                        text = if (isLastStep) "شروع و ورود به سامانه" else "مرحله بعد",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isLastStep) Icons.Default.DoneAll else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
