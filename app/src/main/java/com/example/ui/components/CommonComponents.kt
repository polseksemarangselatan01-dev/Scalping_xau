package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionState
import com.example.data.model.SignalType
import com.example.data.model.Timeframe
import com.example.ui.theme.BuyGreen
import com.example.ui.theme.BuyGreenContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavySurface
import com.example.ui.theme.SellRed
import com.example.ui.theme.SellRedContainer
import com.example.ui.theme.StrongBuyNeon
import com.example.ui.theme.StrongSellCrimson
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WaitAmber
import com.example.ui.theme.WaitAmberContainer

@Composable
fun ConnectionBadge(
    state: ConnectionState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val (bg, contentColor, text, icon) = when (state) {
        ConnectionState.LIVE -> Quad(
            Color(0xFF00381B),
            BuyGreen,
            "LIVE FEED",
            Icons.Default.Wifi
        )
        ConnectionState.DEMO -> Quad(
            Color(0xFF132847),
            Color(0xFF64B5F6),
            "DEMO MODE",
            Icons.Default.Info
        )
        ConnectionState.OFFLINE -> Quad(
            Color(0xFF381014),
            SellRed,
            "OFFLINE",
            Icons.Default.WifiOff
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, contentColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state == ConnectionState.LIVE) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(contentColor)
                    .alpha(alphaAnim)
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun SignalBadge(
    type: SignalType,
    modifier: Modifier = Modifier
) {
    val (bg, fg, border) = when (type) {
        SignalType.STRONG_BUY -> Triple(BuyGreenContainer, StrongBuyNeon, StrongBuyNeon)
        SignalType.BUY -> Triple(BuyGreenContainer, BuyGreen, BuyGreen.copy(alpha = 0.6f))
        SignalType.WAIT -> Triple(WaitAmberContainer, WaitAmber, WaitAmber.copy(alpha = 0.6f))
        SignalType.SELL -> Triple(SellRedContainer, SellRed, SellRed.copy(alpha = 0.6f))
        SignalType.STRONG_SELL -> Triple(SellRedContainer, StrongSellCrimson, StrongSellCrimson)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = type.title,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
fun TimeframeSelector(
    selected: Timeframe,
    onSelect: (Timeframe) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(NavySurface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Timeframe.values().forEach { tf ->
            val isSelected = tf == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) GoldPrimary else Color.Transparent)
                    .clickable { onSelect(tf) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tf.label,
                    color = if (isSelected) Color.Black else TextMuted,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    valueColor: Color = TextWhite,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = NavyCardElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = GoldPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextDim,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun ConfluenceMatrixBar(
    confluenceScore: Int, // 1 to 4
    bias: SignalType,
    modifier: Modifier = Modifier
) {
    val barColor = when (bias) {
        SignalType.STRONG_BUY, SignalType.BUY -> BuyGreen
        SignalType.STRONG_SELL, SignalType.SELL -> SellRed
        else -> WaitAmber
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Multi-Timeframe Confluence", color = TextMuted, fontSize = 12.sp)
            Text(
                text = "$confluenceScore / 4 TF",
                color = barColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 1..4) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (i <= confluenceScore) barColor else NavyBorder)
                )
            }
        }
    }
}
