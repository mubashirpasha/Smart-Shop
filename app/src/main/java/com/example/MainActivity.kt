package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.data.local.preferences.ThemeMode
import com.example.ui.navigation.ShopFlowApp
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.screens.dashboard.DashboardViewModelFactory
import com.example.ui.theme.ShopFlowTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ShopFlowApplication
        val repository = app.repository

        val dashboardViewModel: DashboardViewModel by viewModels {
            DashboardViewModelFactory(repository)
        }

        setContent {
            val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
            val isDark = when (uiState.settings.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            var showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(700)
                showSplash = false
            }

            ShopFlowTheme(darkTheme = isDark) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    ShopFlowApp(
                        navController = navController,
                        dashboardViewModel = dashboardViewModel,
                        repository = repository
                    )

                    AnimatedVisibility(
                        visible = showSplash,
                        enter = fadeIn(tween(100)),
                        exit = fadeOut(tween(350))
                    ) {
                        AppSplashScreen(isUrdu = uiState.settings.language == "ur")
                    }
                }
            }
        }
    }
}

@Composable
private fun AppSplashScreen(isUrdu: Boolean) {
    val bgColor = Color(0xFFFAF5EE)
    val primaryColor = Color(0xFF54382B)
    val caramelColor = Color(0xFFC69A72)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        ambientColor = primaryColor.copy(alpha = 0.2f),
                        spotColor = primaryColor.copy(alpha = 0.3f)
                    )
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, caramelColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.smart_shop_app_icon),
                    contentDescription = "ShopFlow Logo",
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isUrdu) "شاپ فلو • کاروبار" else "ShopFlow",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = primaryColor,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isUrdu) "سمارٹ پی او ایس اور انوینٹری منیجر" else "Point of Sale & Inventory",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = caramelColor,
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = caramelColor,
                strokeWidth = 2.5.dp
            )
        }
    }
}
