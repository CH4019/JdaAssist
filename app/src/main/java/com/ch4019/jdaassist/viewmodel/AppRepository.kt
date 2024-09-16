package com.ch4019.jdaassist.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.ch4019.jdaassist.model.DataStoreCont
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    suspend fun getIsLogin() = getValue(DataStoreCont.IS_LOGIN) ?: false
    suspend fun setIsLogin(newValue: Boolean) = setValue(DataStoreCont.IS_LOGIN, newValue)
    suspend fun getUserName() = getValue(DataStoreCont.USER_NAME) ?: ""
    suspend fun setUserName(newValue: String) = setValue(DataStoreCont.USER_NAME, newValue)
    suspend fun getPassWord() = getValue(DataStoreCont.PASS_WORD) ?: ""
    suspend fun setPassWord(newValue: String) = setValue(DataStoreCont.PASS_WORD, newValue)
    suspend fun getCookies() = getValue(DataStoreCont.COOKIES) ?: ""
    suspend fun setCookies(newValue: String) = setValue(DataStoreCont.COOKIES, newValue)
    suspend fun getIsAutoLogin() = getValue(DataStoreCont.IS_AUTO_LOGIN) ?: false
    suspend fun setIsAutoLogin(newValue: Boolean) = setValue(DataStoreCont.IS_AUTO_LOGIN, newValue)
    suspend fun getLastOpenData() = getValue(DataStoreCont.lastOpenData) ?: ""
    suspend fun setLastOpenData(newValue: String) = setValue(DataStoreCont.lastOpenData, newValue)
    suspend fun getCourseData() = getValue(DataStoreCont.COURSE_DATA) ?: ""
    suspend fun setCourseData(newValue: String) = setValue(DataStoreCont.COURSE_DATA, newValue)
    suspend fun getCourseStartDate() = getValue(DataStoreCont.COURSE_START_DATE) ?: ""
    suspend fun setCourseStartDate(newValue: String) =
        setValue(DataStoreCont.COURSE_START_DATE, newValue)
    suspend fun getIsAgreePrivacy() = getValue(DataStoreCont.IS_AGREE_PRIVACY) ?: false
    suspend fun setIsAgreePrivacy(newValue: Boolean) =
        setValue(DataStoreCont.IS_AGREE_PRIVACY, newValue)

    private suspend fun <T : Any> getValue(key: Preferences.Key<T>): T? {
        return dataStore.data.map { it[key] }.first()
    }

    private suspend fun <T : Any> setValue(
        key: Preferences.Key<T>,
        value: T
    ) {
        dataStore.edit {
            it[key] = value
        }
    }
}
