package com.ch4019.jdaassist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ch4019.jdaassist.config.AppRoute
import com.ch4019.jdaassist.model.DARK_SWITCH_ACTIVE
import com.ch4019.jdaassist.model.IS_DARK_MODEL
import com.ch4019.jdaassist.model.MASK_CLICK_X
import com.ch4019.jdaassist.model.MASK_CLICK_Y
import com.ch4019.jdaassist.model.MaskAnimModel
import com.ch4019.jdaassist.model.WELCOME_STATUS
import com.ch4019.jdaassist.model.dataStore
import com.ch4019.jdaassist.ui.components.Konfetti
import com.ch4019.jdaassist.ui.components.MaskBox
import com.ch4019.jdaassist.ui.components.Welcome
import com.ch4019.jdaassist.ui.components.rememberKonfettiState
import com.ch4019.jdaassist.ui.screen.about.AboutPage
import com.ch4019.jdaassist.ui.screen.login.LoginPage
import com.ch4019.jdaassist.ui.screen.main.ContentUiPage
import com.ch4019.jdaassist.ui.screen.splash.SplashPage
import com.ch4019.jdaassist.ui.theme.JdaAssistTheme
import com.ch4019.jdaassist.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //设置全屏
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()
            val appViewModel: AppViewModel = viewModel()
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            val welcomeStatus by context.dataStore.data
                .map { preferences ->
                    preferences[WELCOME_STATUS] ?: false
                }
                .collectAsState(initial = false)
            val konfettiState = rememberKonfettiState(welcomeStatus)
            val isDarkTheme by context.dataStore.data
                .map { preferences ->
                    preferences[IS_DARK_MODEL] ?: false
                }
                .collectAsState(initial = false)
            val darkSwitchActive by context.dataStore.data
                .map { preferences ->
                    preferences[DARK_SWITCH_ACTIVE] ?: false
                }
                .collectAsState(initial = false)
            val maskClickX by context.dataStore.data
                .map { preferences ->
                    preferences[MASK_CLICK_X] ?: 0f
                }
                .collectAsState(initial = 0f)
            val maskClickY by context.dataStore.data
                .map { preferences ->
                    preferences[MASK_CLICK_Y] ?: 0f
                }
                .collectAsState(initial = 0f)
            JdaAssistTheme(
                darkTheme = isDarkTheme
            ) {
                MaskBox(
                    animTime = 400,
                    maskComplete = {
                        scope.launch {
                            context.dataStore.edit {
                                it[IS_DARK_MODEL] = !isDarkTheme
                            }
                        }
                    },
                    animFinish = {
                        scope.launch {
                            context.dataStore.edit {
                                it[DARK_SWITCH_ACTIVE] = false
                            }
                        }
                    },
                ) { maskActiveEvent ->
                    LaunchedEffect(darkSwitchActive) {
                        if (!darkSwitchActive) return@LaunchedEffect
                        if (isDarkTheme) {
                            maskActiveEvent(MaskAnimModel.SHRINK, maskClickX, maskClickY)
                        }else {
                            maskActiveEvent(MaskAnimModel.EXPEND, maskClickX, maskClickY)
                        }
                    }
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = AppRoute.SPLASH,
                        ) {
                            composable(AppRoute.SPLASH) {
                                SplashPage(navController, appViewModel)
                            }
                            composable(
                                route = AppRoute.LOGIN,
                                enterTransition = {
                                    fadeIn(
                                        animationSpec = tween(
                                            300, easing = LinearOutSlowInEasing
                                        )
                                    ) + slideIntoContainer(
                                        animationSpec = tween(300, easing = EaseIn),
                                        towards = AnimatedContentTransitionScope.SlideDirection.End
                                    )
                                },
                                exitTransition = {
                                    fadeOut(
                                        animationSpec = tween(
                                            300, easing = LinearOutSlowInEasing
                                        )
                                    ) + slideOutOfContainer(
                                        animationSpec = tween(300, easing = EaseOut),
                                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                                    )
                                },
                            ) {
                                LoginPage(navController, appViewModel)
                            }
                            composable(AppRoute.HOME) {
                                ContentUiPage(navController, appViewModel)
                            }
                            composable(AppRoute.ABOUT) {
                                AboutPage(navController, appViewModel)
                            }
                        }
                        Welcome(
                            konfettiState,
                            appViewModel
                        )
                        Konfetti(konfettiState)
                    }
                }
            }
        }
    }
}