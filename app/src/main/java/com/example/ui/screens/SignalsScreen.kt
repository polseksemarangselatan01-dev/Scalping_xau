package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SignalType
import com.example.ui.components.SignalBadge
import com.example.ui.components.TimeframeSelector
import com.example.ui.theme.BuyGreen
import com.example.ui.theme.BuyGreenContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyBlack
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.SellRed
import com.example.ui.theme.SellRedContainer
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WaitAmber
import com.example.ui.viewmodel.TradingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun SignalsScreen(
    viewModel: TradingViewModel,
    onNavigateToCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val timeframe by viewModel.selectedTimeframe.collectAsState()
    val signal by viewModel.latestSignal.collectAsState()
    val ticker by viewModel.ticker.collectAsState()

    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("signals_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AI Signal Generator",
                    style = MaterialTheme.typography.titleLarge,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Real-Time Scalping Confluence Engine",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = { viewModel.generateSignalManually() },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavySurfaceVariant)
                    .testTag("refresh_signal_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = GoldPrimary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timeframe selector
        TimeframeSelector(
            selected = timeframe,
            onSelect = { viewModel.selectTimeframe(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (signal == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(NavySurface, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Mengumpulkan data pasar aktual...", color = TextMuted, fontSize = 14.sp)
                    Text("Sinyal hanya dihasilkan jika pasar terkonfirmasi.", color = TextDim, fontSize = 11.sp)
                }
            }
            return@Column
        }

        val s = signal!!

        // Main Signal Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("main_signal_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Top row: Signal Badge & Confidence
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SignalBadge(type = s.type)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("XAU/USD (${s.timeframe.label})", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    // Confidence Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = NavyCardElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Conf: ${s.confidenceScore}%", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Price levels grid (Entry, SL, TP1, TP2, TP3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LevelBox(
                        title = "ENTRY",
                        price = s.entryPrice,
                        color = TextWhite,
                        bg = NavyCardElevated,
                        modifier = Modifier.weight(1f)
                    )
                    LevelBox(
                        title = "STOP LOSS",
                        price = s.stopLoss,
                        color = SellRed,
                        bg = SellRedContainer,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LevelBox(
                        title = "TP1 (1:1.5)",
                        price = s.takeProfit1,
                        color = BuyGreen,
                        bg = BuyGreenContainer,
                        modifier = Modifier.weight(1f)
                    )
                    LevelBox(
                        title = "TP2 (1:2.5)",
                        price = s.takeProfit2,
                        color = BuyGreen,
                        bg = BuyGreenContainer,
                        modifier = Modifier.weight(1f)
                    )
                    LevelBox(
                        title = "TP3 (1:4.0)",
                        price = s.takeProfit3,
                        color = GoldPrimary,
                        bg = Color(0xFF2A2004),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Risk-Reward & Validity Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Risk Reward Ratio: 1:${s.riskRewardRatio}", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    val remainingMinutes = max(0, ((s.validUntilTimestamp - System.currentTimeMillis()) / 60000L).toInt())
                    Text("Valid s/d: ${timeFormat.format(Date(s.validUntilTimestamp))} (~${remainingMinutes}m)", color = TextMuted, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(NavyBorder))
                Spacer(modifier = Modifier.height(12.dp))

                // Invalidation Guard Warning
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2E1700),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WaitAmber.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = WaitAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Kondisi Pembatalan Sinyal:", color = WaitAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(s.invalidationCondition, color = TextWhite, fontSize = 12.sp, lineHeight = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Technical Rationale list
                Text("Alasan Teknikal & Konfluensi:", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                s.technicalReasons.forEach { reason ->
                    Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(5.dp)
                                .clip(RoundedCornerShape(2.5.dp))
                                .background(GoldPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(reason, color = TextWhite, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons Row: Calculate Lot, Save to Journal, Copy
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.prefillCalculatorFromSignal(s)
                    onNavigateToCalculator()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("apply_to_calc_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Auto Lot Calc", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    viewModel.saveTradeToJournal(s, lotSize = 0.1, notes = "Logged from AI Signal Generator (${s.timeframe.label})")
                    Toast.makeText(context, "Sinyal disimpan ke Trading Journal!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("save_journal_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavySurfaceVariant, contentColor = TextWhite)
            ) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log Journal", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Copy Signal Parameters
        OutlinedButton(
            onClick = {
                val textToCopy = """
                    ⚡ AI GOLD SCALPING PRO
                    Pair: ${s.pair} (${s.timeframe.label})
                    Action: ${s.type.name}
                    Entry: $${String.format("%.2f", s.entryPrice)}
                    Stop Loss: $${String.format("%.2f", s.stopLoss)}
                    TP1: $${String.format("%.2f", s.takeProfit1)}
                    TP2: $${String.format("%.2f", s.takeProfit2)}
                    TP3: $${String.format("%.2f", s.takeProfit3)}
                    Confidence: ${s.confidenceScore}%
                    RRR: 1:${s.riskRewardRatio}
                    Batal: ${s.invalidationCondition}
                """.trimIndent()
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("XAU_USD_SIGNAL", textToCopy)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Parameter sinyal disalin ke clipboard!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("copy_signal_button"),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Copy Sinyal untuk Broker", fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LevelBox(
    title: String,
    price: Double,
    color: Color,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = color.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = String.format("%.2f", price),
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
