package com.ch4019.jdaassist.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.ch4019.jdaassist.model.WELCOME_STATUS
import com.ch4019.jdaassist.model.dataStore
import com.ch4019.jdaassist.viewmodel.AppViewModel
import kotlinx.coroutines.flow.map


@Composable
fun Welcome(
    konfettiState: MutableState<Boolean>,
    appViewModel: AppViewModel,
) {
    val context = LocalContext.current
    val privacyData = appViewModel.isAgreePrivacy.collectAsState()
    var visible by remember { mutableStateOf(!privacyData.value.isAgreePrivacy) }
    if (!visible) {
        return
    }

    val konfettiValue by context.dataStore.data.map { preferences ->
        preferences[WELCOME_STATUS] ?: false
    }.collectAsState(initial = false)

    AlertDialog(
        title = {
            Text(text = "隐私许可")
        },
        text = {

        },
        onDismissRequest = {

        },
        dismissButton = {
            TextButton(onClick = { visible = false }) {
                Text("取消")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                visible = false
                konfettiState.value = true
                appViewModel.makeAgreePrivacy(true)
            }) {
                Text("同意")
            }
        },
    )
}