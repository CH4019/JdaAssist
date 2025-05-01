package com.ch4019.jdaassist.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DataStoreCont.DATA_STORE_NAME)

object DataStoreCont {
    const val DATA_STORE_NAME = "Login"
    val USER_NAME = stringPreferencesKey("userName")
    val PASS_WORD = stringPreferencesKey("passWord")
    val IS_LOGIN = booleanPreferencesKey("isLogin")
    val COOKIES = stringPreferencesKey("cookies")
    val IS_AUTO_LOGIN = booleanPreferencesKey("isAutoLogin")
    val lastOpenData = stringPreferencesKey("lastOpenData")
    val COURSE_DATA = stringPreferencesKey("courseData")
    val COURSE_START_DATE = stringPreferencesKey("courseStartDate")
}

enum class MaskAnimModel {
    EXPEND,
    SHRINK,
}

val IS_DARK_MODEL = booleanPreferencesKey("is_dark_model")
val DARK_SWITCH_ACTIVE = booleanPreferencesKey("dark_switch_active")
val MASK_CLICK_X = floatPreferencesKey("mask_click_x")
val MASK_CLICK_Y = floatPreferencesKey("mask_click_y")


val WELCOME_STATUS = booleanPreferencesKey("key_welcome_status")