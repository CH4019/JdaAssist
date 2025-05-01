package com.ch4019.jdaassist.viewmodel

data class AppState(
    val isNewVision: UpdateStatus = UpdateStatus.InitState,
    val appVersion: AppVision = AppVision(),
)

data class UiPrefs(
    val welcomeDone: Boolean,
    val isDark: Boolean,
    val darkSwitchActive: Boolean,
    val maskClickX: Float,
    val maskClickY: Float
)


enum class UpdateStatus {
    InitState, Checking, Available, NotAvailable, Error
}