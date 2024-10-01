package com.ch4019.jdaassist.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ChipList(
    selected: MutableState<Boolean>,
    text: String,
    menuItems: List<String>,
    onItemSelected: (String) -> Unit
) {
    val toSelect = remember{ mutableStateOf(false) }
    val label = remember{ mutableStateOf(text) }
    Box(
        modifier = Modifier
            .wrapContentSize(align = Alignment.Center)
    ) {
        InputChip(
            selected = selected.value,
            onClick = {toSelect.value = !toSelect.value},
            label = { Text(label.value) },
            leadingIcon = {
                if (selected.value) {
                    Icon(imageVector = Icons.Rounded.Check, contentDescription = label.value)
                } else {
                    Icon(imageVector = Icons.Rounded.AccessTime, contentDescription = label.value)
                }
            },
            trailingIcon = { Icon(imageVector = Icons.Rounded.ArrowDropDown, null) },
        )
        DropdownMenu(
            expanded = toSelect.value,
            onDismissRequest = {
                toSelect.value = !toSelect.value
            },
        ) {
            menuItems.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        selected.value = true
                        label.value = it
                        toSelect.value = !toSelect.value
                        onItemSelected(it)
                    }
                )
            }
        }
    }
}