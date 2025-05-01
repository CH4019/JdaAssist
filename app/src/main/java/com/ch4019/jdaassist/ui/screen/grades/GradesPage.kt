package com.ch4019.jdaassist.ui.screen.grades

import android.view.MotionEvent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ch4019.jdaassist.ui.components.ChipList
import com.ch4019.jdaassist.viewmodel.AppViewModel
import com.ch4019.jdaassist.viewmodel.GradesInfo
import com.ch4019.jdaassist.viewmodel.GradesList
import kotlinx.coroutines.delay
import java.time.Year

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesPage(
    appViewModel: AppViewModel
) {
    var academicYear by remember{ mutableStateOf("2023") }
    var semester by remember{ mutableStateOf("1") }
    var gradesList by remember { mutableStateOf(GradesList()) }
    var isLoading by remember { mutableStateOf(false) }
    var gradesInfo by remember { mutableStateOf(GradesInfo()) }
    val selected1 = remember { mutableStateOf(false) }
    val selected2 = remember { mutableStateOf(false) }
    val isShowClassInfo = remember { mutableStateOf(false) }
    val courseId = remember { mutableStateOf("") }
    val isShowDialog = remember { mutableStateOf(false) }
    var isGetGrades by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val xOffsetPx = with(LocalDensity.current) { 32.dp.toPx().toInt() }

    LaunchedEffect(isShowClassInfo.value) {
        if (isShowClassInfo.value) {
            val result = appViewModel.getGradesInfo(academicYear, semester, courseId.value)
            if (result.isSuccess) {
                gradesInfo = result.getOrNull()!!
                isShowDialog.value = true // 只有成功获取信息后才显示弹窗
            } else {
                // Handle error here, e.g., show a Toast or Snackbar
            }
            isShowClassInfo.value = false // 重置isShowClassInfo状态
        }
    }

    LaunchedEffect(isGetGrades) {
        if (isGetGrades) {
            isLoading = true
            val result = appViewModel.getGrades(academicYear, semester)
            delay(500)
            if (result.isSuccess) {
                gradesList = result.getOrNull()!!
                isLoading = false
            } else {
                isLoading = false
            }
            isGetGrades = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            SelectGrades(
                selected1,
                selected2,
                onYearSelected = { year -> academicYear = year },
                onSemesterSelected = { term -> semester = term }
            )
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
            ) {
                if (gradesList.items.isNotEmpty()){
                    ShowDemo(
                        gradesList,
                        isShowClassInfo,
                        onIdSelected = {
                            courseId.value= it
                        }
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = !scrollState.isScrollInProgress,
            enter = slideInHorizontally (
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                initialOffsetX = { xOffsetPx },
                ),
            exit = slideOutHorizontally(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                targetOffsetX = { xOffsetPx },
            ),
        ) {
            FloatButton(selected1.value, selected2.value) {
                isGetGrades = true
            }
        }
        // 加载指示器
        if (isLoading) {
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
                            "正在请求数据",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.height(16.dp))
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth(),
                            strokeCap = StrokeCap.Round
                        )
                        Spacer(Modifier.height(16.dp))
//                        CircularProgressIndicator(
//                            modifier = Modifier
//                                .size(48.dp)
//                        )
                    }
                }
            }
        }
        if(isShowDialog.value) {
            BasicAlertDialog(
                onDismissRequest = {
                    isShowDialog.value = false
                },
            ) {
                Card(
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.weight(1f))
                        gradesInfo.items.forEachIndexed { index,item->
                            Column {
                                Text(
                                    item.courseGradeInfo,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(item.courseScore)
                            }
                            if(
                                index != gradesInfo.items.size-1
                            ) {
                                Spacer(Modifier.weight(1f))
                                VerticalDivider(
                                    thickness = 2.dp,
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(36.dp)
                                )
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    FilledTonalButton(
                        onClick = {
                            isShowDialog.value = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("确认")
                    }
                }
            }
        }
    }
}


@Composable
fun SelectGrades(
    selected1: MutableState<Boolean>,
    selected2: MutableState<Boolean>,
    onYearSelected: (String) -> Unit,
    onSemesterSelected: (String) -> Unit
) {
    val currentYear = Year.now().value
    val recentYears = mutableListOf<String>()
    for (i in 4 downTo 0) {
        recentYears.add("${currentYear -i}-${currentYear -i+1}")
    }
    val text1 = remember{ mutableStateOf("选择学年") }
    val text2 = remember{ mutableStateOf("选择学期") }
    val menuItems2 = listOf("1", "2")

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))
        ChipList(selected1, text1.value, recentYears) {
            onYearSelected(it.split("-")[0])
        }
        Spacer(modifier = Modifier.weight(1f))
        ChipList(selected2, text2.value, menuItems2){
            onSemesterSelected(it)
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ShowDemo(
    gradesList: GradesList,
    isShowClassInfo:  MutableState<Boolean>,
    onIdSelected: (String) -> Unit,
) {
    gradesList.items.forEachIndexed {index,it->
        Surface(
            onClick = {
                isShowClassInfo.value = true
                onIdSelected(it.courseId)
            },
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = it.courseCode,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "成绩/绩点",
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = it.courseName,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = it.teacherName,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "${it.grade} / ${it.gpa}",
                                fontSize = 24.sp,
                                color = if (it.gpa == "0.00") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            if (it.isGradeInvalid == "是") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "成绩作废",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (it.isDegreeCourse == "是") {
                            Text(
                                text = "${it.courseNature} • 学位课程 • ${it.credits}学分 • ${it.assessmentMethod}",
                                fontSize = 12.sp
                            )
                        } else {
                            Text(
                                text = "${it.courseNature} • 非学位课程 • ${it.credits}学分 • ${it.assessmentMethod}",
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = it.examNature,
                            fontSize = 12.sp
                        )
                    }
                }
                if (index != gradesList.items.size-1) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FloatButton(
    selected1: Boolean,
    selected2: Boolean,
    content: () -> Unit
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    //                        定义一个用于控制缩放状态的变量
    var isPressed by remember { mutableStateOf(false) }
//                        使用 animateFloatAsState 动态控制缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f, // 按下时缩小到0.95f，松开时恢复到1f
        animationSpec = tween(durationMillis = 150) // 动画持续时间
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 32.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            modifier = Modifier
                .pointerInteropFilter { motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            isPressed = true // 按下时缩小
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            true
                        }

                        MotionEvent.ACTION_UP -> {
                            isPressed = false // 松开时恢复
                            if (selected1 && selected2) {
                                content()
                            } else if (selected1) {
                                Toast.makeText(context, "请选择学期", Toast.LENGTH_SHORT).show()
                            } else if (selected2) {
                                Toast.makeText(context, "请选择学年", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "请选择学年和学期", Toast.LENGTH_SHORT)
                                    .show()
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
//                if (selected1 && selected2) {
//                    content()
//                } else if (selected1) {
//                    Toast.makeText(context, "请选择学期", Toast.LENGTH_SHORT).show()
//                } else if (selected2) {
//                    Toast.makeText(context, "请选择学年", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(context, "请选择学年和学期", Toast.LENGTH_SHORT).show()
//                }
            }
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
        }
    }
}