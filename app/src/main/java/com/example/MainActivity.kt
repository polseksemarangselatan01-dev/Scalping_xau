package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AnalyzerScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SignalsScreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyBlack
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.TradingViewModel

enum class AppScreen(val label: String, val icon: ImageVector) {
    DASHBOARD("Terminal", Icons.Default.ShowChart),
    ANALYZER("Analyzer", Icons.Default.QueryStats),
    SIGNALS("Signals", Icons.Default.ElectricBolt),
    CALCULATOR("Lot Calc", Icons.Default.Calculate),
    JOURNAL("Journal", Icons.Default.MenuBook),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: TradingViewModel = viewModel()
                var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }

                // Handle system back button for secondary screens
                if (currentScreen != AppScreen.DASHBOARD) {
                    BackHandler {
                        currentScreen = AppScreen.DASHBOARD
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = NavyBlack,
                    bottomBar = {
                        BottomNavBar(
                            currentScreen = currentScreen,
                            onSelectScreen = { currentScreen = it }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                AppScreen.DASHBOARD -> DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToAnalyzer = { currentScreen = AppScreen.ANALYZER },
                                    onNavigateToSignals = { currentScreen = AppScreen.SIGNALS },
                                    onNavigateToCalculator = { currentScreen = AppScreen.CALCULATOR },
                                    onNavigateToSettings = { currentScreen = AppScreen.SETTINGS }
                                )
                                AppScreen.ANALYZER -> AnalyzerScreen(
                                    viewModel = viewModel
                                )
                                AppScreen.SIGNALS -> SignalsScreen(
                                    viewModel = viewModel,
                                    onNavigateToCalculator = { currentScreen = AppScreen.CALCULATOR }
                                )
                                AppScreen.CALCULATOR -> CalculatorScreen(
                                    viewModel = viewModel
                                )
                                AppScreen.JOURNAL -> JournalScreen(
                                    viewModel = viewModel
                                )
                                AppScreen.SETTINGS -> SettingsScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    currentScreen: AppScreen,
    onSelectScreen: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = NavySurface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = NavyBorder)
            .testTag("bottom_nav_bar")
    ) {
        AppScreen.values().forEach { screen ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectScreen(screen) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = screen.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    unselectedIconColor = TextMuted,
                    selectedTextColor = GoldPrimary,
                    unselectedTextColor = TextMuted,
                    indicatorColor = GoldPrimary
                ),
                modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
            )
        }
    }
}
