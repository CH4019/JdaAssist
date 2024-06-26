package com.ch4019.jdaassist.viewmodel

data class LoginState(
    val userName : String = "",
    val passWord : String = "",
    val url : String = "https://219-231-0-156.webvpn.ahjzu.edu.cn",
    val csrfToken : String = "",
    val cookies: MutableMap<String, String> = mutableMapOf(),
    val isLogin: Boolean = false,
    val isAutoLogin: Boolean = false,
    val isLastOpenData: Boolean = true
)