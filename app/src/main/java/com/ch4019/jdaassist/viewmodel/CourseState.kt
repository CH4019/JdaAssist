package com.ch4019.jdaassist.viewmodel

data class CourseState(
    val startDate: String = "2024/07/01",
    val endDate: String = "",
    val isSummerTime: Boolean = true,
    val courseData: List<CourseJsonList.CourseData> = listOf()
)
