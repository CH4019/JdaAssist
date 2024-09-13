package com.ch4019.jdaassist.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ch4019.jdaassist.util.isMonday

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(
    isOpenDialog: MutableState<Boolean>,
    onDateSelected: (Long) -> Unit,
) {
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled = remember {
            derivedStateOf {
                datePickerState.selectedDateMillis != null
            }
        }
        DatePickerDialog(
            onDismissRequest = {
                isOpenDialog.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isMonday(datePickerState.selectedDateMillis!!)) {
                            isOpenDialog.value = false
                            onDateSelected(datePickerState.selectedDateMillis!!)
                        } else {
                            Toast.makeText(context, "请选择第一周的周一", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text(text = "确认")

                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isOpenDialog.value = false
                    }
                ) {
                    Text("取消")
                }
            },
        ) {
            DatePicker(
                state = datePickerState,
            )
        }
    }
}