package com.ch4019.jdaassist.ui.screen.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavHostController
import com.ch4019.jdaassist.config.AppRoute
import com.ch4019.jdaassist.model.DARK_SWITCH_ACTIVE
import com.ch4019.jdaassist.model.IS_DARK_MODEL
import com.ch4019.jdaassist.model.MASK_CLICK_X
import com.ch4019.jdaassist.model.MASK_CLICK_Y
import com.ch4019.jdaassist.model.dataStore
import com.ch4019.jdaassist.ui.screen.grades.GradesPage
import com.ch4019.jdaassist.viewmodel.LoginViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentUiPage(
    navController: NavHostController,
    loginViewModel: LoginViewModel
) {
    var name by remember { mutableStateOf("") }
    var checkUser by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var darkSwitchPositionX by remember {
        mutableFloatStateOf(0f)
    }
    var darkSwitchPositionY by remember {
        mutableFloatStateOf(0f)
    }
    val darkSwitchActive by context.dataStore.data.map { preferences ->
        preferences[DARK_SWITCH_ACTIVE] ?: false
    }.collectAsState(initial = false)
    val isDarkTheme by context.dataStore.data.map { preferences ->
        preferences[IS_DARK_MODEL] ?: false
    }.collectAsState(initial = false)
    LaunchedEffect(Unit) {
        checkUser = true
    }
    LaunchedEffect(checkUser) {
        if (checkUser) {
            val result = loginViewModel.getUserInfo()
            name = if (result.isSuccess) {
                result.getOrNull() ?: "用户信息为空"
            } else {
                "更新状态失败"
            }
            checkUser = false
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Surface(
                        onClick = { checkUser = true }
                    ) { Text(text = name) }
                },
                navigationIcon = {
                    IconButton(
                        enabled = !darkSwitchActive,
                        onClick = {
                            scope.launch {
                                context.dataStore.edit {
                                    it[MASK_CLICK_X] = darkSwitchPositionX
                                    it[MASK_CLICK_Y] = darkSwitchPositionY
                                    it[DARK_SWITCH_ACTIVE] = true
                                }
                            }
                        }
                    ) {
                        Icon(
                            modifier = Modifier
                                .onGloballyPositioned {coordinates ->
                                    darkSwitchPositionX = coordinates.boundsInRoot().center.x
                                    darkSwitchPositionY = coordinates.boundsInRoot().center.y
                                },
                            imageVector = if(isDarkTheme)Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(AppRoute.ABOUT) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    ) {
                        Icon(
                            modifier = Modifier.graphicsLayer {
                                rotationZ = 90f
                            },
                            imageVector = Icons.Rounded.BarChart,
                            contentDescription = "Logout",
                        )
                    }
                    IconButton(
                        onClick = {
                            loginViewModel.logout()
                            navController.navigate(AppRoute.LOGIN) {
                                popUpTo(AppRoute.HOME) {
                                    inclusive = true
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = "Logout",
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GradesPage(loginViewModel)
        }
    }
}