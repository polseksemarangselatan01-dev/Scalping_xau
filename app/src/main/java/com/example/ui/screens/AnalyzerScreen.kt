package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.data.model.SignalType
import com.example.data.model.Timeframe
import com.example.ui.components.ConfluenceMatrixBar
import com.example.ui.components.SignalBadge
import com.example.ui.components.TimeframeSelector
import com.example.ui.theme.BlueEma
import com.example.ui.theme.BuyGreen
import com.example.ui.theme.CyanRsi
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyBlack
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.OrangeVwap
import com.example.ui.theme.PinkBollinger
import com.example.ui.theme.PurpleMacd
import com.example.ui.theme.SellRed
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WaitAmber
import com.example.ui.viewmodel.TradingViewModel

@Composable
fun AnalyzerScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val timeframe by viewModel.selectedTimeframe.collectAsState()
    val analysis by viewModel.currentAnalysis.collectAsState()
    val mtfSummary by viewModel.mtfSummary.collectAsState()
    val aiReport by viewModel.aiReport.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("analyzer_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AI Quantitative Analyzer",
                    style = MaterialTheme.typography.titleLarge,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SMC, Order Flow & Multi-Timeframe Matrix",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            analysis?.summaryBias?.let {
                SignalBadge(type = it)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Multi-Timeframe Matrix Confluence Card (Requirement 6)
        mtfSummary?.let { mtf ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("MULTI-TIMEFRAME CONFLUENCE MATRIX", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ConfluenceMatrixBar(confluenceScore = mtf.confluenceScore, bias = mtf.overallBias)

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Timeframe Cards: M1 (Entry), M5 (Confirmation), M15 (Trend), H1 (Macro)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MtfMiniPill(tf = "M1", role = "Entry", bias = mtf.m1.summaryBias, modifier = Modifier.weight(1f))
                        MtfMiniPill(tf = "M5", role = "Confirm", bias = mtf.m5.summaryBias, modifier = Modifier.weight(1f))
                        MtfMiniPill(tf = "M15", role = "Trend", bias = mtf.m15.summaryBias, modifier = Modifier.weight(1f))
                        MtfMiniPill(tf = "H1", role = "Macro", bias = mtf.h1.summaryBias, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Verdict Text
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = NavyCardElevated
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = mtf.recommendation,
                                color = TextWhite,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timeframe selector for detailed inspection
        Text("DETAILED INDICATOR BREAKDOWN", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        TimeframeSelector(
            selected = timeframe,
            onSelect = { viewModel.selectTimeframe(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Technical Indicators Table Card
        analysis?.let { data ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Technical Indicators (${timeframe.label})",
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    IndicatorRow("EMA 9 (Fast Scalp)", String.format("%.2f", data.ema9), BlueEma)
                    IndicatorRow("EMA 21 (Short Trend)", String.format("%.2f", data.ema21), GoldPrimary)
                    IndicatorRow("EMA 50 (Dynamic S/R)", String.format("%.2f", data.ema50), TextWhite)
                    IndicatorRow("EMA 200 (Macro Baseline)", String.format("%.2f", data.ema200), TextMuted)

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(vertical = 6.dp).background(NavyBorder))

                    IndicatorRow("RSI 14 Momentum", "${String.format("%.1f", data.rsi14)} (${if (data.rsi14 > 70) "Overbought" else if (data.rsi14 < 30) "Oversold" else "Normal"})", CyanRsi)
                    IndicatorRow("MACD Line / Signal", "${String.format("%.2f", data.macdLine)} / ${String.format("%.2f", data.macdSignal)}", PurpleMacd)
                    IndicatorRow("MACD Histogram", String.format("%.2f", data.macdHistogram), if (data.macdHistogram >= 0) BuyGreen else SellRed)

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(vertical = 6.dp).background(NavyBorder))

                    IndicatorRow("Bollinger Upper (20,2)", String.format("%.2f", data.bbUpper), PinkBollinger)
                    IndicatorRow("Bollinger Middle (SMA20)", String.format("%.2f", data.bbMiddle), TextMuted)
                    IndicatorRow("Bollinger Lower (20,2)", String.format("%.2f", data.bbLower), PinkBollinger)

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(vertical = 6.dp).background(NavyBorder))

                    IndicatorRow("ATR 14 (Pip Volatility)", "$${String.format("%.2f", data.atr14)}", OrangeVwap)
                    IndicatorRow("Institutional VWAP", "$${String.format("%.2f", data.vwap)}", GoldPrimary)
                    IndicatorRow("Dynamic Support S1 / S2", "$${String.format("%.2f", data.support1)} / $${String.format("%.2f", data.support2)}", BuyGreen)
                    IndicatorRow("Dynamic Resist R1 / R2", "$${String.format("%.2f", data.resistance1)} / $${String.format("%.2f", data.resistance2)}", SellRed)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Smart Money Concepts & Price Action Card (Requirement 4)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2. Smart Money Concepts (SMC) & Price Action",
                            color = GoldPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    SmcRow(
                        title = "Market Structure (BOS / CHoCH)",
                        status = data.smcStructure,
                        color = if (data.smcStructure.contains("Bullish")) BuyGreen else if (data.smcStructure.contains("Bearish")) SellRed else WaitAmber
                    )

                    SmcRow(
                        title = "Liquidity Sweep",
                        status = data.liquiditySweep,
                        color = if (data.liquiditySweep.contains("Swept")) GoldPrimary else TextMuted
                    )

                    SmcRow(
                        title = "Order Block (Supply / Demand)",
                        status = data.orderBlockStatus,
                        color = if (data.orderBlockStatus.contains("Demand")) BuyGreen else if (data.orderBlockStatus.contains("Supply")) SellRed else TextMuted
                    )

                    SmcRow(
                        title = "Price Action Candlestick",
                        status = if (data.currentPrice >= data.vwap) "Bullish Absorption above VWAP" else "Bearish Rejection below VWAP",
                        color = if (data.currentPrice >= data.vwap) BuyGreen else SellRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Institutional Deep Intelligence (Gemini AI / Hybrid Engine)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("3. AI DEEP INTELLIGENCE", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = { viewModel.requestAiDeepAnalysis() },
                        enabled = !isAiLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("run_ai_analysis_button")
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                        } else {
                            Text("Run AI Analysis", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                aiReport?.let { report ->
                    Text("Thesis Pasar:", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(report.summaryThesis, color = TextWhite, fontSize = 13.sp, lineHeight = 18.sp)

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Arus Order Smart Money:", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(report.institutionalOrderFlow, color = TextWhite, fontSize = 13.sp, lineHeight = 18.sp)

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Faktor Risiko:", color = SellRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    report.keyRiskFactors.forEach { factor ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = SellRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(factor, color = TextMuted, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Checklist Eksekusi:", color = BuyGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    report.executionChecklist.forEach { item ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BuyGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(item, color = TextWhite, fontSize = 12.sp)
                        }
                    }
                } ?: run {
                    Text(
                        text = "Klik 'Run AI Analysis' untuk mengaktifkan pemrosesan Gemini AI / Smart Quantitative Synthesis berbasis data aktual.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun IndicatorRow(name: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, color = TextMuted, fontSize = 12.sp)
        Text(value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun SmcRow(title: String, status: String, color: Color) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(title, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(color))
            Spacer(modifier = Modifier.width(6.dp))
            Text(status, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MtfMiniPill(tf: String, role: String, bias: SignalType, modifier: Modifier = Modifier) {
    val (bg, color) = when (bias) {
        SignalType.STRONG_BUY, SignalType.BUY -> Pair(Color(0xFF00381B), BuyGreen)
        SignalType.STRONG_SELL, SignalType.SELL -> Pair(Color(0xFF381014), SellRed)
        else -> Pair(Color(0xFF2B2005), WaitAmber)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(tf, color = TextWhite, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            Text(role, color = TextMuted, fontSize = 9.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(bias.name.replace("_", " "), color = color, fontWeight = FontWeight.Bold, fontSize = 9.sp)
        }
    }
}
