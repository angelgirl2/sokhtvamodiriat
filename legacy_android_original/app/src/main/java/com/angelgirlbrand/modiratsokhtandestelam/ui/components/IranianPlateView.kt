package com.angelgirlbrand.modiratsokhtandestelam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IranianPlateView(
    first2: String,
    letter: String,
    last3: String,
    cityCode: String,
    modifier: Modifier = Modifier,
    isYellowTaxi: Boolean = false
) {
    val plateBg = if (isYellowTaxi) Color(0xFFFACC15) else Color.White
    val plateBorder = Color(0xFF0F172A)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(plateBg)
            .border(2.dp, plateBorder, RoundedCornerShape(6.dp))
            .height(48.dp)
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Blue Band (Flag of Iran + I.R. IRAN)
        Column(
            modifier = Modifier
                .width(26.dp)
                .fillMaxHeight()
                .background(Color(0xFF1D4ED8))
                .padding(vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Mini Iranian flag representation
            Column(
                modifier = Modifier
                    .width(18.dp)
                    .height(9.dp)
                    .clip(RoundedCornerShape(1.dp))
            ) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFF22C55E)))
                Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.White))
                Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFFEF4444)))
            }
            Text(
                text = "I.R.",
                color = Color.White,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "IRAN",
                color = Color.White,
                fontSize = 6.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // First 2 Digits
        Text(
            text = first2.ifEmpty { "12" },
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Center Persian Letter
        Box(
            modifier = Modifier
                .padding(horizontal = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter.ifEmpty { "ب" },
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Last 3 Digits
        Text(
            text = last3.ifEmpty { "345" },
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.width(46.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Vertical divider
        Box(
            modifier = Modifier
                .width(1.5.dp)
                .fillMaxHeight(0.85f)
                .background(Color(0xFFCBD5E1))
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Right Box: ایران + City Code
        Column(
            modifier = Modifier
                .width(36.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ایران",
                color = Color(0xFF1E293B),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = cityCode.ifEmpty { "11" },
                color = Color.Black,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}
