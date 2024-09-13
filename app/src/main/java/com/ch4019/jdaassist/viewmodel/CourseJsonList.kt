package com.ch4019.jdaassist.viewmodel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseJsonList(
    val kbList: List<CourseData> = listOf()
) {
    @Serializable
    data class CourseData(
        @SerialName("kcmc")
        val courseName: String = "",//课程名称
        @SerialName("jcs")
        val courseTime: String = "",//课程节次
        @SerialName("xqjmc")
        val courseWeekName: String = "",//课程星期
        @SerialName("zcd")
        val courseWeek: String = "",//课程周次
        @SerialName("xqmc")
        val coursePlace: String = "",//校区
        @SerialName("cdmc")
        val coursePosition: String = "",//课程地点
        @SerialName("xm")
        val courseTeacher: String = "",//授课教师
        @SerialName("zcmc")
        val teacherTitle: String = "",//教师职称
        @SerialName("xf")
        val courseCredit: String = "",//学分
    )
}