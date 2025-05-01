package com.ch4019.jdaassist.ui.screen.main

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DataSaverOn
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.ToggleOff
import androidx.compose.material.icons.rounded.ToggleOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavHostController
import com.ch4019.jdaassist.config.AppRoute
import com.ch4019.jdaassist.model.ClassInfo
import com.ch4019.jdaassist.model.DARK_SWITCH_ACTIVE
import com.ch4019.jdaassist.model.IS_DARK_MODEL
import com.ch4019.jdaassist.model.MASK_CLICK_X
import com.ch4019.jdaassist.model.MASK_CLICK_Y
import com.ch4019.jdaassist.model.dataStore
import com.ch4019.jdaassist.ui.components.DatePicker
import com.ch4019.jdaassist.ui.screen.courses.CoursePage
import com.ch4019.jdaassist.ui.screen.grades.GradesPage
import com.ch4019.jdaassist.ui.screen.grades.SelectGrades
import com.ch4019.jdaassist.util.getToDayDate
import com.ch4019.jdaassist.util.showToast
import com.ch4019.jdaassist.viewmodel.AppViewModel
import com.ch4019.jdaassist.viewmodel.CourseJsonList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ContentUiPage(
    navController: NavHostController,
    appViewModel: AppViewModel
) {
    var name by remember { mutableStateOf("") }
    var coursesData = remember { mutableStateOf(CourseJsonList()) }
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

    val pagerState = rememberPagerState(
        initialPage = 0,
        0f
    ) { 2 }

    LaunchedEffect(Unit) {
        checkUser = true
    }
    LaunchedEffect(checkUser) {
        if (checkUser) {
            val result = appViewModel.getUserInfo()
            name = if (result.isSuccess) {
                result.getOrNull() ?: "用户信息为空"
            } else {
                "更新状态失败"
            }
            checkUser = false
        }
    }
//    获取当天日期
    val today = remember { derivedStateOf { getToDayDate() } }
    val modalBottomSheetState = rememberModalBottomSheetState()
    val classInfoModalBottomSheet = rememberModalBottomSheetState()
    val showBottomSheet = remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val height = with(density) { WindowInsets.statusBars.getTop(density).toDp() }

    LaunchedEffect(modalBottomSheetState.currentValue) {
        withContext(Dispatchers.IO) {
            if (modalBottomSheetState.currentValue == SheetValue.Hidden) {
                showBottomSheet.value = false
            }
        }
    }

    val selected1 = remember { mutableStateOf(false) }
    val selected2 = remember { mutableStateOf(false) }
    val scheduleState = mutableStateListOf("夏季作息", "冬季作息")
    var scheduleSelected by remember { mutableIntStateOf(0) }
    val startDate = remember { mutableStateOf("选择开学日期") }
    val isOpenDialog = remember { mutableStateOf(false) }
    val isSelectedDate = remember { mutableStateOf(false) }

    var academicYear by remember { mutableStateOf("2023") }
    var semester by remember { mutableStateOf("1") }
    val iaShowClassInfo = remember { mutableStateOf(false) }
    val classInfo = remember { mutableStateOf(ClassInfo()) }

    //                        定义一个用于控制缩放状态的变量
    var isPressed by remember { mutableStateOf(false) }
//                        使用 animateFloatAsState 动态控制缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f, // 按下时缩小到0.95f，松开时恢复到1f
        animationSpec = tween(durationMillis = 150) // 动画持续时间
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (pagerState.currentPage == 0) {
                        Text(
                            text = today.value,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    } else {
                        Surface(
                            onClick = { checkUser = true }
                        ) { Text(text = name) }
                    }
                },
                navigationIcon = {
                    IconButton(
                        enabled = !darkSwitchActive,
                        modifier = Modifier
                            .pointerInteropFilter { motionEvent ->
                                when (motionEvent.action) {
                                    MotionEvent.ACTION_DOWN -> {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isPressed = true // 按下时缩小
                                        true
                                    }

                                    MotionEvent.ACTION_UP -> {
                                        isPressed = false // 松开时恢复
                                        scope.launch {
                                            context.dataStore.edit {
                                                it[MASK_CLICK_X] = darkSwitchPositionX
                                                it[MASK_CLICK_Y] = darkSwitchPositionY
                                                it[DARK_SWITCH_ACTIVE] = true
                                            }
                                        }
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
                        onClick = {
//                            scope.launch {
//                                context.dataStore.edit {
//                                    it[MASK_CLICK_X] = darkSwitchPositionX
//                                    it[MASK_CLICK_Y] = darkSwitchPositionY
//                                    it[DARK_SWITCH_ACTIVE] = true
//                                }
//                            }
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
                    AnimatedVisibility(
                        visible = pagerState.currentPage == 0,
                    ) {
                        IconButton(
                            onClick = {
                                showBottomSheet.value = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DataSaverOn,
                                contentDescription = "Next"
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch {
                                if (pagerState.currentPage == 0) {
                                    pagerState.animateScrollToPage(
                                        1,
                                        0f,
                                        tween(
                                            durationMillis = 500
                                        )
                                    )
                                } else {
                                    pagerState.animateScrollToPage(
                                        0,
                                        0f,
                                        tween(
                                            durationMillis = 500
                                        )
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (pagerState.currentPage == 0) Icons.Rounded.ToggleOff else Icons.Rounded.ToggleOn,
                            contentDescription = "Logout",
                        )
                    }
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
                            appViewModel.logout()
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
        VerticalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> CoursePage(appViewModel, iaShowClassInfo, classInfo)
                1 -> GradesPage(appViewModel)
            }
        }

        DatePickerDialog(
            isOpenDialog,
            startDate,
            isSelectedDate
        )
        if (iaShowClassInfo.value) {
            ModalBottomSheet(
                onDismissRequest = {
                    iaShowClassInfo.value = false
                },
                sheetState = classInfoModalBottomSheet,
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
                        Text(
                            text = classInfo.value.className,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "${classInfo.value.classCredit}学分",
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
                    val classShow = listOf(
                        classInfo.value.classWeek,
                        classInfo.value.classTime,
                        classInfo.value.classTeacher,
                        classInfo.value.classRoom
                    )
                    classShow.forEachIndexed { index, _ ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BarChart,
                                contentDescription = "Logout",
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = classShow[index],
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
        if (showBottomSheet.value) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet.value = false
                    isOpenDialog.value = false
                    selected1.value = false
                    selected2.value = false
                    isSelectedDate.value = false
                    startDate.value = "选择开学日期"
                },
                sheetState = modalBottomSheetState,
//                dragHandle = {
//                    DragHandle(
//                        modifier = Modifier.padding(top = height),
//                    )
//                },
                contentWindowInsets = { WindowInsets(top = 0.dp) }
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SelectGrades(
                        selected1,
                        selected2,
                        onYearSelected = { year -> academicYear = year },
                        onSemesterSelected = { term -> semester = term }
                    )
                    InputChip(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(top = 8.dp)
                            .defaultMinSize(
                                minHeight = 36.dp
                            ),
                        shape = RoundedCornerShape(20.dp),
                        selected = isSelectedDate.value,
                        onClick = {
                            isOpenDialog.value = true
                        },
                        label = {
                            Text(
                                startDate.value,
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    )
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(top = 8.dp),
                    ) {
                        scheduleState.forEachIndexed { index, schedule ->
                            SegmentedButton(
                                selected = scheduleSelected == index,
                                onClick = {
                                    scheduleSelected = index
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index,
                                    scheduleState.size
                                )
                            ) {
                                Text(text = schedule)
                            }

                        }
                    }
                    Spacer(Modifier.height(23.dp))
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        state = rememberTooltipState(),
                        tooltip = {
                            PlainTooltip {
                                Text(text = "确认查询")
                            }
                        }
                    ) {
                        OutlinedButton(
                            onClick = {
                                when {
                                    selected1.value && selected2.value -> scope.launch {
                                        val courseResult = async {
                                            appViewModel.getCourseData(
                                                scheduleSelected,
                                                academicYear,
                                                semester,
                                                startDate.value
                                            )
                                        }
                                        courseResult.await()
                                    }

                                    selected1.value -> showToast(context, "请选择学期")
                                    selected2.value -> showToast(context, "请选择学年")
                                    else -> showToast(context, "请选择学年和学期")
                                }
                            }
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = "查询课表"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DatePickerDialog(
    isOpenDialog: MutableState<Boolean>,
    startDate: MutableState<String>,
    isSelectedDate: MutableState<Boolean>
) {
    if (isOpenDialog.value) {
        DatePicker(
            isOpenDialog = isOpenDialog,
        ) {
            startDate.value =
                SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(
                    Date(
                        it
                    )
                )
            if (it != 0L) isSelectedDate.value = true
        }
    }
}
