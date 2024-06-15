package com.ch4019.jdaassist.viewmodel

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
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val loginRepository: LoginRepository): ViewModel() {
    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()

    init{
        initLoginState()
    }

    private fun initLoginState() {
        viewModelScope.launch(Dispatchers.IO) {
            val userName = loginRepository.getUserName().ifBlank { "" }
            val isLogin = loginRepository.getIsLogin()
            val passWord = loginRepository.getPassWord().ifBlank { "" }
            val cookieString = loginRepository.getCookies().ifBlank { "JSESSIONID:000000000" }
            val cookies = parseCookieString(cookieString)
            _loginState.update {
                it.copy(
                    userName = userName,
                    passWord = passWord,
                    csrfToken = "",
                    cookies = cookies,
                    isLogin = isLogin
                )
            }
        }
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
                    loginRepository.setUserName(yhm)
                    loginRepository.setPassWord(passWord)
                    loginRepository.setCookies(cookieString)
                    loginRepository.setIsLogin(true)
                    _loginState.update {
                        it.copy(
                            cookies = newCookies,
                            isLogin = true
                        )
                    }
                } else {
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
            loginRepository.setIsLogin(false)
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

    private fun getTime(): Long {
        return System.currentTimeMillis()
    }

}