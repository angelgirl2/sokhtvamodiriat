package com.angelgirlbrand.modiratsokhtandestelam.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.FuelLogEntity

@Composable
fun FuelAnalysisWidget(
    logs: List<FuelLogEntity>,
    modifier: Modifier = Modifier
) {
    val analysis = remember(logs) {
        performFuelAnalysis(logs)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.2.dp, Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF38BDF8))))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "تحلیل هوشمند الگوی مصرف",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "پیشنهادات بهینه‌سازی اختصاصی رانندگی شما",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF334155))

            if (logs.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "برای تحلیل دقیق‌تر الگوی رانندگی، حداقل ۲ سابقه سوخت‌گیری ثبت کنید. ✨",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                // Key Insight Card
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(analysis.color.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(analysis.icon, contentDescription = null, tint = analysis.color, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(
                                text = "مصرف متوسط: ${analysis.consumptionRate} لیتر در ۱۰۰ کیلومتر",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = analysis.summary,
                                fontSize = 11.sp,
                                color = analysis.color
                            )
                        }
                    }
                }

                // AI Suggestions List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "💡 پیشنهادات برای شما:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCBD5E1)
                    )
                    
                    analysis.suggestions.forEachIndexed { index, suggestion ->
                        SuggestionItem(text = suggestion, delay = index * 100)
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(text: String, delay: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.TipsAndUpdates,
                contentDescription = null,
                tint = Color(0xFFFACC15),
                modifier = Modifier.size(16.dp).padding(top = 2.dp)
            )
            Text(
                text = text,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                lineHeight = 16.sp
            )
        }
    }
}

private data class FuelAnalysisData(
    val consumptionRate: String,
    val summary: String,
    val suggestions: List<String>,
    val icon: ImageVector,
    val color: Color
)

private fun performFuelAnalysis(logs: List<FuelLogEntity>): FuelAnalysisData {
    if (logs.size < 2) return FuelAnalysisData("---", "", emptyList(), Icons.Default.Speed, Color.Gray)

    val sortedLogs = logs.sortedByDescending { it.dateMillis }
    val latest = sortedLogs.first()
    val previous = sortedLogs[1]

    val distance = latest.odometer - previous.odometer
    val rate = if (distance > 0) (latest.liters / distance.toDouble()) * 100.0 else 0.0
    val rateStr = "%.1f".format(rate)

    val suggestions = mutableListOf<String>()
    
    val (summary, icon, color) = when {
        rate > 10.0 -> {
            suggestions.add("فشار باد لاستیک‌ها را هر دو هفته یکبار چک کنید؛ کم‌بادی مصرف را تا ۵٪ افزایش می‌دهد.")
            suggestions.add("تعویض فیلتر هوا و شمع‌ها در سرویس بعدی برای بهبود احتراق الزامی است.")
            suggestions.add("از شتاب‌گیری ناگهانی پرهیز کنید؛ رانندگی نرم تا ۲۰٪ در هزینه‌ها صرفه‌جویی می‌کند.")
            Triple("مصرف سوخت بالا شناسایی شد ⚠️", Icons.Default.Speed, Color(0xFFEF4444))
        }
        rate > 7.5 -> {
            suggestions.add("استفاده از کروز کنترل در بزرگراه‌ها به ثبات مصرف سوخت کمک می‌کند.")
            suggestions.add("بار اضافی صندوق عقب را تخلیه کنید تا وزن خودرو کاهش یابد.")
            suggestions.add("در توقف‌های بیش از ۱ دقیقه، خاموش کردن موتور بهینه است.")
            Triple("الگوی رانندگی شهری معمولی ✅", Icons.Default.ElectricBolt, Color(0xFFF59E0B))
        }
        else -> {
            suggestions.add("الگوی رانندگی شما بسیار بهینه است. همین روند را حفظ کنید! 🌟")
            suggestions.add("سرویس‌های دوره‌ای منظم، ضامن ماندگاری این عملکرد عالی است.")
            suggestions.add("گزارش PDF را برای بررسی دقیق‌تر روند کاهشی دانلود کنید.")
            Triple("بهره‌وری سوخت بسیار عالی 🚀", Icons.Default.AutoAwesome, Color(0xFF10B981))
        }
    }

    // Additional insight based on frequency
    if (sortedLogs.size >= 3) {
        val last3 = sortedLogs.take(3)
        val avgGap = (last3.first().dateMillis - last3.last().dateMillis) / (2 * 24 * 3600 * 1000L)
        if (avgGap < 4) {
            suggestions.add("فواصل سوخت‌گیری شما کوتاه است. رانندگی در ترافیک سنگین یا مسیرهای کوتاه شناسایی شد.")
        }
    }

    return FuelAnalysisData(rateStr, summary, suggestions, icon, color)
}
