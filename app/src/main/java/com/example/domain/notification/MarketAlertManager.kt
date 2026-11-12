package com.example.domain.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.datastore.AppSettings
import com.example.data.model.TradingSignal

class MarketAlertManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_SIGNALS_ID = "gold_signals_channel"
        const val CHANNEL_MARKET_ID = "gold_market_alerts_channel"
        private const val SIGNAL_NOTIF_ID = 1001
        private const val MARKET_NOTIF_ID = 1002
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val signalsChannel = NotificationChannel(
                CHANNEL_SIGNALS_ID,
                "AI Scalping Signals (BUY/SELL)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority notification for new XAU/USD scalping signals"
                enableVibration(true)
            }

            val marketChannel = NotificationChannel(
                CHANNEL_MARKET_ID,
                "Market Volatility & Spread Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts for high spread, trend reversals, and breakout surges"
            }

            notificationManager.createNotificationChannel(signalsChannel)
            notificationManager.createNotificationChannel(marketChannel)
        }
    }

    fun notifySignal(signal: TradingSignal, settings: AppSettings) {
        if (!settings.notifyBuySell) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⚡ AI Signal: ${signal.type.name} (${signal.timeframe.label})"
        val body = "Entry: $${String.format("%.2f", signal.entryPrice)} | SL: $${String.format("%.2f", signal.stopLoss)} | TP1: $${String.format("%.2f", signal.takeProfit1)} | Conf: ${signal.confidenceScore}%"

        val builder = NotificationCompat.Builder(context, CHANNEL_SIGNALS_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$body\n${signal.invalidationCondition}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (!settings.isVibrationEnabled) {
            builder.setVibrate(longArrayOf(0L))
        }

        notificationManager.notify(SIGNAL_NOTIF_ID, builder.build())
    }

    fun notifyMarketAlert(type: String, message: String, settings: AppSettings) {
        val isAllowed = when (type) {
            "TREND" -> settings.notifyTrendChange
            "BREAKOUT" -> settings.notifyBreakout
            "SPREAD" -> settings.notifyHighSpread
            "NEWS" -> settings.notifyEconomicNews
            else -> true
        }
        if (!isAllowed) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_MARKET_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⚠️ Market Alert: $type")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(MARKET_NOTIF_ID, builder.build())
    }
}
