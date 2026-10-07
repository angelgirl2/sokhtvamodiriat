package com.angelgirlbrand.modiratsokhtandestelam.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.angelgirlbrand.modiratsokhtandestelam.data.local.entity.VehicleEntity

// =============================================================================
// UTILITIES: PERSIAN & ENGLISH DIGIT CONVERTERS
// =============================================================================

fun toPersianDigits(input: String): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return input.map { ch ->
        if (ch in '0'..'9') persianDigits[ch - '0'] else ch
    }.joinToString("")
}

fun toEnglishDigits(input: String): String {
    return input.map { ch ->
        when (ch) {
            '۰' -> '0'; '۱' -> '1'; '۲' -> '2'; '۳' -> '3'; '۴' -> '4'
            '۵' -> '5'; '۶' -> '6'; '۷' -> '7'; '۸' -> '8'; '۹' -> '9'
            else -> ch
        }
    }.joinToString("")
}

// =============================================================================
// COMPLETE 32 STANDARD PERSIAN ALPHABET + SPECIAL PLATE CATEGORIES
// =============================================================================

data class PersianPlateLetterItem(
    val letter: String,
    val name: String,
    val description: String,
    val isYellow: Boolean = false,
    val isSpecial: Boolean = false,
    val badgeText: String = "سفید"
)

// The full 32 Persian alphabet as explicitly requested:
// الف، ب، پ، ت، ث، ج، چ، ح، خ، د، ذ، ر، ز، ژ، س، ش، ص، ض، ط، ظ، ع، غ، ف، ق، ک، گ، ل، م، ن، و، ه، ی
val ALL_32_PERSIAN_LETTERS: List<PersianPlateLetterItem> = listOf(
    PersianPlateLetterItem("الف", "الف", "خودروهای شخصی و اداری", isYellow = false),
    PersianPlateLetterItem("ب", "ب", "خودرو سواری شخصی", isYellow = false),
    PersianPlateLetterItem("پ", "پ", "پلاک انتظامی سواری", isYellow = false),
    PersianPlateLetterItem("ت", "ت (تاکسی)", "تاکسی رسمی با نشان بین‌المللی TAXI", isYellow = true, badgeText = "زرد TAXI"),
    PersianPlateLetterItem("ث", "ث", "پلاک انتظامی سواری", isYellow = false),
    PersianPlateLetterItem("ج", "ج", "خودرو سواری شخصی", isYellow = false),
    PersianPlateLetterItem("چ", "چ", "پلاک انتظامی سواری", isYellow = false),
    PersianPlateLetterItem("ح", "ح", "پلاک انتظامی سواری", isYellow = false),
    PersianPlateLetterItem("خ", "خ", "پلاک انتظامی سواری", isYellow = false),
    PersianPlateLetterItem("د", "د", "خودرو سواری شخصی", isYellow = false),
    PersianPlateLetterItem("ذ", "ذ", "پلاک انتظامی سواری", isYellow = false),
    PersianPlateLetterItem("ر", "ر", "پلاک انتظامی سواری", isYellow = false),
    PersianPlateLetterItem("ز", "ز", "پلاک انتظامی سواری", isYellow = false),
    PersianPlateLetterItem("ژ", "ژ", "پلاک انتظامی سواری", isYellow = false),
    PersianPlateLetterItem("س", "س", "خودرو سواری شخصی", isYellow = false),
    PersianPlateLetterItem("ش", "ش", "پلاک انتظامی سواری", isYellow = false),
    PersianPlateLetterItem("ص", "ص", "خودرو سواری شخصی", isYellow = false),
    PersianPlateLetterItem("ض", "ض", "پلاک انتظامی سواری", isYellow = false),
    PersianPlateLetterItem("ط", "ط", "خودرو سواری شخصی", isYellow = false),
    PersianPlateLetterItem("ظ", "ظ", "پلاک انتظامی سواری", isYellow = false),
    PersianPlateLetterItem("ع", "ع (عمومی)", "ناوگان عمومی، اتوبوس و باربری", isYellow = true, badgeText = "زرد عمومی"),
    PersianPlateLetterItem("غ", "غ", "پلاک انتظامی سواری", isYellow = false),
    PersianPlateLetterItem("ف", "ف", "پلاک انتظامی سواری", isYellow = false),
    PersianPlateLetterItem("ق", "ق", "خودرو سواری شخصی", isYellow = false),
    PersianPlateLetterItem("ک", "ک (کشاورزی)", "ادوات کشاورزی و خدمات صنعتی", isYellow = true, badgeText = "زرد خدمات"),
    PersianPlateLetterItem("گ", "گ (گذر موقت)", "تردد موقت بین‌المللی", isYellow = false, isSpecial = true, badgeText = "گذر موقت"),
    PersianPlateLetterItem("ل", "ل", "خودرو سواری شخصی", isYellow = false),
    PersianPlateLetterItem("م", "م", "خودرو سواری شخصی", isYellow = false),
    PersianPlateLetterItem("ن", "ن", "خودرو سواری شخصی", isYellow = false),
    PersianPlateLetterItem("و", "و", "خودرو سواری شخصی", isYellow = false),
    PersianPlateLetterItem("ه", "هـ", "خودرو سواری شخصی", isYellow = false),
    PersianPlateLetterItem("ی", "ی", "خودرو سواری شخصی", isYellow = false),
    PersianPlateLetterItem("♿", "ویلچر", "پلاک ویژه معلولین و جانبازان", isYellow = false, isSpecial = true, badgeText = "معلولین")
)

// =============================================================================
// ENUM & STYLES: DYNAMIC LICENSE PLATE THEMES (NON-MILITARY & NON-GOVERNMENT)
// =============================================================================

enum class PlateCategory(
    val title: String,
    val description: String,
    val sampleLetter: String,
    val previewBgColor: Color,
    val previewTextColor: Color
) {
    PERSONAL("شخصی", "پلاک سواری عادی (سفید)", "ب", Color.White, Color.Black),
    TAXI("تاکسی", "تاکسی زرد با نشان رسمی TAXI", "ت", Color(0xFFFACC15), Color.Black),
    PUBLIC("عمومی", "کامیون، اتوبوس و ناوگان عمومی", "ع", Color(0xFFFACC15), Color.Black),
    AGRICULTURE("کشاورزی", "تراکتور و ادوات کشاورزی", "ک", Color(0xFFFACC15), Color.Black),
    DISABLED("معلولین", "پلاک جانبازان و معلولین", "♿", Color.White, Color(0xFF1D4ED8)),
    TRANSIT("گذر موقت", "پلاک خودروهای گذر موقت", "گ", Color.White, Color.Black),
    ARVAND("اروندی", "منطقه آزاد اروند (۲ زبانه فارسی/لاتین)", "اروند", Color.White, Color.Black),
    MOTORCYCLE("موتورسیکلت", "پلاک ملی موتور (۳ بالا / ۵ پایین)", "موتور", Color.White, Color.Black)
}

fun resolvePlateCategory(letter: String, vehicleType: String = ""): PlateCategory {
    val cleanLetter = letter.trim()
    val cleanType = vehicleType.trim()
    return when {
        cleanType == VehicleEntity.TYPE_ARVAND || cleanLetter == "اروند" -> PlateCategory.ARVAND
        cleanType == VehicleEntity.TYPE_MOTORCYCLE || cleanLetter == "موتور" -> PlateCategory.MOTORCYCLE
        cleanLetter == "ت" -> PlateCategory.TAXI
        cleanLetter == "ع" -> PlateCategory.PUBLIC
        cleanLetter == "ک" -> PlateCategory.AGRICULTURE
        cleanLetter == "♿" || cleanLetter == "معلولین" -> PlateCategory.DISABLED
        cleanLetter == "گ" -> PlateCategory.TRANSIT
        else -> PlateCategory.PERSONAL
    }
}

// =============================================================================
// 1. SMART ADAPTIVE LICENSE PLATE COMPONENT (کامپوننت جدید و هوشمند پلاک)
//    - ابعاد استاندارد و متناسب (Standard Compact Dimensions)
//    - تغییر خودکار و هوشمند رنگ بر اساس ورودی (زرد تاکسی/عمومی، سفید عادی/معلولین)
//    - پشتیبانی کامل از پلاک ملی خودرو، منطقه آزاد اروند و موتورسیکلت
// =============================================================================

@Composable
fun SmartAdaptivePlateView(
    first2: String = "",
    letter: String = "",
    last3: String = "",
    cityCode: String = "",
    isArvand: Boolean = false,
    arvandNumber: String = "",
    isMotorcycle: Boolean = false,
    motoTop3: String = "",
    motoBottom5: String = "",
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    when {
        isArvand || letter == "اروند" -> {
            CompactArvandPlateView(
                plateNumber = arvandNumber.ifEmpty { last3.ifEmpty { first2 } }.ifEmpty { "12365" },
                isLarge = isLarge,
                modifier = modifier
            )
        }
        isMotorcycle || letter == "موتور" -> {
            CompactMotorcyclePlateView(
                top3 = motoTop3.ifEmpty { first2 }.ifEmpty { "123" },
                bottom5 = motoBottom5.ifEmpty { last3 }.ifEmpty { "45678" },
                isLarge = isLarge,
                modifier = modifier
            )
        }
        else -> {
            CompactNationalPlateView(
                first2 = first2.ifEmpty { "12" },
                letter = letter.ifEmpty { "ب" },
                last3 = last3.ifEmpty { "345" },
                cityCode = cityCode.ifEmpty { "11" },
                isLarge = isLarge,
                modifier = modifier
            )
        }
    }
}

@Composable
fun SmartAdaptivePlateView(
    vehicle: VehicleEntity,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    SmartAdaptivePlateView(
        first2 = vehicle.plateFirst2,
        letter = vehicle.plateLetter,
        last3 = vehicle.plateLast3,
        cityCode = vehicle.plateCityCode,
        isArvand = vehicle.isArvand,
        arvandNumber = vehicle.plateLast3.ifEmpty { vehicle.plateFirst2 },
        isMotorcycle = vehicle.isMotorcycle,
        motoTop3 = vehicle.plateFirst2,
        motoBottom5 = vehicle.plateLast3,
        isLarge = isLarge,
        modifier = modifier
    )
}

// Backward Compatibility Aliases
@Composable
fun VehiclePlateView(vehicle: VehicleEntity, modifier: Modifier = Modifier) =
    SmartAdaptivePlateView(vehicle = vehicle, modifier = modifier)

@Composable
fun IranianPlateView(first2: String, letter: String, last3: String, cityCode: String, modifier: Modifier = Modifier) =
    CompactNationalPlateView(first2 = first2, letter = letter, last3 = last3, cityCode = cityCode, modifier = modifier)

@Composable
fun ArvandPlateView(plateNumber: String, modifier: Modifier = Modifier, isYellowCommercial: Boolean = false) =
    CompactArvandPlateView(plateNumber = plateNumber, modifier = modifier)

@Composable
fun IranianMotorcyclePlateView(top3: String, bottom5: String, modifier: Modifier = Modifier) =
    CompactMotorcyclePlateView(top3 = top3, bottom5 = bottom5, modifier = modifier)

// =============================================================================
// 2. COMPACT NATIONAL CAR PLATE (پلاک خودرو در ابعاد استاندارد با تغییر رنگ هوشمند)
// =============================================================================

@Composable
fun CompactNationalPlateView(
    first2: String,
    letter: String,
    last3: String,
    cityCode: String,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val category = remember(letter) { resolvePlateCategory(letter) }

    // Dynamic color determination based on user entered plate letter
    val targetBgColor = when (category) {
        PlateCategory.TAXI, PlateCategory.PUBLIC, PlateCategory.AGRICULTURE -> Color(0xFFFACC15) // Yellow
        else -> Color.White // Personal / Disabled / Transit
    }

    val targetTextColor = Color(0xFF0F172A)
    val targetDividerColor = when (category) {
        PlateCategory.TAXI, PlateCategory.PUBLIC, PlateCategory.AGRICULTURE -> Color(0xFF78350F).copy(alpha = 0.4f)
        else -> Color(0xFFCBD5E1)
    }

    val animatedBg by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "plateBgAnim"
    )

    val animatedDivider by animateColorAsState(
        targetValue = targetDividerColor,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "plateDividerAnim"
    )

    // Sizing and layout measurements scaled based on isLarge
    val heightVal = if (isLarge) 56.dp else 34.dp
    val widthVal = if (isLarge) 275.dp else 172.dp
    val fontSizeMain = if (isLarge) 24.sp else 15.sp
    val fontSizeLetter = if (isLarge) 22.sp else 14.5.sp
    val fontSizeIranLabel = if (isLarge) 11.sp else 7.5.sp
    val fontSizeCityCode = if (isLarge) 20.sp else 13.sp
    val blueBandWidth = if (isLarge) 32.dp else 20.dp
    val flagWidth = if (isLarge) 22.dp else 14.dp
    val flagHeight = if (isLarge) 11.dp else 7.dp
    val spaceBetween = if (isLarge) 6.dp else 4.dp
    val spaceBetweenSmall = if (isLarge) 3.dp else 2.dp
    val spaceBetweenDivider = if (isLarge) 5.dp else 3.dp
    val colWidthDigits = if (isLarge) 35.dp else 22.dp
    val colWidthLetter = if (isLarge) 35.dp else 22.dp
    val colWidthLast3 = if (isLarge) 52.dp else 33.dp
    val colWidthCityCode = if (isLarge) 44.dp else 28.dp

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(if (isLarge) 6.dp else 4.dp))
                .background(animatedBg)
                .border(if (isLarge) 2.2.dp else 1.5.dp, Color(0xFF0F172A), RoundedCornerShape(if (isLarge) 6.dp else 4.dp))
                .height(heightVal)
                .width(widthVal)
                .padding(horizontal = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Far Left: Blue Band (Flag of Iran + I.R. IRAN)
            Column(
                modifier = Modifier
                    .width(blueBandWidth)
                    .fillMaxHeight()
                    .background(Color(0xFF1D4ED8))
                    .padding(vertical = if (isLarge) 3.dp else 1.5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Official Flag of Iran
                Column(
                    modifier = Modifier
                        .width(flagWidth)
                        .height(flagHeight)
                        .clip(RoundedCornerShape(0.5.dp))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFF22C55E)))
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.White))
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFFEF4444)))
                }
                Text(
                    text = "I.R.",
                    color = Color.White,
                    fontSize = if (isLarge) 8.5.sp else 5.5.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isLarge) 9.sp else 6.sp
                )
                Text(
                    text = "IRAN",
                    color = Color.White,
                    fontSize = if (isLarge) 7.5.sp else 4.8.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isLarge) 8.sp else 5.sp
                )
            }

            Spacer(modifier = Modifier.width(spaceBetween))

            // 2. First 2 Digits (e.g. ۲۱)
            Text(
                text = toPersianDigits(first2.ifEmpty { "12" }),
                color = targetTextColor,
                fontSize = fontSizeMain,
                fontWeight = FontWeight.Black,
                modifier = Modifier.width(colWidthDigits),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(spaceBetweenSmall))

            // 3. Center Persian Letter or Special Badge (TAXI, Wheelchair, etc.)
            Box(
                modifier = Modifier
                    .width(colWidthLetter)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                when (category) {
                    PlateCategory.TAXI -> {
                        // Taxi: Letter "ت" with miniature "TAXI" label right on top
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "TAXI",
                                color = Color.Black,
                                fontSize = if (isLarge) 8.sp else 5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.3.sp,
                                lineHeight = if (isLarge) 9.sp else 6.sp
                            )
                            Text(
                                text = "ت",
                                color = Color.Black,
                                fontSize = if (isLarge) 21.sp else 13.5.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                lineHeight = if (isLarge) 23.sp else 15.sp
                            )
                        }
                    }
                    PlateCategory.DISABLED -> {
                        // Wheelchair icon for Disabled / Veterans
                        Icon(
                            imageVector = Icons.Default.Accessible,
                            contentDescription = "معلولین",
                            tint = Color(0xFF1D4ED8),
                            modifier = Modifier.size(if (isLarge) 25.dp else 16.dp)
                        )
                    }
                    else -> {
                        // Standard or Public letter
                        Text(
                            text = letter.ifEmpty { "ب" },
                            color = targetTextColor,
                            fontSize = fontSizeLetter,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(spaceBetweenSmall))

            // 4. Last 3 Digits (e.g. ۳۴۵)
            Text(
                text = toPersianDigits(last3.ifEmpty { "345" }),
                color = targetTextColor,
                fontSize = fontSizeMain,
                fontWeight = FontWeight.Black,
                modifier = Modifier.width(colWidthLast3),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(spaceBetweenDivider))

            // 5. Vertical divider line
            Box(
                modifier = Modifier
                    .width(if (isLarge) 1.8.dp else 1.2.dp)
                    .fillMaxHeight(0.82f)
                    .background(animatedDivider)
            )

            Spacer(modifier = Modifier.width(spaceBetweenDivider))

            // 6. Far Right Box: ایران + City Code (e.g. ایران ۱۱)
            Column(
                modifier = Modifier
                    .width(colWidthCityCode)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ایران",
                    color = targetTextColor.copy(alpha = 0.85f),
                    fontSize = fontSizeIranLabel,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isLarge) 12.sp else 8.sp
                )
                Text(
                    text = toPersianDigits(cityCode.ifEmpty { "11" }),
                    color = targetTextColor,
                    fontSize = fontSizeCityCode,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isLarge) 22.sp else 14.sp
                )
            }
        }
    }
}

// =============================================================================
// 2.5 RIGHT BLUE BAND NATIONAL PLATE (پلاک ملی با نوار آبی در سمت راست)
// =============================================================================

@Composable
fun RightBlueBandNationalPlateView(
    first2: String,
    letter: String,
    last3: String,
    cityCode: String,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val category = remember(letter) { resolvePlateCategory(letter) }

    val targetBgColor = when (category) {
        PlateCategory.TAXI, PlateCategory.PUBLIC, PlateCategory.AGRICULTURE -> Color(0xFFFACC15) // Yellow
        else -> Color.White
    }

    val targetTextColor = Color(0xFF0F172A)
    val targetDividerColor = when (category) {
        PlateCategory.TAXI, PlateCategory.PUBLIC, PlateCategory.AGRICULTURE -> Color(0xFF78350F).copy(alpha = 0.4f)
        else -> Color(0xFFCBD5E1)
    }

    val animatedBg by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "plateBgAnim"
    )

    val animatedDivider by animateColorAsState(
        targetValue = targetDividerColor,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "plateDividerAnim"
    )

    val heightVal = if (isLarge) 56.dp else 34.dp
    val widthVal = if (isLarge) 275.dp else 172.dp
    val fontSizeMain = if (isLarge) 24.sp else 15.sp
    val fontSizeLetter = if (isLarge) 22.sp else 14.5.sp
    val fontSizeIranLabel = if (isLarge) 11.sp else 7.5.sp
    val fontSizeCityCode = if (isLarge) 20.sp else 13.sp
    val blueBandWidth = if (isLarge) 32.dp else 20.dp
    val flagWidth = if (isLarge) 22.dp else 14.dp
    val flagHeight = if (isLarge) 11.dp else 7.dp
    val spaceBetween = if (isLarge) 6.dp else 4.dp
    val spaceBetweenSmall = if (isLarge) 3.dp else 2.dp
    val spaceBetweenDivider = if (isLarge) 5.dp else 3.dp
    val colWidthDigits = if (isLarge) 35.dp else 22.dp
    val colWidthLetter = if (isLarge) 35.dp else 22.dp
    val colWidthLast3 = if (isLarge) 52.dp else 33.dp
    val colWidthCityCode = if (isLarge) 44.dp else 28.dp

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(if (isLarge) 6.dp else 4.dp))
                .background(animatedBg)
                .border(if (isLarge) 2.2.dp else 1.5.dp, Color(0xFF0F172A), RoundedCornerShape(if (isLarge) 6.dp else 4.dp))
                .height(heightVal)
                .width(widthVal)
                .padding(horizontal = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Far Left: City Code + "ایران" (ایران ۱۱)
            Column(
                modifier = Modifier
                    .width(colWidthCityCode)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ایران",
                    color = targetTextColor.copy(alpha = 0.85f),
                    fontSize = fontSizeIranLabel,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isLarge) 12.sp else 8.sp
                )
                Text(
                    text = toPersianDigits(cityCode.ifEmpty { "11" }),
                    color = targetTextColor,
                    fontSize = fontSizeCityCode,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isLarge) 22.sp else 14.sp
                )
            }

            Spacer(modifier = Modifier.width(spaceBetweenDivider))

            // 2. Vertical divider line
            Box(
                modifier = Modifier
                    .width(if (isLarge) 1.8.dp else 1.2.dp)
                    .fillMaxHeight(0.82f)
                    .background(animatedDivider)
            )

            Spacer(modifier = Modifier.width(spaceBetweenDivider))

            // 3. Last 3 Digits (e.g. ۳۴۵)
            Text(
                text = toPersianDigits(last3.ifEmpty { "345" }),
                color = targetTextColor,
                fontSize = fontSizeMain,
                fontWeight = FontWeight.Black,
                modifier = Modifier.width(colWidthLast3),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(spaceBetweenSmall))

            // 4. Center Persian Letter or Special Badge (TAXI, Wheelchair, etc.)
            Box(
                modifier = Modifier
                    .width(colWidthLetter)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                when (category) {
                    PlateCategory.TAXI -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "TAXI",
                                color = Color.Black,
                                fontSize = if (isLarge) 8.sp else 5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.3.sp,
                                lineHeight = if (isLarge) 9.sp else 6.sp
                            )
                            Text(
                                text = "ت",
                                color = Color.Black,
                                fontSize = if (isLarge) 21.sp else 13.5.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                lineHeight = if (isLarge) 23.sp else 15.sp
                            )
                        }
                    }
                    PlateCategory.DISABLED -> {
                        Icon(
                            imageVector = Icons.Default.Accessible,
                            contentDescription = "معلولین",
                            tint = Color(0xFF1D4ED8),
                            modifier = Modifier.size(if (isLarge) 25.dp else 16.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = letter.ifEmpty { "ب" },
                            color = targetTextColor,
                            fontSize = fontSizeLetter,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(spaceBetweenSmall))

            // 5. First 2 Digits (e.g. ۱۲)
            Text(
                text = toPersianDigits(first2.ifEmpty { "12" }),
                color = targetTextColor,
                fontSize = fontSizeMain,
                fontWeight = FontWeight.Black,
                modifier = Modifier.width(colWidthDigits),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(spaceBetween))

            // 6. Far Right: Blue Band (Flag of Iran + I.R. IRAN)
            Column(
                modifier = Modifier
                    .width(blueBandWidth)
                    .fillMaxHeight()
                    .background(Color(0xFF1D4ED8))
                    .padding(vertical = if (isLarge) 3.dp else 1.5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .width(flagWidth)
                        .height(flagHeight)
                        .clip(RoundedCornerShape(0.5.dp))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFF22C55E)))
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.White))
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFFEF4444)))
                }
                Text(
                    text = "I.R.",
                    color = Color.White,
                    fontSize = if (isLarge) 8.5.sp else 5.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isLarge) 9.sp else 6.sp
                )
                Text(
                    text = "IRAN",
                    color = Color.White,
                    fontSize = if (isLarge) 7.5.sp else 4.5.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isLarge) 8.sp else 5.sp
                )
            }
        }
    }
}

// =============================================================================
// 3. COMPACT ARVAND FREE ZONE PLATE (پلاک استاندارد منطقه آزاد اروند)
// =============================================================================

@Composable
fun CompactArvandPlateView(
    plateNumber: String,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val persianDigits = toPersianDigits(plateNumber.ifEmpty { "12365" })
    val englishDigits = toEnglishDigits(plateNumber.ifEmpty { "12365" })

    val heightVal = if (isLarge) 58.dp else 36.dp
    val widthVal = if (isLarge) 260.dp else 162.dp
    val blueSectionWidth = if (isLarge) 74.dp else 46.dp
    val fontSizePersian = if (isLarge) 21.sp else 13.5.sp
    val fontSizeEnglish = if (isLarge) 19.sp else 12.5.sp
    val flagWidth = if (isLarge) 21.dp else 13.dp
    val flagHeight = if (isLarge) 10.dp else 6.5.dp
    val arvandTextSize = if (isLarge) 10.sp else 6.5.sp
    val emblemWidth = if (isLarge) 26.dp else 16.dp
    val emblemHeight = if (isLarge) 16.dp else 10.dp

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(if (isLarge) 6.dp else 4.dp))
                .background(Color.White)
                .border(if (isLarge) 2.2.dp else 1.5.dp, Color(0xFF0F172A), RoundedCornerShape(if (isLarge) 6.dp else 4.dp))
                .height(heightVal)
                .width(widthVal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Blue Section: Iran Flag + I.R. IRAN + Arvand Emblem + "ARVAND" Text
            Column(
                modifier = Modifier
                    .width(blueSectionWidth)
                    .fillMaxHeight()
                    .background(Color(0xFF0038A8))
                    .padding(vertical = if (isLarge) 2.dp else 1.dp, horizontal = if (isLarge) 4.dp else 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Iran Flag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .width(flagWidth)
                            .height(flagHeight)
                            .clip(RoundedCornerShape(0.5.dp))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFF22C55E)))
                        Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.White))
                        Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFFEF4444)))
                    }
                    Text(
                        text = "I.R. IRAN",
                        color = Color.White,
                        fontSize = if (isLarge) 7.sp else 4.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Middle: Arvand Crest (geometric stylized badge)
                Box(
                    modifier = Modifier
                        .size(emblemWidth, emblemHeight)
                        .clip(RoundedCornerShape(1.dp))
                        .background(Color(0xFF1E3A8A))
                        .border(if (isLarge) 1.dp else 0.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(1.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val redPath = Path().apply {
                            moveTo(w * 0.45f, 0f)
                            lineTo(w, 0f)
                            lineTo(w, h)
                            lineTo(w * 0.45f, h)
                            close()
                        }
                        drawPath(redPath, Color(0xFFEF4444))

                        val whitePath = Path().apply {
                            moveTo(w * 0.40f, 0f)
                            lineTo(w * 0.58f, 0f)
                            lineTo(w * 0.48f, h)
                            lineTo(w * 0.30f, h)
                            close()
                        }
                        drawPath(whitePath, Color.White)
                    }
                }

                // Bottom: "ARVAND"
                Text(
                    text = "ARVAND",
                    color = Color.White,
                    fontSize = arvandTextSize,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isLarge) 11.sp else 7.sp
                )
            }

            // Divider line
            Box(
                modifier = Modifier
                    .width(if (isLarge) 1.8.dp else 1.2.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF0F172A))
            )

            // Right Section: 2 Tiers (Top = Persian numbers, Bottom = English numbers)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Top Row: Persian Digits
                Text(
                    text = persianDigits,
                    color = Color(0xFF0F172A),
                    fontSize = fontSizePersian,
                    fontWeight = FontWeight.Black,
                    letterSpacing = if (isLarge) 2.sp else 1.2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = if (isLarge) 23.sp else 14.sp
                )

                // Divider line between Persian and English digits
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(if (isLarge) 1.5.dp else 1.dp)
                        .background(Color(0xFF0F172A).copy(alpha = 0.8f))
                )

                // Bottom Row: English Digits
                Text(
                    text = englishDigits,
                    color = Color(0xFF0F172A),
                    fontSize = fontSizeEnglish,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = if (isLarge) 2.sp else 1.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = if (isLarge) 21.sp else 13.sp
                )
            }
        }
    }
}

// =============================================================================
// 4. COMPACT MOTORCYCLE PLATE (پلاک استاندارد موتورسیکلت)
// =============================================================================

@Composable
fun CompactMotorcyclePlateView(
    top3: String,
    bottom5: String,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val topDigits = toPersianDigits(top3.ifEmpty { "123" })
    val bottomDigits = toPersianDigits(bottom5.ifEmpty { "45678" })

    val heightVal = if (isLarge) 60.dp else 38.dp
    val widthVal = if (isLarge) 172.dp else 108.dp
    val blueFlagWidth = if (isLarge) 27.dp else 17.dp
    val flagWidth = if (isLarge) 19.dp else 12.dp
    val flagHeight = if (isLarge) 9.5.dp else 6.0.dp
    val fontSizeTop = if (isLarge) 19.sp else 12.sp
    val fontSizeBottom = if (isLarge) 19.sp else 12.sp
    val screwSize = if (isLarge) 6.dp else 4.dp

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White)
                .border(if (isLarge) 2.2.dp else 1.5.dp, Color(0xFF0F172A), RoundedCornerShape(5.dp))
                .height(heightVal)
                .width(widthVal)
                .padding(horizontal = if (isLarge) 5.dp else 3.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Blue Flag Box on Top-Left
            Column(
                modifier = Modifier
                    .width(blueFlagWidth)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF0038A8))
                    .padding(vertical = if (isLarge) 2.dp else 1.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .width(flagWidth)
                        .height(flagHeight)
                        .clip(RoundedCornerShape(0.5.dp))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFF22C55E)))
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.White))
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFFEF4444)))
                }
                Text(
                    text = "I.R.",
                    color = Color.White,
                    fontSize = if (isLarge) 7.sp else 4.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "IRAN",
                    color = Color.White,
                    fontSize = if (isLarge) 6.5.sp else 4.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(if (isLarge) 5.dp else 3.dp))

            // Simulated Mounting Screw Hole
            Box(
                modifier = Modifier
                    .size(screwSize)
                    .clip(CircleShape)
                    .background(Color(0xFF94A3B8))
            )

            // Two-tiered motorcycle plate numbers
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = topDigits,
                    color = Color(0xFF0F172A),
                    fontSize = fontSizeTop,
                    fontWeight = FontWeight.Black,
                    letterSpacing = if (isLarge) 2.sp else 1.2.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isLarge) 20.sp else 13.sp
                )
                Text(
                    text = bottomDigits,
                    color = Color(0xFF0F172A),
                    fontSize = fontSizeBottom,
                    fontWeight = FontWeight.Black,
                    letterSpacing = if (isLarge) 2.sp else 1.2.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isLarge) 20.sp else 13.sp
                )
            }

            // Right Mounting Screw Hole
            Box(
                modifier = Modifier
                    .size(screwSize)
                    .clip(CircleShape)
                    .background(Color(0xFF94A3B8))
            )
        }
    }
}

// =============================================================================
// 5. SOPHISTICATED & PROFESSIONAL 32-LETTER LICENSE PLATE PICKER
//    - شامل تمامی ۳۲ حرف استاندارد الفبای فارسی:
//      (الف، ب، پ، ت، ث، ج، چ، ح، خ، د، ذ، ر، ز، ژ، س، ش، ص، ض، ط، ظ، ع، غ، ف، ق، ک، گ، ل، م، ن، و، ه، ی)
//    - طراحی فوق‌العاده حرفه‌ای، شکیل و چندلایه با سربرگ گرادیان تم اصلی اپ
//    - نوار جستجوی سریع و فیلتر حروف
//    - چیپ‌های دسته‌بندی و پیش‌نمایش گرافیکی لحظه‌ای پلاک
// =============================================================================

@Composable
fun IranianPlateLetterPicker(
    selectedLetter: String,
    onLetterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showModal by remember { mutableStateOf(false) }

    val currentCat = remember(selectedLetter) { resolvePlateCategory(selectedLetter) }

    // Clickable button trigger with active letter and color dot
    OutlinedButton(
        onClick = { showModal = true },
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
        border = BorderStroke(
            1.2.dp,
            if (currentCat == PlateCategory.TAXI || currentCat == PlateCategory.PUBLIC || currentCat == PlateCategory.AGRICULTURE) Color(0xFFEAB308)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Live color dot indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(currentCat.previewBgColor)
                    .border(1.dp, Color(0xFF0F172A), CircleShape)
            )
            Text(
                text = selectedLetter.ifEmpty { "ب" },
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (showModal) {
        ChicPlateLetterPickerDialog(
            currentLetter = selectedLetter,
            onLetterSelected = {
                onLetterSelected(it)
                showModal = false
            },
            onDismiss = { showModal = false }
        )
    }
}

@Composable
fun ChicPlateLetterPickerDialog(
    currentLetter: String,
    onLetterSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var previewLetter by remember { mutableStateOf(currentLetter.ifEmpty { "ب" }) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterCategory by remember { mutableStateOf("همه") }

    // Filter items based on user search and category
    val filteredLetters = remember(searchQuery, selectedFilterCategory) {
        ALL_32_PERSIAN_LETTERS.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                item.letter.contains(searchQuery.trim()) ||
                item.name.contains(searchQuery.trim()) ||
                item.description.contains(searchQuery.trim())

            val matchesCategory = when (selectedFilterCategory) {
                "سواری (سفید)" -> !item.isYellow && !item.isSpecial
                "تاکسی و عمومی (زرد)" -> item.isYellow
                "ویژه و گذر موقت" -> item.isSpecial
                else -> true // "همه"
            }

            matchesSearch && matchesCategory
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
        ) {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .widthIn(max = 520.dp)
                    .padding(vertical = 16.dp)
            ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. Luxury Gradient Header matching App Theme
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF0369A1),
                                    Color(0xFF0284C7)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "انتخابگر جامع حروف پلاک",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "۳۲ حرف استاندارد فارسی و رده‌های تاکسی، عمومی و ویژه",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFBAE6FD)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "بستن",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 2. Realistic License Plate Preview with Live Hover/Selected Letter
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "پیش‌نمایش زنده پلاک استاندارد:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when {
                                        previewLetter == "ت" || previewLetter == "ع" || previewLetter == "ک" -> Color(0xFFFEF08A)
                                        previewLetter == "♿" -> Color(0xFFDBEAFE)
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                ) {
                                    Text(
                                        text = when {
                                            previewLetter == "ت" -> "🚕 تاکسی (نشان TAXI)"
                                            previewLetter == "ع" -> "🚛 عمومی و باربری"
                                            previewLetter == "ک" -> "🚜 ادوات کشاورزی"
                                            previewLetter == "♿" -> "♿ معلولین و جانبازان"
                                            previewLetter == "گ" -> "🌐 گذر موقت"
                                            else -> "🚗 سواری (سفید)"
                                        },
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            previewLetter == "ت" || previewLetter == "ع" || previewLetter == "ک" -> Color(0xFF854D0E)
                                            previewLetter == "♿" -> Color(0xFF1E40AF)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier.padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CompactNationalPlateView(
                                    first2 = "21",
                                    letter = previewLetter,
                                    last3 = "845",
                                    cityCode = "11"
                                )
                            }
                        }
                    }

                    // 3. Search and Quick Filter Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("جستجوی حرف (مثلا: ب، ت، س، ع)...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "پاک کردن", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 4. Category Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val categories = listOf("همه", "سواری (سفید)", "تاکسی و عمومی (زرد)", "ویژه و گذر موقت")
                        items(categories) { cat ->
                            val isSelected = selectedFilterCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilterCategory = cat },
                                label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // 5. Complete Alphabet Grid (۳۲ حرف با طراحی غنی و متالیک)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        items(filteredLetters, key = { it.letter }) { item ->
                            val isSelected = previewLetter == item.letter
                            Surface(
                                onClick = {
                                    previewLetter = item.letter
                                    onLetterSelected(item.letter)
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                                    item.isYellow -> Color(0xFFFEF9C3)
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                border = BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        item.isYellow -> Color(0xFFEAB308).copy(alpha = 0.7f)
                                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    }
                                ),
                                shadowElevation = if (isSelected) 4.dp else 1.dp,
                                modifier = Modifier
                                    .height(60.dp)
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    if (item.letter == "♿") {
                                        Icon(
                                            Icons.Default.Accessible,
                                            contentDescription = "معلولین",
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1D4ED8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Text(
                                            text = item.letter,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Black,
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                item.isYellow -> Color(0xFF713F12)
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.badgeText,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            item.isYellow -> Color(0xFF854D0E)
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 6. Action Button
                    Button(
                        onClick = { onLetterSelected(previewLetter) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تایید حرف «$previewLetter» و ثبت در پلاک",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }
    }
}
}
