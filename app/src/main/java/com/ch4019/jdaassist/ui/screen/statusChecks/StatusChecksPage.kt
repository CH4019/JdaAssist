package com.ch4019.jdaassist.ui.screen.statusChecks

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ch4019.jdaassist.config.AppRoute
import com.ch4019.jdaassist.viewmodel.AppViewModel
import kotlinx.coroutines.delay

private const val STATUS_CHECK_DELAY_MS = 200L
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun StatusChecksPage(
    navController: NavHostController,
    appViewModel: AppViewModel
) {
    // 订阅登录状态
    val loginState by appViewModel.loginState.collectAsState()
    // 当loginState.isLogin 发生变化时，触发导航
    LaunchedEffect(loginState.isLogin) {
        // 给 UI 一点“加载”过渡时间
        delay(STATUS_CHECK_DELAY_MS)
        navigateByLoginState(navController, loginState.isLogin)
    }
    // UI 层
    StatusLoadingUI()
}

/**
 * 根据登录状态执行导航
 */
private fun navigateByLoginState(navController: NavHostController, isLoggedIn: Boolean) {
    val targetRoute = if (isLoggedIn) AppRoute.HOME else AppRoute.LOGIN
    navController.navigate(targetRoute) {
        popUpTo(AppRoute.STATUS_CHECKS) {
            inclusive = true
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun StatusLoadingUI() {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp),
                strokeCap = StrokeCap.Round
            )
            Text(
                text = "正在检查登录状态…",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}