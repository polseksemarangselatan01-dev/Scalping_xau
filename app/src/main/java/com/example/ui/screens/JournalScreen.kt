package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.TradeJournalEntity
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
import com.example.ui.theme.WaitAmberContainer
import com.example.ui.viewmodel.TradingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JournalScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val trades by viewModel.journalTrades.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }
    var tradeToUpdate by remember { mutableStateOf<TradeJournalEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredTrades = when (selectedFilter) {
        "OPEN" -> trades.filter { it.status == "OPEN" }
        "WIN" -> trades.filter { it.status.startsWith("WIN") }
        "LOSS" -> trades.filter { it.status == "LOSS" }
        else -> trades
    }

    val totalTrades = trades.size
    val winTrades = trades.count { it.status.startsWith("WIN") }
    val lossTrades = trades.count { it.status == "LOSS" }
    val closedTrades = winTrades + lossTrades
    val winRate = if (closedTrades > 0) ((winTrades.toDouble() / closedTrades) * 100).toInt() else 0
    val totalPnl = trades.sumOf { it.pnl }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBlack)
            .testTag("journal_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Trading Journal",
                            style = MaterialTheme.typography.titleLarge,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Evaluasi Performa & Catatan Scalping XAU/USD",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    if (trades.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearJournal() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Dashboard Hero Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(title = "TOTAL TRADES", value = "$totalTrades", color = TextWhite)
                        StatItem(title = "WIN RATE", value = "$winRate%", color = if (winRate >= 60) BuyGreen else GoldPrimary)
                        StatItem(
                            title = "TOTAL PnL",
                            value = "${if (totalPnl >= 0) "+" else ""}$${String.format("%.2f", totalPnl)}",
                            color = if (totalPnl >= 0) BuyGreen else SellRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Filter Tabs: ALL, OPEN, WIN, LOSS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL", "OPEN", "WIN", "LOSS").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GoldPrimary else NavyCardElevated)
                                .clickable { selectedFilter = filter }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) Color.Black else TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            if (filteredTrades.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(NavySurface, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.History, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Belum ada riwayat transaksi journal.", color = TextMuted, fontSize = 13.sp)
                            Text("Simpan sinyal dari tab AI Signals atau tambah manual.", color = TextDim, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                items(filteredTrades, key = { it.id }) { trade ->
                    TradeCard(
                        trade = trade,
                        dateFormat = dateFormat,
                        onUpdateClick = { tradeToUpdate = trade },
                        onDeleteClick = { viewModel.deleteTrade(trade) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // FAB to add custom trade
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = GoldPrimary,
            contentColor = Color.Black,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_trade_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Catat Transaksi")
        }

        // Update trade status dialog
        tradeToUpdate?.let { trade ->
            UpdateTradeDialog(
                trade = trade,
                onDismiss = { tradeToUpdate = null },
                onConfirm = { status, exitPrice ->
                    viewModel.updateTradeStatus(trade, status, exitPrice)
                    tradeToUpdate = null
                    Toast.makeText(context, "Status transaksi diperbarui!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Add manual trade dialog
        if (showAddDialog) {
            AddTradeDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { signalType, entry, sl, tp, lot, notes ->
                    viewModel.saveTradeToJournal(
                        signal = com.example.data.model.TradingSignal(
                            pair = "XAU/USD",
                            type = com.example.data.model.SignalType.valueOf(signalType),
                            timeframe = com.example.data.model.Timeframe.M5,
                            entryPrice = entry,
                            stopLoss = sl,
                            takeProfit1 = tp,
                            takeProfit2 = tp + 2.0,
                            takeProfit3 = tp + 5.0,
                            riskRewardRatio = 2.0,
                            confidenceScore = 85,
                            technicalReasons = listOf("Manual entry log"),
                            validUntilTimestamp = System.currentTimeMillis() + 3600000L,
                            invalidationCondition = "Manual trade"
                        ),
                        lotSize = lot,
                        notes = notes
                    )
                    showAddDialog = false
                    Toast.makeText(context, "Transaksi berhasil dicatat ke Journal!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun StatItem(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, color = color, fontSize = 17.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun TradeCard(
    trade: TradeJournalEntity,
    dateFormat: SimpleDateFormat,
    onUpdateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isBuy = trade.signalType.contains("BUY", ignoreCase = true)
    val statusColor = when {
        trade.status.startsWith("WIN") -> BuyGreen
        trade.status == "LOSS" -> SellRed
        trade.status == "OPEN" -> GoldPrimary
        else -> TextMuted
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isBuy) BuyGreenContainer else SellRedContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(trade.signalType, color = if (isBuy) BuyGreen else SellRed, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${trade.pair} (${trade.timeframe})", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { onUpdateClick() }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(trade.status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details: Entry, SL, TP, Lots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Entry: $${String.format("%.2f", trade.entryPrice)}", color = TextWhite, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("SL: $${String.format("%.2f", trade.stopLoss)}", color = SellRed, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("TP: $${String.format("%.2f", trade.takeProfit)}", color = BuyGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("Lot: ${trade.lotSize}", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            if (trade.status != "OPEN") {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    trade.exitPrice?.let {
                        Text("Exit: $${String.format("%.2f", it)}", color = TextMuted, fontSize = 11.sp)
                    }
                    Text(
                        text = "PnL: ${if (trade.pnl >= 0) "+" else ""}$${String.format("%.2f", trade.pnl)}",
                        color = if (trade.pnl >= 0) BuyGreen else SellRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (trade.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Catatan: ${trade.notes}", color = TextMuted, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(dateFormat.format(Date(trade.timestamp)), color = TextDim, fontSize = 10.sp)
                Row {
                    TextButton(onClick = onUpdateClick) {
                        Text("Update", color = GoldPrimary, fontSize = 11.sp)
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = TextDim, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateTradeDialog(
    trade: TradeJournalEntity,
    onDismiss: () -> Unit,
    onConfirm: (status: String, exitPrice: Double) -> Unit
) {
    var exitPriceText by remember { mutableStateOf(trade.takeProfit.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Status Transaksi", color = GoldPrimary) },
        text = {
            Column {
                Text("Pair: ${trade.pair} | Entry: $${trade.entryPrice}", color = TextWhite, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = exitPriceText,
                    onValueChange = { exitPriceText = it },
                    label = { Text("Harga Exit ($)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Pilih Hasil:", color = TextMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { onConfirm("WIN_TP1", exitPriceText.toDoubleOrNull() ?: trade.takeProfit) },
                        colors = ButtonDefaults.buttonColors(containerColor = BuyGreen, contentColor = Color.Black),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Win TP1", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onConfirm("WIN_TP2", exitPriceText.toDoubleOrNull() ?: (trade.entryPrice + 5.0)) },
                        colors = ButtonDefaults.buttonColors(containerColor = BuyGreen, contentColor = Color.Black),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Win TP2", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onConfirm("LOSS", exitPriceText.toDoubleOrNull() ?: trade.stopLoss) },
                        colors = ButtonDefaults.buttonColors(containerColor = SellRed, contentColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hit SL", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = TextMuted) }
        },
        containerColor = NavySurface
    )
}

@Composable
private fun AddTradeDialog(
    onDismiss: () -> Unit,
    onConfirm: (signalType: String, entry: Double, sl: Double, tp: Double, lot: Double, notes: String) -> Unit
) {
    var isBuy by remember { mutableStateOf(true) }
    var entryText by remember { mutableStateOf("2684.00") }
    var slText by remember { mutableStateOf("2681.50") }
    var tpText by remember { mutableStateOf("2689.00") }
    var lotText by remember { mutableStateOf("0.10") }
    var notesText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Transaksi Manual", color = GoldPrimary) },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { isBuy = true },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isBuy) BuyGreen else NavyCardElevated, contentColor = if (isBuy) Color.Black else TextWhite),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("BUY", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { isBuy = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if (!isBuy) SellRed else NavyCardElevated, contentColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SELL", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = entryText,
                        onValueChange = { entryText = it },
                        label = { Text("Entry ($)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lotText,
                        onValueChange = { lotText = it },
                        label = { Text("Lot") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = slText,
                        onValueChange = { slText = it },
                        label = { Text("SL ($)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = tpText,
                        onValueChange = { tpText = it },
                        label = { Text("TP ($)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Catatan Trading") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        if (isBuy) "BUY" else "SELL",
                        entryText.toDoubleOrNull() ?: 2684.0,
                        slText.toDoubleOrNull() ?: 2681.5,
                        tpText.toDoubleOrNull() ?: 2689.0,
                        lotText.toDoubleOrNull() ?: 0.1,
                        notesText
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = TextMuted) }
        },
        containerColor = NavySurface
    )
}
