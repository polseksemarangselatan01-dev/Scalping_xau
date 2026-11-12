package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Candle
import com.example.ui.theme.BlueEma
import com.example.ui.theme.BuyGreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.SellRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    currentPrice: Double,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(NavyCardElevated, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Memuat data pasar...", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    var selectedCandleIndex by remember { mutableStateOf<Int?>(null) }
    val displayCandle = selectedCandleIndex?.let { if (it in candles.indices) candles[it] else null } ?: candles.last()

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(NavyCardElevated, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        // Top HUD for selected/latest candle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timeFormat.format(Date(displayCandle.timestamp)),
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("O: ", color = TextMuted, fontSize = 11.sp)
            Text(String.format("%.2f", displayCandle.open), color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Text("H: ", color = TextMuted, fontSize = 11.sp)
            Text(String.format("%.2f", displayCandle.high), color = BuyGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Text("L: ", color = TextMuted, fontSize = 11.sp)
            Text(String.format("%.2f", displayCandle.low), color = SellRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Text("C: ", color = TextMuted, fontSize = 11.sp)
            val closeColor = if (displayCandle.close >= displayCandle.open) BuyGreen else SellRed
            Text(String.format("%.2f", displayCandle.close), color = closeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        // Indicator Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(12.dp).height(2.dp).background(BlueEma))
            Spacer(modifier = Modifier.width(4.dp))
            Text("EMA 9", color = BlueEma, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.width(12.dp).height(2.dp).background(GoldPrimary))
            Spacer(modifier = Modifier.width(4.dp))
            Text("EMA 21", color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.weight(1f))
            Text("VOL: ${String.format("%.1f", displayCandle.volume)}", color = TextMuted, fontSize = 10.sp)
        }

        // Main Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(candles) {
                        detectDragGestures(
                            onDragEnd = { selectedCandleIndex = null },
                            onDragCancel = { selectedCandleIndex = null },
                            onDrag = { change, _ ->
                                val candleWidth = size.width / candles.size
                                val index = (change.position.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                                selectedCandleIndex = index
                            }
                        )
                    }
                    .pointerInput(candles) {
                        detectTapGestures { offset ->
                            val candleWidth = size.width / candles.size
                            val index = (offset.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                            selectedCandleIndex = index
                        }
                    }
            ) {
                val chartHeight = size.height * 0.78f
                val volumeTop = size.height * 0.82f
                val volumeHeight = size.height * 0.18f

                val visibleCandles = candles.takeLast(35)
                val candleCount = visibleCandles.size
                if (candleCount == 0) return@Canvas

                val minPrice = visibleCandles.minOf { it.low } - 0.50
                val maxPrice = visibleCandles.maxOf { it.high } + 0.50
                val priceRange = max(maxPrice - minPrice, 1.0)

                val maxVolume = max(visibleCandles.maxOf { it.volume }, 1.0)

                val candleSlotWidth = size.width / candleCount
                val candleBodyWidth = candleSlotWidth * 0.65f

                // Draw Grid Lines (3 horizontal price lines)
                val gridSteps = 4
                for (i in 0..gridSteps) {
                    val y = (chartHeight / gridSteps) * i
                    drawLine(
                        color = NavyBorder.copy(alpha = 0.6f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }

                // Prepare EMA paths
                val ema9Path = Path()
                val ema21Path = Path()
                var ema9Val = visibleCandles.first().close
                var ema21Val = visibleCandles.first().close
                val k9 = 2.0 / (9.0 + 1.0)
                val k21 = 2.0 / (21.0 + 1.0)

                // Render each candle
                visibleCandles.forEachIndexed { i, candle ->
                    val xCenter = (i * candleSlotWidth) + (candleSlotWidth / 2f)

                    // Price to Y coordinate conversion
                    val yOpen = chartHeight - (((candle.open - minPrice) / priceRange) * chartHeight).toFloat()
                    val yClose = chartHeight - (((candle.close - minPrice) / priceRange) * chartHeight).toFloat()
                    val yHigh = chartHeight - (((candle.high - minPrice) / priceRange) * chartHeight).toFloat()
                    val yLow = chartHeight - (((candle.low - minPrice) / priceRange) * chartHeight).toFloat()

                    val isBull = candle.close >= candle.open
                    val color = if (isBull) BuyGreen else SellRed

                    // 1. Draw Wick
                    drawLine(
                        color = color,
                        start = Offset(xCenter, yHigh),
                        end = Offset(xCenter, yLow),
                        strokeWidth = 1.6f
                    )

                    // 2. Draw Body
                    val bodyTop = min(yOpen, yClose)
                    val bodyBottom = max(yOpen, yClose)
                    val bodyHeight = max(bodyBottom - bodyTop, 2f)
                    drawRect(
                        color = color,
                        topLeft = Offset(xCenter - (candleBodyWidth / 2f), bodyTop),
                        size = Size(candleBodyWidth, bodyHeight)
                    )

                    // 3. Draw Volume Bar at bottom
                    val volBarHeight = ((candle.volume / maxVolume) * volumeHeight).toFloat()
                    drawRect(
                        color = color.copy(alpha = 0.45f),
                        topLeft = Offset(xCenter - (candleBodyWidth / 2f), size.height - volBarHeight),
                        size = Size(candleBodyWidth, volBarHeight)
                    )

                    // Compute EMA points
                    ema9Val = (candle.close * k9) + (ema9Val * (1.0 - k9))
                    ema21Val = (candle.close * k21) + (ema21Val * (1.0 - k21))

                    val yEma9 = chartHeight - (((ema9Val - minPrice) / priceRange) * chartHeight).toFloat()
                    val yEma21 = chartHeight - (((ema21Val - minPrice) / priceRange) * chartHeight).toFloat()

                    if (i == 0) {
                        ema9Path.moveTo(xCenter, yEma9)
                        ema21Path.moveTo(xCenter, yEma21)
                    } else {
                        ema9Path.lineTo(xCenter, yEma9)
                        ema21Path.lineTo(xCenter, yEma21)
                    }
                }

                // Draw EMA lines
                drawPath(path = ema9Path, color = BlueEma, style = Stroke(width = 2f))
                drawPath(path = ema21Path, color = GoldPrimary, style = Stroke(width = 2f))

                // Draw Current Price dashed line
                val currentY = chartHeight - (((currentPrice - minPrice) / priceRange) * chartHeight).toFloat()
                if (currentY in 0f..chartHeight) {
                    drawLine(
                        color = GoldPrimary,
                        start = Offset(0f, currentY),
                        end = Offset(size.width, currentY),
                        strokeWidth = 1.8f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                }

                // Crosshair cursor if user is dragging
                selectedCandleIndex?.let { idx ->
                    if (idx in 0 until candleCount) {
                        val crosshairX = (idx * candleSlotWidth) + (candleSlotWidth / 2f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.7f),
                            start = Offset(crosshairX, 0f),
                            end = Offset(crosshairX, size.height),
                            strokeWidth = 1.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        )
                    }
                }
            }
        }
    }
}
