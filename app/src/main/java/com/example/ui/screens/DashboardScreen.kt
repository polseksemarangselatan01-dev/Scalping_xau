package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionState
import com.example.data.model.SignalType
import com.example.ui.components.CandlestickChart
import com.example.ui.components.ConnectionBadge
import com.example.ui.components.MetricCard
import com.example.ui.components.SignalBadge
import com.example.ui.components.TimeframeSelector
import com.example.ui.theme.BuyGreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyBlack
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.SellRed
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.TradingViewModel

@Composable
fun DashboardScreen(
    viewModel: TradingViewModel,
    onNavigateToAnalyzer: () -> Unit,
    onNavigateToSignals: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ticker by viewModel.ticker.collectAsState()
    val timeframe by viewModel.selectedTimeframe.collectAsState()
    val candles by viewModel.currentCandles.collectAsState()
    val analysis by viewModel.currentAnalysis.collectAsState()
    val mtfSummary by viewModel.mtfSummary.collectAsState()
    val latestSignal by viewModel.latestSignal.collectAsState()

    val isPriceUp = ticker.change >= 0
    val priceChangeColor = if (isPriceUp) BuyGreen else SellRed

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("dashboard_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "XAU/USD",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = GoldPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GOLD SPOT",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NavySurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "Institutional Scalping Pro Terminal",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            ConnectionBadge(
                state = ticker.connectionState,
                onClick = onNavigateToSettings,
                modifier = Modifier.testTag("connection_badge")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Bid / Ask Price Board
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // BID PRICE
                    Column(modifier = Modifier.weight(1f)) {
                        Text("BID (SELL)", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.2f", ticker.bid),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = TextWhite
                        )
                    }

                    // SPREAD BADGE IN CENTER
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavyCardElevated)
                            .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SPREAD", color = GoldPrimary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                            Text(
                                text = "${String.format("%.1f", ticker.spread)} pips",
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // ASK PRICE
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("ASK (BUY)", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.2f", ticker.ask),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = GoldPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(NavyBorder))
                Spacer(modifier = Modifier.height(10.dp))

                // 24H STATS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("24h Change: ", color = TextMuted, fontSize = 12.sp)
                        Text(
                            text = "${if (isPriceUp) "+" else ""}${String.format("%.2f", ticker.change)} (${if (isPriceUp) "+" else ""}${String.format("%.2f", ticker.changePercent)}%)",
                            color = priceChangeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row {
                        Text("H: ", color = TextMuted, fontSize = 12.sp)
                        Text(String.format("%.2f", ticker.high24h), color = BuyGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("L: ", color = TextMuted, fontSize = 12.sp)
                        Text(String.format("%.2f", ticker.low24h), color = SellRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timeframe Selector
        TimeframeSelector(
            selected = timeframe,
            onSelect = { viewModel.selectTimeframe(it) },
            modifier = Modifier.testTag("timeframe_selector")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Candlestick Chart
        CandlestickChart(
            candles = candles,
            currentPrice = ticker.ask,
            modifier = Modifier.testTag("candlestick_chart")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Signal Flash Banner
        latestSignal?.let { signal ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSignals() }
                    .testTag("signal_banner_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SignalBadge(type = signal.type)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TF ${signal.timeframe.label}",
                                color = GoldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Conf: ${signal.confidenceScore}%",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Entry: $${String.format("%.2f", signal.entryPrice)}  •  SL: $${String.format("%.2f", signal.stopLoss)}  •  TP1: $${String.format("%.2f", signal.takeProfit1)}",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Detail",
                        tint = GoldPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Quantitative Indicator Metrics Grid
        Text(
            text = "INSTITUTIONAL QUANT METRICS (${timeframe.label})",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val rsiVal = analysis?.rsi14 ?: 50.0
            val rsiColor = when {
                rsiVal > 70.0 -> SellRed
                rsiVal < 30.0 -> BuyGreen
                else -> GoldPrimary
            }
            MetricCard(
                title = "RSI 14",
                value = String.format("%.1f", rsiVal),
                subtitle = if (rsiVal > 70) "Overbought" else if (rsiVal < 30) "Oversold" else "Momentum OK",
                valueColor = rsiColor,
                icon = Icons.Default.Speed,
                modifier = Modifier.weight(1f)
            )

            val emaStatus = if ((analysis?.ema9 ?: 0.0) > (analysis?.ema21 ?: 0.0)) "Bull Stack" else "Bear Stack"
            val emaColor = if (emaStatus == "Bull Stack") BuyGreen else SellRed
            MetricCard(
                title = "EMA 9 / 21",
                value = emaStatus,
                subtitle = "EMA9: ${String.format("%.1f", analysis?.ema9 ?: 0.0)}",
                valueColor = emaColor,
                icon = Icons.Default.TrendingUp,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "ATR 14 (VOLATILITY)",
                value = "$${String.format("%.2f", analysis?.atr14 ?: 1.50)}",
                subtitle = "Expected Pip Range",
                valueColor = TextWhite,
                icon = Icons.Default.ShowChart,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "INSTITUTIONAL VWAP",
                value = "$${String.format("%.2f", analysis?.vwap ?: ticker.ask)}",
                subtitle = if (ticker.ask >= (analysis?.vwap ?: ticker.ask)) "Price Above VWAP" else "Price Below VWAP",
                valueColor = GoldPrimary,
                icon = Icons.Default.QueryStats,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Primary Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onNavigateToSignals,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("view_signals_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("AI Signals", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onNavigateToCalculator,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("auto_lot_calc_button"),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Auto Lot Calc", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onNavigateToAnalyzer,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("open_analyzer_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NavySurfaceVariant,
                contentColor = TextWhite
            )
        ) {
            Icon(Icons.Default.QueryStats, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open AI Gold Analyzer & SMC Matrix", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
