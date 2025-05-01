package com.ch4019.jdaassist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ch4019.jdaassist.config.AppRoute
import com.ch4019.jdaassist.model.MaskAnimModel
import com.ch4019.jdaassist.ui.components.Konfetti
import com.ch4019.jdaassist.ui.components.MaskBox
import com.ch4019.jdaassist.ui.components.Welcome
import com.ch4019.jdaassist.ui.components.rememberKonfettiState
import com.ch4019.jdaassist.ui.screen.about.AboutPage
import com.ch4019.jdaassist.ui.screen.login.LoginPage
import com.ch4019.jdaassist.ui.screen.main.ContentUiPage
import com.ch4019.jdaassist.ui.screen.statusChecks.StatusChecksPage
import com.ch4019.jdaassist.ui.theme.JdaAssistTheme
import com.ch4019.jdaassist.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Install the splash screen
        installSplashScreen()
        //设置全屏
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()
            val appViewModel: AppViewModel = viewModel()
            val uiPrefs by appViewModel.uiPrefs.collectAsState()
            val konfettiState = rememberKonfettiState(uiPrefs.welcomeDone)

            JdaAssistTheme(
                darkTheme = uiPrefs.isDark
            ) {
                MaskBox(
                    animTime = 400,
                    maskComplete = { appViewModel.toggleDarkMode() },
                    animFinish = { appViewModel.resetDarkSwitch() },
                ) { maskActiveEvent ->
                    LaunchedEffect(uiPrefs.darkSwitchActive) {
                        if (!uiPrefs.darkSwitchActive) return@LaunchedEffect
                        // 先更新点击坐标
                        appViewModel.updateMaskClick(uiPrefs.maskClickX, uiPrefs.maskClickY)
                        maskActiveEvent(
                            if (uiPrefs.isDark) MaskAnimModel.SHRINK else MaskAnimModel.EXPEND,
                            uiPrefs.maskClickX,
                            uiPrefs.maskClickY
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = AppRoute.STATUS_CHECKS,
                            popEnterTransition = {
                                scaleIn(
                                    animationSpec = tween(durationMillis = 500, delayMillis = 35),
                                    initialScale = 1.1f,
                                ) + fadeIn(
                                    animationSpec = tween(durationMillis = 500, delayMillis = 35)
                                )
                            },
                            popExitTransition = {
                                scaleOut(
                                    targetScale = 0.9f,
                                ) + fadeOut(
                                    animationSpec = tween(
                                        durationMillis = 35,
                                        easing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f)
                                    )
                                )
                            },
                        ) {
                            composable(AppRoute.STATUS_CHECKS) {
                                StatusChecksPage(navController, appViewModel)
                            }
                            composable(
                                route = AppRoute.LOGIN,
                            ) {
                                LoginPage(navController, appViewModel)
                            }
                            composable(
                                route = AppRoute.HOME,
                            ) {
                                ContentUiPage(navController, appViewModel)
                            }
                            composable(
                                route = AppRoute.ABOUT,
                            ) {
                                AboutPage(navController, appViewModel)
                            }
                        }
                        Welcome(
                            konfettiState,
                            appViewModel,
                        )
                        Konfetti(konfettiState)
                    }
                }
            }
        }
    }
}