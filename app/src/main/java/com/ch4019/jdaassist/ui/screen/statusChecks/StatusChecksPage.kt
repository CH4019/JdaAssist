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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun StatusChecksPage(
    navController: NavHostController,
    appViewModel: AppViewModel
) {
//    val logo = ImageBitmap.imageResource(R.drawable.logo)
    val loginState by appViewModel.loginState.collectAsState()
    LaunchedEffect(Dispatchers.IO) {
        delay(200)
        if (loginState.isLogin) {
            navController.navigate(AppRoute.HOME) {
                popUpTo(AppRoute.STATUS_CHECKS) {
                    inclusive = true
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }else {
            navController.navigate(AppRoute.LOGIN) {
                popUpTo(AppRoute.STATUS_CHECKS) {
                    inclusive = true
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 8.dp),
                strokeCap = StrokeCap.Round
            )
            Text(
                text = "正在检查登录状态...",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}