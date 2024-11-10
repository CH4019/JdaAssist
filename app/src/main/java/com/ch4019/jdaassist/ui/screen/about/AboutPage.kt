package com.ch4019.jdaassist.ui.screen.about

import android.content.Intent
import android.net.Uri
import android.view.MotionEvent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.pm.PackageInfoCompat.getLongVersionCode
import androidx.navigation.NavHostController
import com.ch4019.jdaassist.R
import com.ch4019.jdaassist.ui.components.CardButton
import com.ch4019.jdaassist.util.bytesToMb
import com.ch4019.jdaassist.util.getPackageInfoCompat
import com.ch4019.jdaassist.util.openWebPage
import com.ch4019.jdaassist.viewmodel.AppViewModel
import com.ch4019.jdaassist.viewmodel.UpdateStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AboutPage(
    navController: NavHostController,
    appViewModel: AppViewModel
) {
    val appVisionState = appViewModel.appVisionState.collectAsState()
    val showNewVisionModalBottomSheet = rememberModalBottomSheetState()
//    定义一个用于控制缩放状态的变量
    var isPressed by remember { mutableStateOf(false) }
//    使用 animateFloatAsState 动态控制缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f, // 按下时缩小到0.95f，松开时恢复到1f
        animationSpec = tween(durationMillis = 150) // 动画持续时间
    )
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = null
                        )
                    }
                },
            )
        },
        bottomBar = {

        }
    ) { paddingValues ->
        AboutView(
            modifier = Modifier
                .padding(paddingValues),
            appViewModel
        )
        when (appVisionState.value.isNewVision) {
            UpdateStatus.Checking -> {
                Dialog(
                    onDismissRequest = { }
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
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                strokeCap = StrokeCap.Round
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "正在检查更新...",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            UpdateStatus.Available -> {
                ModalBottomSheet(
                    onDismissRequest = {
                        appViewModel.closeNewVision()
                    },
                    sheetState = showNewVisionModalBottomSheet,
                    contentWindowInsets = { WindowInsets(top = 0.dp) },
//                containerColor = MaterialTheme.colorScheme.primaryContainer,
                    dragHandle = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LogoImage(
                                Modifier
                                    .size(48.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = stringResource(id = R.string.app_name),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = appVisionState.value.appVersion.version,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier
//                        .fillMaxHeight()
                            .padding(vertical = 8.dp)
                            .padding(bottom = 56.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val appSizeInBytes = appVisionState.value.appVersion.assets[0].appsSize
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "大小：",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "${bytesToMb(appSizeInBytes)}MB (${appSizeInBytes}Bytes)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        CardButton(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp)
                                .padding(top = 16.dp)
                                .pointerInteropFilter { motionEvent ->
                                    when (motionEvent.action) {
                                        MotionEvent.ACTION_DOWN -> {
                                            isPressed = true // 按下时缩小
                                            true
                                        }

                                        MotionEvent.ACTION_UP -> {
                                            isPressed = false // 松开时恢复
                                            openWebPage(
                                                context,
                                                appVisionState.value.appVersion.assets[0].url
                                            )
                                            true
                                        }

                                        MotionEvent.ACTION_CANCEL -> {
                                            isPressed = false // 松开时恢复
                                            true
                                        }

                                        else -> false
                                    }
                                }
                                .graphicsLayer {
                                    this.scaleX = scale
                                    this.scaleY = scale
                                    this.transformOrigin = TransformOrigin.Center
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(text = "前往浏览器更新")
                        }
                    }
                }
            }

            UpdateStatus.NotAvailable, UpdateStatus.Error -> {}
        }
    }
}

@Composable
fun AboutView(
    modifier: Modifier,
    appViewModel: AppViewModel
) {
    val context = LocalContext.current
    val urlGithub = "https://github.com/CH4019/JdaAssist"
    val urlTerms = "https://jdaassistant.ch4019.fun/docs/AppUpdateLog/terms_of_user"
    val urlPrivacy = "https://jdaassistant.ch4019.fun/docs/AppUpdateLog/privacy"
    val intentGithub = Intent(Intent.ACTION_VIEW, Uri.parse(urlGithub))
    val intentTerms = Intent(Intent.ACTION_VIEW, Uri.parse(urlTerms))
    val intentPrivacy = Intent(Intent.ACTION_VIEW, Uri.parse(urlPrivacy))
    val packageInfo = context.packageManager.getPackageInfoCompat(context.packageName, 0)
    val versionName = packageInfo.versionName
    val versionCode = getLongVersionCode(packageInfo)
    val scope = rememberCoroutineScope()
    var isAutoLogin by remember { mutableStateOf(false) }
    val loginState by appViewModel.loginState.collectAsState()
    SideEffect { isAutoLogin = loginState.isAutoLogin }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LogoImage(
            Modifier
                .padding(top = 32.dp)
                .size(72.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Jda Assist",
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(48.dp))
        Surface(
            shape = RoundedCornerShape(15.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Column {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = Color.Transparent,
                    onClick = {
                        appViewModel.getNewVision()
//                        Toast.makeText(context, "应用内更新功能暂未开放", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("当前版本")
                        Spacer(Modifier.weight(1f))
                        Text("$versionName($versionCode)")
                    }
                }
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = Color.Transparent,
                    onClick = {
                        context.startActivity(intentGithub)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Github主页")
                        Spacer(Modifier.weight(1f))
                        Icon(
                            Icons.Rounded.Link,
                            contentDescription = null,
                        )
                    }
                }
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("每日首次启动自动登录")
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = isAutoLogin,
                        onCheckedChange = {
                            scope.launch {
                                isAutoLogin = it
                                appViewModel.setIsAutoLogin(it)
                            }
                        },
                        thumbContent = {
                            AnimatedContent(
                                isAutoLogin,
                                transitionSpec = {
                                    scaleIn() togetherWith scaleOut()
                                },
                                label = ""
                            ) { targetState ->
                                Icon(
                                    imageVector = if (targetState) Icons.Rounded.Check else Icons.Rounded.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        }
                    )
                }
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = Color.Transparent,
                    onClick = {
                        context.startActivity(intentPrivacy)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("隐私政策")
                        Spacer(Modifier.weight(1f))
                        Icon(
                            Icons.Rounded.Link,
                            contentDescription = null,
                        )
                    }
                }
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = Color.Transparent,
                    onClick = {
                        context.startActivity(intentTerms)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("用户协议")
                        Spacer(Modifier.weight(1f))
                        Icon(
                            Icons.Rounded.Link,
                            contentDescription = null,
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "纵然世间黑暗      仍有一点星光",
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun LogoImage(modifier: Modifier = Modifier) {
    Image(
        bitmap = ImageBitmap.imageResource(R.drawable.logo),
        contentDescription = "logo",
        modifier
    )
}