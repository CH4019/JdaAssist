package com.ch4019.jdaassist.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private fun parseCookieString(cookieString: String): MutableMap<String, String> {
        val cookies = mutableMapOf<String, String>()
        val keyValue = cookieString.split("=")
        if (keyValue.size == 2) {
            val key = keyValue[0].trim()
            val value = keyValue[1].trim()
            cookies[key] = value
        }
        return cookies
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
    ): Result<GradesList> {
        return withContext(Dispatchers.IO) {
            val term = when (semester) {
                "1" -> "3"
                "2" -> "12"
                else -> "16"
            }
            // 定义最大重试次数
            val maxRetries = 3
            var currentRetry = 0
            var getGrades: GradesList?
            while (currentRetry < maxRetries) {
                try {
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
                    if (response.statusCode() == 200){
                        val responseBody = response.body()
                        val json = Json { ignoreUnknownKeys = true }
                        getGrades = json.decodeFromString<GradesList>(responseBody)
                        return@withContext Result.success(getGrades)
                    }
                } catch (e: Exception) {
                    currentRetry++
                    if (currentRetry < maxRetries) {
                        delay(1000L)
                    } else {
                        return@withContext Result.failure(e)
                    }
                }
            }
            Result.failure(Exception("Failed to retrieve data after $maxRetries retries"))
        }
    }


    suspend fun getGradesInfo(
        year: String,
        semester: String,
        courseId: String
    ): Result<GradesInfo> {
        return withContext(Dispatchers.IO) {
            val term = when (semester) {
                "1" -> "3"
                "2" -> "12"
                else -> "16"
            }
            // 定义最大重试次数
            val maxRetries = 3
            var currentRetry = 0
            var gradesInfo: GradesInfo?
            while (currentRetry < maxRetries) {
                try {
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
                        gradesInfo = json.decodeFromString<GradesInfo>(responseBody)
                        return@withContext Result.success(gradesInfo)
                    }
                } catch (e: Exception) {
                    currentRetry++
                    if (currentRetry < maxRetries) {
                        delay(1000L)
                    } else {
                        return@withContext Result.failure(e)
                    }
                }
            }
            Result.failure(Exception("Failed to retrieve data after $maxRetries retries"))
        }
    }

    private suspend fun getCsrfToken() {
        withContext(Dispatchers.IO) {
            try {
                val connection = Jsoup.connect("${loginState.value.url}/xtgl/login_slogin.html")
                connection.header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"
                )
                val response = connection.execute()
                //保存csrfToken和cookies
                val document = Jsoup.parse(response.body())
                val csrfToken = document.getElementById("csrftoken")?.`val`() ?: ""
                _loginState.update {
                    it.copy(
                        cookies = response.cookies(),
                        csrfToken = csrfToken
                    )
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private suspend fun getPassWord(
        password: String
    ): String {
        return withContext(Dispatchers.IO) {
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
            val passWord = B64.hexToB64(
                RSAEncoder.rsaEncrypt(
                    password,
                    B64.b64ToHex(data.modulus),
                    B64.b64ToHex(data.exponent)
                )
            )
            return@withContext passWord
        }
    }

    private suspend fun login(
        passWord: String,
        yhm: String
    ) {
        withContext(Dispatchers.IO) {
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

            try {
                val response = connection.execute()
                if (response.statusCode() == 302) {
                    val setCookie = response.headers("Set-Cookie")
                    val jsEsSionId = setCookie.map { it.split(";")[0] }
                        .firstOrNull { it.startsWith("JSESSIONID=") }
                        ?.substringAfter("JSESSIONID=")
                    val newCookies = mutableMapOf<String, String>()
                    newCookies["JSESSIONID"] = jsEsSionId ?: ""
                    val cookieString = "JSESSIONID=$jsEsSionId"
                    appRepository.setCookies(cookieString)
                    appRepository.setIsLogin(true)
                    _loginState.update {
                        it.copy(
                            cookies = newCookies,
                            isLogin = true
                        )
                    }
                } else {
                    _loginState.update {
                        it.copy(
                            isLogin = false
                        )
                    }
                    println("登录失败")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Login request failed: ${e.message}")
            }
        }
    }

    fun logout(){
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.setIsLogin(false)
            _loginState.update { it.copy(isLogin = false) }
        }
    }

    suspend fun getUserInfo(): Result<String>{
        return withContext(Dispatchers.IO) {
            // 定义最大重试次数
            val maxRetries = 3
            var currentRetry = 0
            var name: String?
            while (currentRetry < maxRetries) {
                try {
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
                    if(response.statusCode() == 200){
                        val document = Jsoup.parse(response.body())
                        name = document.selectFirst("#col_xm .form-control-static")?.text() ?: ""
                        return@withContext Result.success(name)
                    }
                } catch (e: Exception) {
                    currentRetry++
                    if (currentRetry < maxRetries) {
                        delay(1000L)
                    } else {
                        return@withContext Result.failure(e)
                    }
                }
            }
            Result.failure(Exception("Failed to retrieve data after $maxRetries retries"))
        }
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
        val isSummerTime = when (scheduleSelected) {
            0 -> true
            else -> false
        }
        val result = getCourseList(year, semester)
        result.onSuccess { course ->
            appRepository.setCourseData(course.second)
            appRepository.setCourseStartDate(startDate)
            Log.d("json2", course.second)
            _courseState.update {
                it.copy(
                    startDate = startDate,
                    isSummerTime = isSummerTime,
                    courseData = course.first.kbList
                )
            }
        }
    }


    private suspend fun getCourseList(
        year: String,
        semester: String
    ): Result<Pair<CourseJsonList, String>> {
        return withContext(Dispatchers.IO) {
            val term = when (semester) {
                "1" -> "3"
                "2" -> "12"
                else -> "16"
            }

            val connection =
                Jsoup.connect("https://219-231-0-156.webvpn.ahjzu.edu.cn/kbcx/xskbcx_cxXsKb.html?gnmkdm=N253508")
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
            try {
                val response = connection.execute()
                val responseBody = response.body()
                if (responseBody.isNotBlank()) {
                    val json = Json { ignoreUnknownKeys = true }
                    val data = json.decodeFromString<CourseJsonList>(responseBody)
                    val result = Pair<CourseJsonList, String>(data, responseBody)
                    return@withContext Result.success(result)
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
            return@withContext Result.failure(Exception("Failed to retrieve data after  retries"))
        }
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