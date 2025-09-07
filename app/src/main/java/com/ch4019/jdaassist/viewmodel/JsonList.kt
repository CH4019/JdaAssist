package com.ch4019.jdaassist.viewmodel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class PublicKeyList(
    val modulus: String,
    val exponent: String
)

@Serializable
data class GradesList(
    val items: List<Grade> = listOf(),
){
    @Serializable
    data class Grade(
        @SerialName("kch")
        val courseCode: String = "", // 课程代码
        @SerialName("kcmc")
        val courseName: String = "", // 课程名称
        @SerialName("kcxzmc")
        val courseNature: String = "", // 课程性质
        @SerialName("xf")
        val credits: String = "", // 学分
        @SerialName("cj")
        val grade: String = "", // 成绩
        @SerialName("jd")
        val gpa: String = "", // 绩点
        @SerialName("ksxz")
        val examNature: String = "", // 成绩性质
        @SerialName("cjsfzf")
        val isGradeInvalid: String = "", // 是否成绩作废
        @SerialName("sfxwkc")
        val isDegreeCourse: String = "", // 是否学位课程
        @SerialName("jsxm")
        val teacherName: String = "", // 任课教师
        @SerialName("khfsmc")
        val assessmentMethod: String = "", // 考核方式
        @SerialName("jxb_id")
        val courseId: String = "" // 课程ID
    )
}

@Serializable
data class GradesInfo(
    val items: GradeInfo = GradeInfo(),
){
    @Serializable
    data class GradeInfo(
        @SerialName("xmblmc")
        val courseGradeInfo: String = "", // 成绩类型
        @SerialName("xmcj")
        val courseScore: String = "", // 课程成绩
    )
}

@Serializable
data class AppVision(
    @SerialName("tag_name")
    val version: String = "",
    @SerialName("name")
    val name: String = "",
    val assets: List<Assets> = listOf(),
) {
    @Serializable
    data class Assets(
        @SerialName("size")
        val appsSize: Long = 0L,
        @SerialName("browser_download_url")
        val url: String = "",
    )
}