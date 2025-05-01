package com.ch4019.jdaassist.ui.screen.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.ch4019.jdaassist.config.AppRoute
import com.ch4019.jdaassist.config.LoginPageUiState
import com.ch4019.jdaassist.ui.components.CardButton
import com.ch4019.jdaassist.viewmodel.AppViewModel
import com.ch4019.jdaassist.viewmodel.LoginState
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun LoginPage(
    navController: NavHostController,
    appViewModel: AppViewModel,
) {
    var uiState by remember {
        mutableStateOf(LoginPageUiState("", "", false))
    }
//    var userName by remember { mutableStateOf("") }
//    var passWord by remember { mutableStateOf("") }
//    var autoUserName by remember { mutableStateOf("") }
//    var autoPassWord by remember { mutableStateOf("") }
//    var isShow by remember{ mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val loginState by appViewModel.loginState.collectAsState()
//    autoUserName = loginState.userName
//    autoPassWord = loginState.passWord
    LaunchedEffect(key1 = loginState) {
        if (loginState.isLogin) {
            navController.navigate(AppRoute.HOME){
                popUpTo(AppRoute.LOGIN){
                    inclusive = true
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    if (loginState.isAutoLogin && !loginState.isLastOpenData) {
        LaunchedEffect(key1 = loginState.isAutoLogin && !loginState.isLastOpenData) {
            appViewModel.getLoginState(
                LoginState(
                    loginState.userName,
                    loginState.passWord,
                )
            )
        }
        Dialog(
            onDismissRequest = {}
        ) {
            Card(
                shape = RoundedCornerShape(25.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "正在自动登录",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth(),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(it)
                .padding(top = 32.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "教务系统 ID",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "登录你的教务系统账户",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(32.dp))

//            VerificationCodeField(Modifier){newCode ->
//                uiState = uiState.copy(userName = newCode)
//            }

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                value = uiState.userName,
                onValueChange = { newValue ->
                    uiState = uiState.copy(userName = newValue)
                },
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                label = { Text(text = "学号") },
                keyboardOptions = KeyboardOptions().copy(keyboardType = KeyboardType.Number),
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                value = uiState.passWord,
                onValueChange = { newValue ->
                    uiState = uiState.copy(passWord = newValue)
                },
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                visualTransformation = if (uiState.isShowPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions().copy(keyboardType = KeyboardType.Password),
                label = { Text(text = "密码") },
                trailingIcon = {
                    IconButton(onClick = {
                        uiState = uiState.copy(isShowPassword = !uiState.isShowPassword)
                    }) {
                        Icon(
                            imageVector = if (uiState.isShowPassword) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            contentDescription = null,
                            tint = if (uiState.isShowPassword) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                },
            )
            Spacer(modifier = Modifier.height(32.dp))
            CardButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(top = 16.dp),
                onClick = {
                    scope.launch {
                        val loginResult = async {
                            appViewModel.getLoginState(
                                LoginState(
                                    uiState.userName,
                                    uiState.passWord,
                                )
                            )
                        }
                        loginResult.await()
                    }
                },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(text = "登录")
            }
        }
    }
}