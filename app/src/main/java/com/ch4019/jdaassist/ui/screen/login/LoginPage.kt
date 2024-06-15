package com.ch4019.jdaassist.ui.screen.login

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ch4019.jdaassist.config.AppRoute
import com.ch4019.jdaassist.ui.components.CardButton
import com.ch4019.jdaassist.viewmodel.LoginState
import com.ch4019.jdaassist.viewmodel.LoginViewModel

import kotlinx.coroutines.launch

@Composable
fun LoginPage(
    navController: NavHostController,
    loginViewModel: LoginViewModel,
) {
    var userName by remember { mutableStateOf("") }
    var passWord by remember { mutableStateOf("") }
    var isShow by remember{ mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val loginState by loginViewModel.loginState.collectAsState()
    LaunchedEffect(loginState.isLogin) {
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
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                value = userName,
                onValueChange = { newValue ->
                    userName = newValue
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
                value = passWord,
                onValueChange = { newValue ->
                    passWord = newValue
                },
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                visualTransformation = if (isShow) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions().copy(keyboardType = KeyboardType.Password),
                label = { Text(text = "密码") },
                trailingIcon = {
                    IconButton(onClick = { isShow = !isShow }) {
                        Icon(
                            imageVector=if (isShow) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            contentDescription = null,
                            tint = if (isShow) MaterialTheme.colorScheme.primary else LocalContentColor.current
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
                        loginViewModel.getLoginState(
                            LoginState(
                                userName,
                                passWord,
                            )
                        )
                    }
                }
            ) {
                Text(text = "登录")
            }
        }
    }
}