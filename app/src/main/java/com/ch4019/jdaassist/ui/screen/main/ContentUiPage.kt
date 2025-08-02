package com.ch4019.jdaassist.ui.screen.main

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
    var checkUser by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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
    val hapticFeedback = LocalHapticFeedback.current

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
    var academicYear by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }
    val iaShowClassInfo = remember { mutableStateOf(false) }
    val classInfo = remember { mutableStateOf(ClassInfo()) }

    Scaffold(
        topBar = {
            AppTopBar(
                pagerState.currentPage,
                today.value,
                name,
                isDarkTheme,
                darkSwitchActive,
                onSwitchToggle = { x, y -> // 按下切换主题时保存坐标并激活遮罩
                    scope.launch {
                        context.dataStore.edit {
                            it[MASK_CLICK_X] = x
                            it[MASK_CLICK_Y] = y
                            it[DARK_SWITCH_ACTIVE] = true
                        }
                    }
                },
                onOpenSheet = { // 点击 DataSaverOn 图标时打开 BottomSheet
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    showBottomSheet.value = true
                },
                onPageToggle = { // 点击 ToggleOn/ToggleOff 图标时切换页面
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        val target = if (pagerState.currentPage == 0) 1 else 0
                        pagerState.animateScrollToPage(target, 0f, tween(500))
                    }
                },
                onNavigateAbout = { // 点击 BarChart 图标时跳转 About
                    navController.navigate(AppRoute.ABOUT) {
                        launchSingleTop = true; restoreState = true
                    }
                },
                onLogout = { // 点击 Logout 图标时登出并返回登录页
                    appViewModel.logout()
                    navController.navigate(AppRoute.LOGIN) {
                        popUpTo(AppRoute.HOME) { inclusive = true; saveState = true }
                        launchSingleTop = true; restoreState = true
                    }
                }
            )
        },
    ) { paddingValues ->
        MainPager(
            pagerState,
            { CoursePage(appViewModel, iaShowClassInfo, classInfo) },
            { GradesPage(appViewModel) },
            modifier = Modifier.padding(paddingValues)
        )
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
                contentWindowInsets = { WindowInsets(top = 0.dp) }
            ) {
                CourseFilterSheet(
                    academicYear = academicYear,
                    semester = semester,
                    startDate = startDate.value,
                    scheduleOptions = scheduleState,
                    selectedSchedule = scheduleSelected,
                    // 当学年被选中时，除了回调外也要标记 selected1=true
                    onYearChange = { year ->
                        academicYear = year
                        selected1.value = true
                    },
                    // 当学期被选中时，除了回调外也要标记 selected2=true
                    onSemesterChange = { term ->
                        semester = term
                        selected2.value = true
                    },
                    // 打开日期选择对话框
                    onDateClick = { isOpenDialog.value = true },
                    // 切换作息表选项
                    onScheduleSelect = { idx -> scheduleSelected = idx },
                    // 点击“查询课表”按钮时，执行原来的逻辑
                    onQuery = {
                        val start = startDate.value.trim()
                        when {
                            start.isBlank() || start == "选择开学日期" -> showToast(
                                context,
                                "请选择开学日期"
                            )

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
                )
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

@Composable
fun MainPager(
    pagerState: PagerState,
    coursePage: @Composable () -> Unit,
    gradesPage: @Composable () -> Unit,
    modifier: Modifier
) {
    VerticalPager(
        state = pagerState,
        userScrollEnabled = false,
        modifier = modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> coursePage()
            1 -> gradesPage()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentPage: Int,
    today: String,
    userName: String,
    isDarkTheme: Boolean,
    isSwitchActive: Boolean,
    onSwitchToggle: (Float, Float) -> Unit,
    onOpenSheet: () -> Unit,
    onPageToggle: () -> Unit,
    onNavigateAbout: () -> Unit,
    onLogout: () -> Unit
) {
    TopAppBar(
        title = {
            if (currentPage == 0) {
                BasicText(
                    text = today,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = LocalContentColor.current
                    ),
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 10.sp,
                        maxFontSize = 24.sp,
                        stepSize = 1.sp
                    ),
                    maxLines = 1
                )
            } else Text(userName, fontWeight = FontWeight.Bold)
        },
        navigationIcon = {
            DarkSwitchButton(
                isDark = isDarkTheme,
                isActive = isSwitchActive,
                onActivate = onSwitchToggle
            )
        },
        actions = {
            if (currentPage == 0) IconButton(onClick = onOpenSheet) {
                Icon(Icons.Rounded.DataSaverOn, contentDescription = "查询课表")
            }
            IconButton(onClick = onPageToggle) {
                Icon(
                    imageVector = if (currentPage == 0) Icons.Rounded.ToggleOff else Icons.Rounded.ToggleOn,
                    contentDescription = "切换页面"
                )
            }
            IconButton(onClick = onNavigateAbout) {
                Icon(Icons.Rounded.BarChart, contentDescription = "关于")
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "登出")
            }
        }
    )
}

@Composable
fun DarkSwitchButton(
    isDark: Boolean,
    isActive: Boolean,
    onActivate: (x: Float, y: Float) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.8f else 1f, tween(150))
    var posX by remember { mutableFloatStateOf(0f) }
    var posY by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current
//    val context = LocalContext.current

    IconButton(
        enabled = !isActive,
        modifier = Modifier
            .onGloballyPositioned { pos ->
                val bounds = pos.boundsInRoot()
                posX = bounds.center.x; posY = bounds.center.y
            }
            .graphicsLayer {
                scaleX = scale; scaleY = scale; this.transformOrigin = TransformOrigin.Center
            }
            .pointerInteropFilter { e ->
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isPressed =
                            true; haptic.performHapticFeedback(HapticFeedbackType.LongPress); true
                    }

                    MotionEvent.ACTION_UP -> {
                        isPressed = false; onActivate(posX, posY); true
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        isPressed = false; true
                    }

                    else -> false
                }
            },
        onClick = { /* no-op */ }
    ) {
        Icon(if (isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode, null)
    }
}

@Composable
fun CourseFilterSheet(
    academicYear: String,
    semester: String,
    startDate: String,
    scheduleOptions: List<String>,
    selectedSchedule: Int,
    onYearChange: (String) -> Unit,
    onSemesterChange: (String) -> Unit,
    onDateClick: () -> Unit,
    onScheduleSelect: (Int) -> Unit,
    onQuery: () -> Unit
) {
    Column(Modifier.padding(16.dp)) {
        SelectGrades(
            academicYear,
            semester,
            onYearSelected = { year -> onYearChange(year) },
            onSemesterSelected = { term -> onSemesterChange(term) }
        )
        Spacer(Modifier.height(8.dp))
        InputChip(
            modifier = Modifier.fillMaxWidth(),
            selected = startDate.isNotBlank(),
            onClick = onDateClick,
            label = { Text(startDate.ifBlank { "选择开学日期" }) }
        )
        Spacer(Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(
            Modifier.fillMaxWidth()
        ) {
            scheduleOptions.forEachIndexed { idx, label ->
                SegmentedButton(
                    selected = selectedSchedule == idx,
                    onClick = { onScheduleSelect(idx) },
                    shape = SegmentedButtonDefaults.itemShape(
                        idx,
                        scheduleOptions.size
                    )
                ) { Text(label) }
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onQuery, Modifier.fillMaxWidth()) {
            Text("查询课表")
        }
    }
}

