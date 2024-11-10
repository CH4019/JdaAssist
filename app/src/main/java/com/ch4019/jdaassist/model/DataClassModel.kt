package com.ch4019.jdaassist.model

import com.ch4019.jdaassist.viewmodel.CourseJsonList

data class CourseResult(val courseList: CourseJsonList, val responseBody: String)

data class ClassInfo(
    val className: String = "",
    val classCredit: String = "",
    val classWeek: String = "",
    val classTime: String = "",
    val classTeacher: String = "",
    val classRoom: String = "",
)