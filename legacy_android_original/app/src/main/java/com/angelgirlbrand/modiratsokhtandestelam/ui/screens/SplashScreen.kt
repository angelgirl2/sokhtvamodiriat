package com.angelgirlbrand.modiratsokhtandestelam.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.angelgirlbrand.modiratsokhtandestelam.R
import com.angelgirlbrand.modiratsokhtandestelam.security.SecurityManager
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    securityManager: SecurityManager,
    onNavigateToNext: () -> Unit
) {
    val context = LocalContext.current
    var showTutorialDialog by remember { mutableStateOf(false) }

    // Dynamic animated progress
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    var progress by remember { mutableStateOf(0.1f) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (progress < 1.0f) {
            delay(100)
            progress += 0.05f
        }
        delay(300)
        // Check if first-run tutorial needed
        if (!securityManager.isTutorialSeen()) {
            showTutorialDialog = true
        } else {
            onNavigateToNext()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF003544),
                        Color(0xFF0284C7)
                    )
                )
            )
            .padding(24.dp)
    ) {
        // Center Logo & Title & Progress Bar
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF3A9DB5))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "لوگوی برنامه",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "مدیریت سوخت و استعلام",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "سامانه هوشمند خدمات، استعلام و بیمه خودرو",
                fontSize = 14.sp,
                color = Color(0xFFBAE6FD),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Dynamic loading progress bar
            Column(
                modifier = Modifier.fillMaxWidth(0.7f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF38BDF8),
                    trackColor = Color.White.copy(alpha = 0.2f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "در حال آماده‌سازی سامانه و اتصال داده‌ها...",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Bottom: Developer branding
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                color = Color.White.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "برنامه‌نویس",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "برنامه‌نویس: میلاد قنواتی (angelgirlbrand)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "نسخه ۱.۰",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }
    }

    // --- First-time Showcase / Tutorial Dialog ---
    if (showTutorialDialog) {
        Dialog(onDismissRequest = {
            securityManager.setTutorialSeen(true)
            showTutorialDialog = false
            onNavigateToNext()
        }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "به سامانه مدیریت سوخت خوش آمدید!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    TutorialPoint(
                        number = "۱",
                        title = "مدیریت صفر تا صد خودروها",
                        desc = "هیچ خودرویی از پیش تعریف نشده؛ با لمس دکمه «افزودن خودرو» وسیله نقلیه خود را ثبت و مصرف آن را رصد کنید."
                    )
                    TutorialPoint(
                        number = "۲",
                        title = "سامانه درخواست خدمات و صدور بیمه",
                        desc = "درخواست‌های صدور و تمدید انواع بیمه، کارت سوخت و خدمات خودرو را ثبت و وضعیت تایید را پیگیری کنید."
                    )
                    TutorialPoint(
                        number = "۳",
                        title = "استعلام جامع خلافی، عوارض و مالیات",
                        desc = "استعلام دقیق خلافی، عوارض آزادراهی، عوارض سالیانه و مالیات نقل و انتقال با ورود مشخصات خودرو و کد پستی."
                    )
                    TutorialPoint(
                        number = "۴",
                        title = "خروجی رسمی PDF و اکسل",
                        desc = "گزارش‌های تحلیلی و مستندات سوخت‌گیری را با یک لمس در قالب PDF و فایل سازگار با Excel استخراج و ارسال کنید."
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            securityManager.setTutorialSeen(true)
                            showTutorialDialog = false
                            onNavigateToNext()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("شروع استفاده از برنامه", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialPoint(number: String, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
