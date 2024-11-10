package com.ch4019.jdaassist.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.FloatRange
import androidx.core.graphics.ColorUtils
import com.ch4019.jdaassist.viewmodel.CourseJsonList
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.min

fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int = 0): PackageInfo =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
    } else {
        getPackageInfo(packageName, flags)
    }

fun getToDayDate(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    val currentDate: String = LocalDate.now().format(formatter)
    return currentDate
}

fun getWeekCourse(
    week: Int,
    courseData: CourseJsonList
): List<CourseJsonList.CourseData> {
    return courseData.kbList.filter { coursesData ->
        val weeks = coursesData.courseWeek
        val cleanedWeeks = weeks.removeSuffix("周")
        val weekRanges = cleanedWeeks.split(",").mapNotNull { range ->
            when {
                range.contains("-") -> { // 处理周范围
                    val (start, end) = range.split("-").map { it.toIntOrNull() }
                    if (start != null && end != null) start..end else null
                }

                range.isNotEmpty() -> { // 处理单一周
                    range.toIntOrNull()?.let { it..it }
                }

                else -> null // 忽略空字符串
            }
        }
        weekRanges.any { week in it }
//        val (startWeek, endWeek) = cleanedWeeks.split("-").map { it.toInt() }
//        // 判断目标周是否在课程的周数范围内
//        week in startWeek..endWeek
    }
}

// 计算两个日期之间的周数
fun getWeekSince(startDate: Calendar, endDate: Calendar): Int {
    val diffMillis = endDate.timeInMillis - startDate.timeInMillis
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
    return (diffDays / 7).toInt()
}


fun Int.blend(
    color: Int,
    @FloatRange(from = 0.0, to = 1.0) fraction: Float = 0.5f,
): Int = ColorUtils.blendARGB(this, color, fraction)

fun calculateDateDifference(date1: Calendar, date2: Calendar): Long {
    val diffInMillis = date2.timeInMillis - date1.timeInMillis
    return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
}

// 用于判断所选日期是否为周一
fun isMonday(dateInMillis: Long): Boolean {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = dateInMillis
    }
    return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
}

fun openWebPage(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

// 获取软件当前 versionName
fun getCurrentVersionName(context: Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName.toString()
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e("UpdateCheck", "Failed to get current version name", e)
        "" // 或其他默认值
    }
}

// 比较版本号（之前提供的代码）
fun isNewerVersion(latestVersion: String, currentVersion: String): Boolean {
    val latestParts = latestVersion.split(".").map { it.toIntOrNull() ?: 0 }
    val currentParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }

    val minLength = min(latestParts.size, currentParts.size) // 使用 minLength

    for (i in 0 until minLength) { // 循环到 minLength
        val latestPart = latestParts[i]
        val currentPart = currentParts[i]

        if (latestPart > currentPart) {
            return true // 前面部分较大，则认为整个版本号较大
        } else if (latestPart < currentPart) {
            return false
        }
    }

    // 如果前面部分都相等，则比较长度
    return latestParts.size > currentParts.size
}


fun bytesToMb(bytes: Long): String {
    val mb = bytes.toDouble() / (1024 * 1024)
    return String.format(Locale.getDefault(), "%.2f", mb) // 保留两位小数
}
