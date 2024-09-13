package com.ch4019.jdaassist.ui.screen.splash

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ch4019.jdaassist.R
import com.ch4019.jdaassist.config.AppRoute
import com.ch4019.jdaassist.viewmodel.AppViewModel
import kotlinx.coroutines.delay

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SplashPage(
    navController: NavHostController,
    appViewModel: AppViewModel
) {
    val logo = ImageBitmap.imageResource(R.drawable.logo)
    val loginState by appViewModel.loginState.collectAsState()
    LaunchedEffect(Unit) {
        delay(500)
        if (loginState.isLogin) {
            navController.navigate(AppRoute.HOME) {
                popUpTo(AppRoute.SPLASH) {
                    inclusive = true
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }else {
            navController.navigate(AppRoute.LOGIN) {
                popUpTo(AppRoute.SPLASH) {
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
            Image(
                bitmap = logo,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
            )
            Text(
                text = "Jda Assist",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}