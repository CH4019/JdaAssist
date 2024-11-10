package com.ch4019.jdaassist.ui.screen.courses

import android.view.MotionEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ch4019.jdaassist.model.ClassInfo
import com.ch4019.jdaassist.model.Schedule
import com.ch4019.jdaassist.util.calculateDateDifference
import com.ch4019.jdaassist.util.getWeekCourse
import com.ch4019.jdaassist.util.getWeekSince
import com.ch4019.jdaassist.viewmodel.AppViewModel
import com.ch4019.jdaassist.viewmodel.CourseJsonList
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CoursePage(
//    coursesData : CourseJsonList,
    appViewModel: AppViewModel,
    iaShowClassInfo: MutableState<Boolean>,
    classInfo: MutableState<ClassInfo>
) {
    var courseData by remember { mutableStateOf(CourseJsonList()) }
    var isSummerTime by remember { mutableStateOf(true) }
    val coursesData = appViewModel.courseState.collectAsState()


    // 获取当前日期
    val currentDate = Calendar.getInstance()
    // 获取学期开始日期
    val semesterStartDate = Calendar.getInstance().apply {
        val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        time = formatter.parse(coursesData.value.startDate) ?: Date() // 解析日期字符串，如果解析失败则使用当前日期
    }

    // 计算当前星期几 (1 for Monday, 7 for Sunday)
    val currentDayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK)
    val semesterEndDate = (semesterStartDate.clone() as Calendar).apply {
        add(Calendar.WEEK_OF_YEAR, 21)
    }


    val isBefore = currentDate < semesterStartDate
    val isAfter = currentDate >= semesterEndDate
    val dateWith = calculateDateDifference(currentDate, semesterStartDate)

    // 计算当前周数
    val currentWeek = remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        currentWeek.intValue = if (isBefore) {
            0
        } else getWeekSince(semesterStartDate, currentDate)
    }


    val pagerState = rememberPagerState(
        initialPage = currentWeek.intValue,
        0f
    ) {
        if (isAfter) {
            1
        } else {
            20 + if (isBefore) 1 else 0
        }
    }
    LaunchedEffect(coursesData.value.courseData) {
        courseData = CourseJsonList(coursesData.value.courseData)
        isSummerTime = coursesData.value.isSummerTime
        pagerState.animateScrollToPage(
            currentWeek.intValue
        )
    }


    Box {
        Box(
            modifier = Modifier
                .fillMaxSize()
//                .background(
//                    color = Color(0xFFC1D2EE)
//                )
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
        ) { week ->
            // 计算当前周的周一日期
            val currentWeekMonday = Calendar.getInstance().apply {
                timeInMillis = semesterStartDate.timeInMillis
                add(Calendar.WEEK_OF_YEAR, if (isBefore) week - 1 else week)
                firstDayOfWeek = Calendar.MONDAY
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            }

            when {
                isAfter -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("本学期课程已结束!")
                    }
                }

                isBefore && week == 0 -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("距离开学还有${dateWith}天")
                    }
                }

                else -> {
                    val courseData1 = getWeekCourse(week + if (isBefore) 0 else 1, courseData)
                    val currentMondayCopy =
                        currentWeekMonday.clone() as Calendar // 拷贝对象，避免修改原始 `currentWeekMonday`
                    WeekCourseView(
                        isSummerTime,
                        courseData1,
                        currentMondayCopy,
                        iaShowClassInfo,
                        classInfo
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WeekCourseView(
    isSummerTime: Boolean,
    courseData: List<CourseJsonList.CourseData>,
    currentMondayCopy: Calendar,
    iaShowClassInfo: MutableState<Boolean>,
    classInfo: MutableState<ClassInfo>,
) {
    val schedule = if (isSummerTime) Schedule.summerSchedule else Schedule.winterSchedule
    val scrollState = rememberScrollState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp // 获取屏幕宽度
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Column(
                modifier = Modifier
                    .height(56.dp)
                    .width(42.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = SimpleDateFormat(
                        "MM",
                        Locale.getDefault()
                    ).format(currentMondayCopy.time),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "月",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            val week = 1..7
            val weekList = listOf("一", "二", "三", "四", "五", "六", "日")
            val weekWidth = (screenWidth - 42.dp) / 7
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth(),
            ) {
                week.forEach {
                    val dayCalendar = (currentMondayCopy.clone() as Calendar).apply {
                        add(Calendar.DAY_OF_YEAR, it - 1)
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(weekWidth),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = weekList[it - 1],
                            fontSize = 20.sp
                        )
                        Text(
                            text = SimpleDateFormat("dd/MM", Locale.getDefault()).format(
                                dayCalendar.time // 使用计算后的日期
                            ),
                            fontSize = 14.sp
                        )
                    }
//                    content()
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(scrollState)
        ) {
            Row {
                Column(
                    modifier = Modifier
                        .width(42.dp),
                ) {
                    schedule.forEachIndexed { _, courseDate ->
                        Column(
                            modifier = Modifier
                                .height(72.dp)
                                .width(42.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = courseDate.name.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 1.sp,
                            )
                            Text(
                                text = courseDate.startDate,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 1.sp,
                            )
                            Text(
                                text = courseDate.endDate,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 1.sp,
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    courseData.forEachIndexed { _, courseData ->
                        val times = courseData.courseTime
                        val (startTime, endTime) = times.split("-").map { it.toInt() }
                        val day = when (courseData.courseWeekName) {
                            "星期一" -> 0
                            "星期二" -> 1
                            "星期三" -> 2
                            "星期四" -> 3
                            "星期五" -> 4
                            "星期六" -> 5
                            "星期日" -> 6
                            else -> 7
                        }
                        val availableWidth = screenWidth - 42.dp // 减去左边用掉的宽度
                        val cardWidth = availableWidth / 7 // 计算每个 Card 的宽度
                        val boxHeight = 72.dp * (endTime - startTime + 1)
                        val topPadding = 72.dp * (startTime - 1)
                        val startPadding = cardWidth * day

//                        定义一个用于控制缩放状态的变量
                        var isPressed by remember { mutableStateOf(false) }
//                        使用 animateFloatAsState 动态控制缩放动画
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.95f else 1f, // 按下时缩小到0.95f，松开时恢复到1f
                            animationSpec = tween(durationMillis = 150) // 动画持续时间
                        )
                        Card(
                            modifier = Modifier
                                .width(cardWidth)
                                .height(boxHeight)
                                .offset(x = startPadding, y = topPadding)
                                .padding(3.dp)
                                .pointerInteropFilter { motionEvent ->
                                    when (motionEvent.action) {
                                        MotionEvent.ACTION_DOWN -> {
                                            isPressed = true // 按下时缩小
                                            true
                                        }

                                        MotionEvent.ACTION_UP -> {
                                            isPressed = false // 松开时恢复
                                            iaShowClassInfo.value = true
                                            classInfo.value = ClassInfo().copy(
                                                className = courseData.courseName,
                                                classCredit = courseData.courseCredit,
                                                classWeek = courseData.courseWeek,
                                                classTime = "${courseData.courseTime}节",
                                                classTeacher = courseData.courseTeacher,
                                                classRoom = "${courseData.coursePlace}  ${courseData.coursePosition}",
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
                            shape = RoundedCornerShape(15f),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    schedule[startTime - 1].startDate,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                )
                                Text(
                                    courseData.courseName,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    "@" + courseData.coursePlace,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    courseData.coursePosition,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    courseData.courseTeacher,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(144.dp))
        }
    }

}