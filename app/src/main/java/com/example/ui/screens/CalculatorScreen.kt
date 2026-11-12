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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.viewmodel.TradingViewModel

@Composable
fun CalculatorScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val calcResult by viewModel.calculatorResult.collectAsState()
    val ticker by viewModel.ticker.collectAsState()

    var balanceText by remember { mutableStateOf(calcResult.balance.toInt().toString()) }
    var equityText by remember { mutableStateOf(calcResult.equity.toInt().toString()) }
    var riskPercentText by remember { mutableStateOf(calcResult.riskPercent.toString()) }
    var entryText by remember { mutableStateOf(String.format("%.2f", calcResult.entryPrice)) }
    var slText by remember { mutableStateOf(String.format("%.2f", calcResult.stopLossPrice)) }
    var contractText by remember { mutableStateOf(calcResult.contractSize.toInt().toString()) }
    var currency by remember { mutableStateOf("USD") }

    fun triggerCalc() {
        val b = balanceText.toDoubleOrNull() ?: 5000.0
        val eq = equityText.toDoubleOrNull() ?: b
        val r = riskPercentText.toDoubleOrNull() ?: 1.0
        val entry = entryText.toDoubleOrNull() ?: ticker.ask
        val sl = slText.toDoubleOrNull() ?: (entry - 2.50)
        val contract = contractText.toDoubleOrNull() ?: 100.0

        viewModel.calculateLot(
            balance = b,
            equity = eq,
            riskPercent = r,
            entryPrice = entry,
            stopLossPrice = sl,
            contractSize = contract,
            currency = currency
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("calculator_screen")
    ) {
        // Header
        Column {
            Text(
                text = "Auto Lot & Risk Calculator",
                style = MaterialTheme.typography.titleLarge,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Perhitungan Lot Presisi Sesuai Spesifikasi Broker",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Output Result Hero Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("lot_result_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "REKOMENDASI UKURAN LOT",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%.2f", calcResult.recommendedLot),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LOTS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(NavyBorder))
                Spacer(modifier = Modifier.height(12.dp))

                // Breakdown: Nominal Risk, Estimated Loss, Estimated Profit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Risiko Nominal", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = "$${String.format("%.2f", calcResult.riskAmount)}",
                            color = SellRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Jarak Stop Loss", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = "${String.format("%.2f", calcResult.pipDifference)} pts",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Estimasi Rugi (SL)", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = "-$${String.format("%.2f", calcResult.estimatedLoss)}",
                            color = SellRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Estimated Profit for TP1, TP2, TP3
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NavyCardElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfitCol(tp = "TP 1", amt = calcResult.estimatedProfitTp1)
                        ProfitCol(tp = "TP 2", amt = calcResult.estimatedProfitTp2)
                        ProfitCol(tp = "TP 3", amt = calcResult.estimatedProfitTp3)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Input Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("PARAMETER AKUN & RISIKO", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalcInput(
                        label = "Balance ($)",
                        value = balanceText,
                        onValueChange = { balanceText = it; triggerCalc() },
                        modifier = Modifier.weight(1f)
                    )
                    CalcInput(
                        label = "Equity ($)",
                        value = equityText,
                        onValueChange = { equityText = it; triggerCalc() },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Risk percentage quick selection pills
                Text("Persentase Risiko:", color = TextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("0.5", "1.0", "1.5", "2.0", "3.0").forEach { pct ->
                        val isSelected = riskPercentText == pct
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) GoldPrimary else NavyCardElevated)
                                .clickable {
                                    riskPercentText = pct
                                    triggerCalc()
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$pct%",
                                color = if (isSelected) Color.Black else TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("LEVEL HARGA ENTRY & STOP LOSS", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalcInput(
                        label = "Entry Price ($)",
                        value = entryText,
                        onValueChange = { entryText = it; triggerCalc() },
                        modifier = Modifier.weight(1f)
                    )
                    CalcInput(
                        label = "Stop Loss ($)",
                        value = slText,
                        onValueChange = { slText = it; triggerCalc() },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Quick buttons to grab current market price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            entryText = String.format("%.2f", ticker.ask)
                            slText = String.format("%.2f", ticker.ask - 2.50)
                            triggerCalc()
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyCardElevated, contentColor = BuyGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Use Ask (BUY): $${String.format("%.2f", ticker.ask)}", fontSize = 10.sp)
                    }

                    Button(
                        onClick = {
                            entryText = String.format("%.2f", ticker.bid)
                            slText = String.format("%.2f", ticker.bid + 2.50)
                            triggerCalc()
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyCardElevated, contentColor = SellRed),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Use Bid (SELL): $${String.format("%.2f", ticker.bid)}", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("SPESIFIKASI KONTRAK BROKER", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalcInput(
                        label = "Contract Size (oz/lot)",
                        value = contractText,
                        onValueChange = { contractText = it; triggerCalc() },
                        modifier = Modifier.weight(1f)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mata Uang Akun", color = TextMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("USD", "EUR", "IDR").forEach { cur ->
                                val isSelected = currency == cur
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) GoldPrimary else NavyCardElevated)
                                        .clickable {
                                            currency = cur
                                            triggerCalc()
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cur,
                                        color = if (isSelected) Color.Black else TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ProfitCol(tp: String, amt: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(tp, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "+$${String.format("%.2f", amt)}",
            color = BuyGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun CalcInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, color = TextMuted, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = NavyCardElevated,
                unfocusedContainerColor = NavyCardElevated,
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = NavyBorder,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
