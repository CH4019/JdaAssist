package com.ch4019.jdaassist.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DataStoreCont.DATA_STORE_NAME)

object DataStoreCont {
    const val DATA_STORE_NAME = "Login"
    val USER_NAME = stringPreferencesKey("userName")
    val PASS_WORD = stringPreferencesKey("passWord")
    val IS_LOGIN = booleanPreferencesKey("isLogin")
    val COOKIES = stringPreferencesKey("cookies")
}

enum class MaskAnimModel {
    EXPEND,
    SHRINK,
}

val IS_DARK_MODEL = booleanPreferencesKey("is_dark_model")
val DARK_SWITCH_ACTIVE = booleanPreferencesKey("dark_switch_active")
val MASK_CLICK_X = floatPreferencesKey("mask_click_x")
val MASK_CLICK_Y = floatPreferencesKey("mask_click_y")
