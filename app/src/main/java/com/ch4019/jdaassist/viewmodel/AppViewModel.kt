package com.ch4019.jdaassist.viewmodel

import android.app.Application
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ch4019.jdaassist.model.CourseResult
import com.ch4019.jdaassist.model.DARK_SWITCH_ACTIVE
import com.ch4019.jdaassist.model.IS_DARK_MODEL
import com.ch4019.jdaassist.model.MASK_CLICK_X
import com.ch4019.jdaassist.model.MASK_CLICK_Y
import com.ch4019.jdaassist.model.WELCOME_STATUS
import com.ch4019.jdaassist.util.B64
import com.ch4019.jdaassist.util.RSAEncoder
import com.ch4019.jdaassist.util.getCurrentVersionName
import com.ch4019.jdaassist.util.isNewerVersion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val application: Application,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val _loginState = MutableStateFlow(LoginState())
    private val _courseState = MutableStateFlow(CourseState())
    private val _appVisionState = MutableStateFlow(AppState())
    val loginState = _loginState.asStateFlow()
    val courseState = _courseState.asStateFlow()
    val appVisionState = _appVisionState.asStateFlow()

    val uiPrefs: StateFlow<UiPrefs> = dataStore.data
        .map { prefs ->
            UiPrefs(
                welcomeDone = prefs[WELCOME_STATUS] ?: false,
                isDark = prefs[IS_DARK_MODEL] ?: false,
                darkSwitchActive = prefs[DARK_SWITCH_ACTIVE] ?: false,
                maskClickX = prefs[MASK_CLICK_X] ?: 0f,
                maskClickY = prefs[MASK_CLICK_Y] ?: 0f
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            UiPrefs(
                false,
                isDark = false,
                darkSwitchActive = false,
                maskClickX = 0f,
                maskClickY = 0f
            )
        )


    init{
        initLoginState()
    }

    private fun initLoginState() {
        viewModelScope.launch(Dispatchers.IO) {
            val userName = appRepository.getUserName().ifBlank { "" }
            val isLogin = appRepository.getIsLogin()
            val passWord = appRepository.getPassWord().ifBlank { "" }
            val cookieString = appRepository.getCookies().ifBlank { "JSESSIONID:000000000" }
            val cookies = parseCookieString(cookieString)
            val isAutoLogin = appRepository.getIsAutoLogin()
            val openData = getData()
            val isLastOpenData = if (openData != appRepository.getLastOpenData()) {
                appRepository.setLastOpenData(openData)
                false
            } else {
                true
            }

            val courseData = appRepository.getCourseData().ifBlank { "{}" }
            val courseStartDate = appRepository.getCourseStartDate().ifBlank { "2024/07/01" }
            val json = Json { ignoreUnknownKeys = true }
            val coursesData = json.decodeFromString<CourseJsonList>(courseData)
            Log.d("data", courseData)
            Log.d("data2", coursesData.kbList.toString())
            _loginState.update {
                it.copy(
                    userName = userName,
                    passWord = passWord,
                    csrfToken = "",
                    cookies = cookies,
                    isLogin = if (isAutoLogin && !isLastOpenData) false else isLogin,
                    isAutoLogin = isAutoLogin,
                    isLastOpenData = isLastOpenData
                )
            }
            _courseState.update {
                it.copy(
                    startDate = courseStartDate,
                    courseData = coursesData.kbList
                )
            }
        }
    }

    private fun getData(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val currentDate: String = LocalDate.now().format(formatter)
        return currentDate
    }

    private fun parseCookieString(cookieString: String): Map<String, String> {
//        val cookies = mutableMapOf<String, String>()
//        val keyValue = cookieString.split("=")
//        if (keyValue.size == 2) {
//            val key = keyValue[0].trim()
//            val value = keyValue[1].trim()
//            cookies[key] = value
//        }
//        return cookies
        return cookieString.split(";")
            .map { it.trim() }
            .mapNotNull {
                val parts = it.split("=")
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            .associate { it }
    }

    suspend fun getLoginState(
        loginIntent: LoginState
    ) {
        _loginState.update {
            it.copy(
                userName = loginIntent.userName,
                passWord = loginIntent.passWord
            )
        }
        appRepository.setUserName(loginIntent.userName)
        appRepository.setPassWord(loginIntent.passWord)
        withContext(Dispatchers.IO) {
            getCsrfToken()
            val passWord = getPassWord(loginIntent.passWord)
            login(passWord, loginIntent.userName)
        }
    }

    suspend fun getGrades(
        year: String,
        semester: String
    ): Result<GradesList> = withContext(Dispatchers.IO) {
        val term = when (semester) {
            "1" -> "3"
            "2" -> "12"
            else -> "16"
        }
        val maxRetries = 3
        repeat(maxRetries) { currentRetry -> // 使用 repeat 函数简化循环
            runCatching {
                val connection =
                    Jsoup.connect("${loginState.value.url}/cjcx/cjcx_cxDgXscj.html?doType=query&gnmkdm=N305005")
                        .header(
                            "User-Agent",
                            "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Mobile Safari/537.36 Edg/115.0.1901.203"
                        )
                        .cookies(loginState.value.cookies)
                        .ignoreContentType(true)
                        .data("xnm", year)
                        .data("xqm", term)
                        .data("_search", "false")
                        .data("nd", (getTime() * 1000).toString())
                        .data("queryModel.showCount", "100")
                        .data("queryModel.currentPage", "1")
                        .data("queryModel.sortName", "")
                        .data("queryModel.sortOrder", "asc")
                        .data("time", "0")
                val response = connection.execute()
                if (response.statusCode() == 200) {
                    val responseBody = response.body()
                    val json = Json { ignoreUnknownKeys = true }
                    json.decodeFromString<GradesList>(responseBody)
                } else {
                    throw Exception("HTTP request failed with status code: ${response.statusCode()}") // 更详细的错误信息
                }
            }.onSuccess {
                return@withContext Result.success(it) // 成功获取数据，直接返回
            }.onFailure {
                if (currentRetry < maxRetries - 1) { // 还有重试机会
                    delay(1000L)
                } else {
                    return@withContext Result.failure(it) // 重试次数用尽，返回错误
                }
            }
        }
        Result.failure(Exception("Failed to retrieve data after $maxRetries retries"))
    }


    suspend fun getGradesInfo(
        year: String,
        semester: String,
        courseId: String
    ): Result<GradesInfo> = withContext(Dispatchers.IO) {
        val term = when (semester) {
            "1" -> "3"
            "2" -> "12"
            else -> "16"
        }
        val maxRetries = 3
        repeat(maxRetries) { currentRetry -> // 使用 repeat 函数简化循环
            runCatching {
                val connection =
                    Jsoup.connect("${loginState.value.url}/cjcx/cjcx_cxXsXmcjList.html?gnmkdm=N305007&su=${loginState.value.userName}")
                        .header(
                            "User-Agent",
                            "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Mobile Safari/537.36 Edg/115.0.1901.203"
                        )
                        .cookies(loginState.value.cookies)
                        .ignoreContentType(true)
                        .data("xnm", year)
                        .data("xqm", term)
                        .data("jxb_id", courseId)
                        .data("_search", "false")
                        .data("nd", (getTime() * 1000).toString())
                        .data("queryModel.showCount", "100")
                        .data("queryModel.currentPage", "1")
                        .data("queryModel.sortName", "")
                        .data("queryModel.sortOrder", "asc")
                        .data("time", "0")
                val response = connection.execute()
                if (response.statusCode() == 200) {
                    val responseBody = response.body()
                    val json = Json { ignoreUnknownKeys = true }
                    json.decodeFromString<GradesInfo>(responseBody)
                } else {
                    throw Exception("HTTP request failed with status code: ${response.statusCode()}") // 更详细的错误信息
                }
            }.onSuccess {
                return@withContext Result.success(it) // 成功获取数据，直接返回
            }.onFailure {
                if (currentRetry < maxRetries - 1) { // 还有重试机会
                    delay(1000L)
                } else {
                    return@withContext Result.failure(it) // 重试次数用尽，返回错误
                }
            }
        }
        Result.failure(Exception("Failed to retrieve data after $maxRetries retries")) // 所有重试都失败

    }

    private fun getCsrfToken() {
        runCatching {
            val connection = Jsoup.connect("${loginState.value.url}/xtgl/login_slogin.html")
            connection.header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"
            )
            val response = connection.execute()
            val document = Jsoup.parse(response.body())
            val csrfToken = document.getElementById("csrftoken")?.`val`() ?: ""
            _loginState.update {
                it.copy(
                    cookies = response.cookies(),
                    csrfToken = csrfToken
                )
            }
        }.onFailure {
            Log.d("getCsrfToken", it.message.toString())
        }
//        withContext(Dispatchers.IO) {
//            try {
//                val connection = Jsoup.connect("${loginState.value.url}/xtgl/login_slogin.html")
//                connection.header(
//                    "User-Agent",
//                    "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"
//                )
//                val response = connection.execute()
//                //保存csrfToken和cookies
//                val document = Jsoup.parse(response.body())
//                val csrfToken = document.getElementById("csrftoken")?.`val`() ?: ""
//                _loginState.update {
//                    it.copy(
//                        cookies = response.cookies(),
//                        csrfToken = csrfToken
//                    )
//                }
//            } catch (ex: Exception) {
//                ex.printStackTrace()
//            }
//        }
    }

    private fun getPassWord(
        password: String
    ): String {
        return runCatching {
            val time = getTime()
            val connection =
                Jsoup.connect("${loginState.value.url}/xtgl/login_getPublicKey.html?time=$time")
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Mobile Safari/537.36 Edg/115.0.1901.203"
                    )
                    .cookies(loginState.value.cookies)
                    .ignoreContentType(true)
            val response = connection.execute()
            val responseBody = response.body()
            val data = Json.decodeFromString<PublicKeyList>(responseBody)
            B64.hexToB64(
                RSAEncoder.rsaEncrypt(
                    password,
                    B64.b64ToHex(data.modulus),
                    B64.b64ToHex(data.exponent)
                )
            )
        }.getOrElse {
            Log.e("getPassWord", "Error encrypting password", it)
            "" // 或抛出异常
        }
//        return withContext(Dispatchers.IO) {
//            val time = getTime()
//            val connection =
//                Jsoup.connect("${loginState.value.url}/xtgl/login_getPublicKey.html?time=$time")
//                    .header(
//                        "User-Agent",
//                        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Mobile Safari/537.36 Edg/115.0.1901.203"
//                    )
//                    .cookies(loginState.value.cookies)
//                    .ignoreContentType(true)
//            val response = connection.execute()
//            val responseBody = response.body()
//            val data = Json.decodeFromString<PublicKeyList>(responseBody)
//            val passWord = B64.hexToB64(
//                RSAEncoder.rsaEncrypt(
//                    password,
//                    B64.b64ToHex(data.modulus),
//                    B64.b64ToHex(data.exponent)
//                )
//            )
//            return@withContext passWord
//        }
    }

    private suspend fun login(
        passWord: String,
        yhm: String
    ) {
        runCatching {
            val time = getTime()
            val connection =
                Jsoup.connect("${loginState.value.url}/xtgl/login_slogin.html?time=$time")
                    .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Mobile Safari/537.36 Edg/115.0.1901.203"
                    )
                    .data("csrftoken", loginState.value.csrfToken)
                    .data("yhm", yhm)
                    .data("mm", passWord)
                    .cookies(loginState.value.cookies)
                    .ignoreContentType(true)
                    .followRedirects(false)  // 禁用自动重定向
                    .method(Connection.Method.POST)
            val response = connection.execute()
            if (response.statusCode() == 302) {
                val newCookies = response.headers("Set-Cookie")
                    .mapNotNull { it.split(";").firstOrNull() }
                    .associate {
                        val parts = it.split("=")
                        parts[0] to parts[1]
                    }
                val cookieString = newCookies.entries.joinToString(";") { "${it.key}=${it.value}" }
                appRepository.setCookies(cookieString)
                appRepository.setIsLogin(true)
                _loginState.update { it.copy(cookies = newCookies, isLogin = true) }
            } else {
                _loginState.update { it.copy(isLogin = false) }
                Log.d("login", "Login failed")
            }
        }.onFailure {
            Log.e("login", "Login request failed", it)
        }
    }

    fun logout(){
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.setIsLogin(false)
            _loginState.update { it.copy(isLogin = false) }
        }
    }

    suspend fun getUserInfo(): Result<String> = withContext(Dispatchers.IO) {
        val maxRetries = 3
        repeat(maxRetries) { currentRetry ->
            runCatching {
                val connection =
                    Jsoup.connect("${loginState.value.url}/xsxxxggl/xsgrxxwh_cxXsgrxx.html?gnmkdm=N100801&layout=default&su=${loginState.value.userName}")
                        .header(
                            "Content-Type",
                            "application/x-www-form-urlencoded;charset=utf-8"
                        )
                        .header(
                            "User-Agent",
                            "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Mobile Safari/537.36 Edg/115.0.1901.203"
                        )
                        .cookies(loginState.value.cookies)
                        .ignoreContentType(true)
                val response = connection.execute()
                if (response.statusCode() == 200) {
                    val document = Jsoup.parse(response.body())
                    document.selectFirst("#col_xm .form-control-static")?.text() ?: ""
                } else {
                    throw Exception("HTTP request failed with status code: ${response.statusCode()}")
                }
            }.onSuccess {
                return@withContext Result.success(it)
            }.onFailure {
                if (currentRetry < maxRetries - 1) {
                    delay(1000L)
                } else {
                    return@withContext Result.failure(it)
                }
            }
        }
        Result.failure(Exception("Failed to retrieve data after $maxRetries retries"))
    }

//    suspend fun setCourseInfo(
//        startDate: String,
//        endDate: String,
//        isSummerTime: Boolean,
//        year: String,
//        semester: String
//    ) {
//        _courseState.update {
//            it.copy(
//                startDate = startDate,
//                endDate = endDate,
//                isSummerTime = isSummerTime
//            )
//        }
//        getCourseList(year, semester)
//    }

    suspend fun getCourseData(
        scheduleSelected: Int,
        year: String,
        semester: String,
        startDate: String,
    ) {
        val isSummerTime = scheduleSelected == 0 // 简化 isSummerTime 计算
        getCourseList(year, semester).onSuccess { course ->
            appRepository.setCourseData(course.responseBody)
            appRepository.setCourseStartDate(startDate)
            Log.d("json2", course.responseBody)
            _courseState.update {
                it.copy(
                    startDate = startDate,
                    isSummerTime = isSummerTime,
                    courseData = course.courseList.kbList
                )
            }
        }.onFailure { throwable -> // 添加 onFailure 处理错误
            Log.e("getCourseData", "Failed to get course data", throwable)
            // 可以在这里显示错误消息或执行其他错误处理操作
        }
    }


    private suspend fun getCourseList(
        year: String,
        semester: String
    ): Result<CourseResult> = withContext(Dispatchers.IO) {
        val term = when (semester) {
            "1" -> "3"
            "2" -> "12"
            in listOf("3", "12", "16") -> semester
            else -> "16"
        }
        val connection =
            Jsoup.connect("${loginState.value.url}/kbcx/xskbcx_cxXsKb.html?gnmkdm=N253508")
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Mobile Safari/537.36 Edg/115.0.1901.203"
                )
                .data("xnm", year)
                .data("xqm", term)
                .cookies(loginState.value.cookies)
                .ignoreContentType(true)
                .followRedirects(false)  // 禁用自动重定向
                .method(Connection.Method.POST)
        runCatching {
            val response = connection.execute()
            val responseBody = response.body()
            if (responseBody.isNotBlank()) {
                val json = Json { ignoreUnknownKeys = true }
                val data = json.decodeFromString<CourseJsonList>(responseBody)
                CourseResult(data, responseBody)
            } else {
                throw Exception("Response body is empty")
            }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(Exception("Failed to get course list: ${it.message}")) }
        )
    }

    suspend fun setIsAutoLogin(isAutoLogin: Boolean) {
        withContext(Dispatchers.IO) {
            appRepository.setIsAutoLogin(isAutoLogin)
            _loginState.update {
                it.copy(
                    isLogin = isAutoLogin
                )
            }
        }
    }


    fun getNewVision() {
        viewModelScope.launch {
            _appVisionState.update { it.copy(isNewVision = UpdateStatus.Checking) } // 开始检查
            try {
                val latestVersion = fetchLatestVersion()
                val currentVersionName = getCurrentVersionName(application.applicationContext)
                val isNewVersion = isNewerVersion(latestVersion.version, currentVersionName)
                _appVisionState.update {
                    it.copy(
                        isNewVision = if (isNewVersion) UpdateStatus.Available else UpdateStatus.NotAvailable,
                        appVersion = latestVersion // 假设 AppVision 有一个 version 属性
                    )
                }
            } catch (e: Exception) {
                Log.e("UpdateCheck", "Failed to fetch update information: ${e.message}", e)
                _appVisionState.update {
                    it.copy(isNewVision = UpdateStatus.Error)
                }
                // 可以在这里更新 appVisionState 以指示错误状态
            }
        }
    }

    fun closeNewVision() {
        _appVisionState.update { it.copy(isNewVision = UpdateStatus.InitState) }
    }

    private suspend fun fetchLatestVersion(): AppVision {
        val githubUrl = "https://api.github.com/repos/ch4019/JdaAssist/releases/latest"
        val httpClient = OkHttpClient()
        val request = Request.Builder()
            .url(githubUrl)
            .build()

        return withContext(Dispatchers.IO) {
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromString<AppVision>(response.body?.string() ?: "")
        }
    }

    // AppViewModel.kt
    fun toggleDarkMode() = viewModelScope.launch {
        dataStore.edit { it[IS_DARK_MODEL] = !(it[IS_DARK_MODEL] ?: false) }
    }

    fun resetDarkSwitch() = viewModelScope.launch {
        dataStore.edit { it[DARK_SWITCH_ACTIVE] = false }
    }

    fun welcomeDone() = viewModelScope.launch {
        dataStore.edit { it[WELCOME_STATUS] = true }
    }

    fun updateMaskClick(x: Float, y: Float) = viewModelScope.launch {
        dataStore.edit {
            it[MASK_CLICK_X] = x
            it[MASK_CLICK_Y] = y
        }
    }


    private fun getTime(): Long {
        return System.currentTimeMillis()
    }

}