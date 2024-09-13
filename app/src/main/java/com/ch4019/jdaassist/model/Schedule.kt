package com.ch4019.jdaassist.model

object Schedule {
    data class CourseTime(
        val name: Int,
        val startDate: String,
        val endDate: String,
    )

    val summerSchedule = listOf(
        CourseTime(1, "07:50", "08:35"),
        CourseTime(2, "08:40", "09:25"),
        CourseTime(3, "09:35", "10:20"),
        CourseTime(4, "10:25", "11:10"),
        CourseTime(5, "11:15", "12:00"),
        CourseTime(6, "14:15", "15:00"),
        CourseTime(7, "15:05", "15:50"),
        CourseTime(8, "16:00", "16:45"),
        CourseTime(9, "16:50", "17:35"),
        CourseTime(10, "18:40", "19:25"),
        CourseTime(11, "19:30", "20:15"),
        CourseTime(12, "20:20", "21:05"),
    )

    val winterSchedule = listOf(
        CourseTime(1, "07:50", "08:35"),
        CourseTime(2, "08:40", "09:25"),
        CourseTime(3, "09:35", "10:20"),
        CourseTime(4, "10:25", "11:10"),
        CourseTime(5, "11:15", "12:00"),
        CourseTime(6, "14:00", "14:45"),
        CourseTime(7, "14:50", "15:35"),
        CourseTime(8, "15:45", "16:30"),
        CourseTime(9, "16:35", "17:20"),
        CourseTime(10, "18:40", "19:25"),
        CourseTime(11, "19:30", "20:15"),
        CourseTime(12, "20:20", "21:05"),
    )
}



