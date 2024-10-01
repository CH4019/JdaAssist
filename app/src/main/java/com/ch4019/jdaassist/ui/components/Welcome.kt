package com.ch4019.jdaassist.ui.components

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ch4019.jdaassist.model.WELCOME_STATUS
import com.ch4019.jdaassist.model.dataStore
import com.ch4019.jdaassist.util.openWebPage
import com.ch4019.jdaassist.viewmodel.AppViewModel
import kotlinx.coroutines.flow.map


@Composable
fun Welcome(
    konfettiState: MutableState<Boolean>,
    appViewModel: AppViewModel,
    isAgreePrivacy: Boolean
) {
    val context = LocalContext.current
    val activity = (context as? Activity)
    var visible by remember { mutableStateOf(false) }

    val konfettiValue by context.dataStore.data.map { preferences ->
        preferences[WELCOME_STATUS] ?: false
    }.collectAsState(initial = false)

    visible = isAgreePrivacy

    if (visible) {
        Permission(
            onDismissRequest = {
            },
            onDismiss = {
                visible = false
                // 结束 Activity
                activity?.finishAffinity()
            },
            onConfirm = {
                visible = false
                konfettiState.value = true
                appViewModel.makeAgreePrivacy(true)
            },
        )
    }
}

@Composable
fun Permission(
    onDismissRequest: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current
    val annotatedString = buildAnnotatedString {
        append("根据相关政策规定，你需要先阅读并同意")
        pushStringAnnotation(
            tag = "terms",
            annotation = "https://jdaassistant.ch4019.fun/docs/AppUpdateLog/terms_of_user"
        )
        addStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary), start = 18, end = 33)
        append("《JdaAssist使用条例》")
        pop()
        append("和")
        pushStringAnnotation(
            tag = "privacy",
            annotation = "https://jdaassistant.ch4019.fun/docs/AppUpdateLog/privacy"
        )
        addStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary), start = 34, end = 40)
        append("《隐私协议》")
        pop()
        append("后才能开始使用本软件")
    }
    DynamicHeightDialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "JdaAssist",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ClickableText(
                    text = annotatedString,
                    style = TextStyle(fontWeight = FontWeight.Light, fontSize = 16.sp),
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(
                            tag = "terms",
                            start = offset,
                            end = offset
                        )
                            .firstOrNull()?.let { annotation ->
                                openWebPage(context, annotation.item)
                            }
                        annotatedString.getStringAnnotations(
                            tag = "privacy",
                            start = offset,
                            end = offset
                        )
                            .firstOrNull()?.let { annotation ->
                                openWebPage(context, annotation.item)
                            }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
//                Text(
//                    text = "根据相关政策规定，你需要先阅读并同意《JdaAssist使用条例》和《隐私协议》后才能开始使用本软件",
//                    fontWeight = FontWeight.Light,
//                    fontSize = 16.sp,
//                    modifier = Modifier.fillMaxWidth()
//                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "为确保软件的正常运行，软件会请求以下权限",
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
//              权限列表
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 112.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.SwapVert,
                            contentDescription = null,
                        )
                        Spacer(Modifier.width(72.dp))
                        Text("网络权限")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Storage,
                            contentDescription = null,
                        )
                        Spacer(Modifier.width(72.dp))
                        Text("存储权限")
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    CardButton(
                        onClick = {
                            onDismiss()
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(text = "拒绝")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    CardButton(
                        onClick = {
                            onConfirm()
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(text = "同意")
                    }
                }
            }
        }
    }
}