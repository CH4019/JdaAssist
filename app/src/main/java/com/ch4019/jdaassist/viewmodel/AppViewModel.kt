package com.ch4019.jdaassist.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ch4019.jdaassist.model.CourseResult
import com.ch4019.jdaassist.util.B64
import com.ch4019.jdaassist.util.RSAEncoder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(private val appRepository: AppRepository) : ViewModel() {
    private val _loginState = MutableStateFlow(LoginState())
    private val _courseState = MutableStateFlow(CourseState())
    private val _isAgreePrivacy = MutableStateFlow(AppState())
    val loginState = _loginState.asStateFlow()
    val courseState = _courseState.asStateFlow()
    val isAgreePrivacy = _isAgreePrivacy.asStateFlow()

    init{
        initLoginState()
    }

    private fun initLoginState() {
        viewModelScope.launch(Dispatchers.IO) {

            val isAgreePrivacy = appRepository.getIsAgreePrivacy()
            _isAgreePrivacy.update {
                it.copy(
                    isAgreePrivacy = isAgreePrivacy
                )
            }

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

    fun makeAgreePrivacy(
        isAgree: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isAgreePrivacy.update {
                it.copy(
                    isAgreePrivacy = isAgree
                )
            }
            appRepository.setIsAgreePrivacy(isAgree)
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

//        withContext(Dispatchers.IO) {
//            val time = getTime()
//            val connection =
//                Jsoup.connect("${loginState.value.url}/xtgl/login_slogin.html?time=$time")
//                    .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
//                    .header(
//                        "User-Agent",
//                        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Mobile Safari/537.36 Edg/115.0.1901.203"
//                    )
//                    .data("csrftoken", loginState.value.csrfToken)
//                    .data("yhm", yhm)
//                    .data("mm", passWord)
//                    .cookies(loginState.value.cookies)
//                    .ignoreContentType(true)
//                    .followRedirects(false)  // 禁用自动重定向
//                    .method(Connection.Method.POST)
//
//            try {
//                val response = connection.execute()
//                if (response.statusCode() == 302) {
//                    val setCookie = response.headers("Set-Cookie")
//                    val jsEsSionId = setCookie.map { it.split(";")[0] }
//                        .firstOrNull { it.startsWith("JSESSIONID=") }
//                        ?.substringAfter("JSESSIONID=")
//                    val newCookies = mutableMapOf<String, String>()
//                    newCookies["JSESSIONID"] = jsEsSionId ?: ""
//                    val cookieString = "JSESSIONID=$jsEsSionId"
//                    appRepository.setCookies(cookieString)
//                    appRepository.setIsLogin(true)
//                    _loginState.update {
//                        it.copy(
//                            cookies = newCookies,
//                            isLogin = true
//                        )
//                    }
//                } else {
//                    _loginState.update {
//                        it.copy(
//                            isLogin = false
//                        )
//                    }
//                    println("登录失败")
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                println("Login request failed: ${e.message}")
//            }
//        }
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

    suspend fun setCourseInfo(
        startDate: String,
        endDate: String,
        isSummerTime: Boolean,
        year: String,
        semester: String
    ) {
        _courseState.update {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                isSummerTime = isSummerTime
            )
        }
        getCourseList(year, semester)
    }

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

    private fun getTime(): Long {
        return System.currentTimeMillis()
    }

}