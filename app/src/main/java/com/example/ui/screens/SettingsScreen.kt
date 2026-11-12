package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
import com.example.ui.theme.WaitAmber
import com.example.ui.viewmodel.TradingViewModel

@Composable
fun SettingsScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    var marketApiUrl by remember(settings.marketApiUrl) { mutableStateOf(settings.marketApiUrl) }
    var marketApiKey by remember(settings.marketApiKey) { mutableStateOf(settings.marketApiKey) }
    var webSocketUrl by remember(settings.webSocketUrl) { mutableStateOf(settings.webSocketUrl) }
    var geminiApiKey by remember(settings.geminiApiKey) { mutableStateOf(settings.geminiApiKey) }
    var geminiEndpoint by remember(settings.geminiEndpoint) { mutableStateOf(settings.geminiEndpoint) }

    var hideGeminiKey by remember { mutableStateOf(true) }
    var hideMarketKey by remember { mutableStateOf(true) }

    // Permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Izin notifikasi diberikan!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        // Header
        Column {
            Text(
                text = "Pengaturan & Konfigurasi API",
                style = MaterialTheme.typography.titleLarge,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Kelola Endpoint Live Data, WebSocket, Gemini AI & Notifikasi",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mode Switching Card (DEMO MODE vs LIVE DATA)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("mode_toggle_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, if (settings.isDemoMode) Color(0xFF4A90E2) else BuyGreen)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (settings.isDemoMode) "MODE DEMO (PENGUJIAN)" else "MODE LIVE MARKET",
                            color = if (settings.isDemoMode) Color(0xFF64B5F6) else BuyGreen,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (settings.isDemoMode)
                                "Menggunakan simulasi pergerakan harga emas berpresisi tinggi untuk pengujian strategi tanpa API key."
                            else
                                "Mengambil harga aktual secara real-time dari REST API / WebSocket yang dikonfigurasi.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Switch(
                        checked = settings.isDemoMode,
                        onCheckedChange = { viewModel.toggleDemoMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF64B5F6),
                            checkedTrackColor = Color(0xFF132847),
                            uncheckedThumbColor = BuyGreen,
                            uncheckedTrackColor = Color(0xFF00381B)
                        ),
                        modifier = Modifier.testTag("demo_mode_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Market Data REST API Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Api, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("1. Market Data REST API", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = marketApiUrl,
                    onValueChange = { marketApiUrl = it },
                    label = { Text("Market Data REST URL") },
                    placeholder = { Text("Contoh: https://www.goldapi.io/api/XAU/USD") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyBorder,
                        focusedContainerColor = NavyCardElevated,
                        unfocusedContainerColor = NavyCardElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = marketApiKey,
                    onValueChange = { marketApiKey = it },
                    label = { Text("Market Data API Key (Opsional)") },
                    singleLine = true,
                    visualTransformation = if (hideMarketKey) PasswordVisualTransformation() else VisualTransformation.None,
                    trailingIcon = {
                        Text(
                            text = if (hideMarketKey) "Show" else "Hide",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyBorder,
                        focusedContainerColor = NavyCardElevated,
                        unfocusedContainerColor = NavyCardElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. WebSocket Streaming URL Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wifi, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("2. WebSocket Streaming URL", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = webSocketUrl,
                    onValueChange = { webSocketUrl = it },
                    label = { Text("WebSocket Streaming URL") },
                    placeholder = { Text("Contoh: wss://stream.binance.com:9443/ws/paxgusdt@ticker") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyBorder,
                        focusedContainerColor = NavyCardElevated,
                        unfocusedContainerColor = NavyCardElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Streaming langsung sub-second tick untuk scalping presisi.",
                    color = TextDim,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Gemini AI / Backend API Card
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
                    Text("3. Google Gemini AI & Secure Backend", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = geminiApiKey,
                    onValueChange = { geminiApiKey = it },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("Masukkan Gemini API Key") },
                    singleLine = true,
                    visualTransformation = if (hideGeminiKey) PasswordVisualTransformation() else VisualTransformation.None,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyBorder,
                        focusedContainerColor = NavyCardElevated,
                        unfocusedContainerColor = NavyCardElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = geminiEndpoint,
                    onValueChange = { geminiEndpoint = it },
                    label = { Text("Secure Backend Proxy URL (Opsional)") },
                    placeholder = { Text("Contoh: https://api.yourbackend.com/ai-gold") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyBorder,
                        focusedContainerColor = NavyCardElevated,
                        unfocusedContainerColor = NavyCardElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Key disimpan aman di DataStore lokal perangkat pengguna dan tidak dikompromikan.",
                    color = TextDim,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save All API Settings Button
        Button(
            onClick = {
                viewModel.saveApiSettings(
                    marketUrl = marketApiUrl,
                    marketKey = marketApiKey,
                    wsUrl = webSocketUrl,
                    geminiKey = geminiApiKey,
                    geminiEndpoint = geminiEndpoint
                )
                Toast.makeText(context, "Konfigurasi API berhasil disimpan!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_api_settings_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simpan Konfigurasi API", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Market Alerts & Notification Settings (Requirement 8)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("4. Market Alerts & Notifikasi", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Button(
                            onClick = { notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Izinkan Notif", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                NotifToggleRow("Sinyal BUY & SELL Baru", settings.notifyBuySell) {
                    viewModel.updateNotificationToggles(it, settings.notifyTrendChange, settings.notifyBreakout, settings.notifyHighSpread, settings.notifyEconomicNews, settings.isSoundEnabled, settings.isVibrationEnabled)
                }
                NotifToggleRow("Perubahan Tren Multi-Timeframe", settings.notifyTrendChange) {
                    viewModel.updateNotificationToggles(settings.notifyBuySell, it, settings.notifyBreakout, settings.notifyHighSpread, settings.notifyEconomicNews, settings.isSoundEnabled, settings.isVibrationEnabled)
                }
                NotifToggleRow("Breakout & Lonjakan Volatilitas (ATR)", settings.notifyBreakout) {
                    viewModel.updateNotificationToggles(settings.notifyBuySell, settings.notifyTrendChange, it, settings.notifyHighSpread, settings.notifyEconomicNews, settings.isSoundEnabled, settings.isVibrationEnabled)
                }
                NotifToggleRow("Peringatan Spread Terlalu Tinggi (>3.5 pips)", settings.notifyHighSpread) {
                    viewModel.updateNotificationToggles(settings.notifyBuySell, settings.notifyTrendChange, settings.notifyBreakout, it, settings.notifyEconomicNews, settings.isSoundEnabled, settings.isVibrationEnabled)
                }
                NotifToggleRow("Berita Ekonomi Berdampak Tinggi", settings.notifyEconomicNews) {
                    viewModel.updateNotificationToggles(settings.notifyBuySell, settings.notifyTrendChange, settings.notifyBreakout, settings.notifyHighSpread, it, settings.isSoundEnabled, settings.isVibrationEnabled)
                }
                NotifToggleRow("Getar Saat Alert Aktif", settings.isVibrationEnabled) {
                    viewModel.updateNotificationToggles(settings.notifyBuySell, settings.notifyTrendChange, settings.notifyBreakout, settings.notifyHighSpread, settings.notifyEconomicNews, settings.isSoundEnabled, it)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Documentation & Disclaimer Card
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = NavyCardElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Disclaimer & Ketentuan:", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Aplikasi AI Gold Scalping Pro bersifat analisis dan pemberi sinyal edukatif, bukan eksekutor transaksi otomatis (tidak terkoneksi eksekusi dana riil langsung ke broker tanpa persetujuan manual). Selalu utamakan manajemen risiko disiplin.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun NotifToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = TextWhite, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = GoldPrimary,
                checkedTrackColor = Color(0xFF2A2004),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = NavyCardElevated
            )
        )
    }
}
