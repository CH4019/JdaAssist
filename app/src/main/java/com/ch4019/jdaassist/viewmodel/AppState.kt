package com.ch4019.jdaassist.viewmodel

data class AppState(
    val isAgreePrivacy: Boolean = false,
    val isNewVision: UpdateStatus = UpdateStatus.NotAvailable,
    val appVersion: AppVision = AppVision(),
)

enum class UpdateStatus {
    Checking, Available, NotAvailable, Error
}