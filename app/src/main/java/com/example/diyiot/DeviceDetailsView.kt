package com.example.diyiot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailsView(
    device: Light,
    cookie: String
) {
    var deviceBrightness: Float by remember {
        mutableStateOf(device.brightness.toFloat())
    }
    var menuExpanded by remember {
        mutableStateOf(false)
    }
    val openDialog = remember { mutableStateOf(false) }
    var deviceName by remember {
        mutableStateOf(device.name)
    }
    var newDeviceName by remember {
        mutableStateOf("")
    }
    if (openDialog.value) {
        BasicAlertDialog(
            onDismissRequest = {
                openDialog.value = false
            }
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Rename device",
                        modifier = Modifier.align(Alignment.Start),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(8.dp, 0.dp, 8.dp, 0.dp)
                            .fillMaxWidth(0.8f),
                        value = newDeviceName,
                        onValueChange = { newDeviceName = it },
                        label = { Text("New device name") },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {

                            CoroutineScope(Dispatchers.IO).launch {
                                deviceName = device.setNewName(cookie, newDeviceName) ?: "Device"
                            }
                            openDialog.value = false
                            menuExpanded = false
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        text = deviceName
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
                actions = {
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Rename device") },
                            onClick = { openDialog.value = true })
                    }
                    IconButton(onClick = { menuExpanded = !menuExpanded }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Localized description"
                        )
                    }
                },

                )
        },

        ) {
        if (device != null) {
            Column(modifier = Modifier.padding(it)) {
                Row(
                    modifier = Modifier.fillMaxWidth(1f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Slider(
                        value = deviceBrightness,
                        modifier = Modifier.fillMaxWidth(0.8f),
                        onValueChangeFinished = ({
                            device.setBrightness(cookie, deviceBrightness.toInt())
                            println("Change brightness")
                        }),
                        onValueChange = ({
                            deviceBrightness = it
                        }),
                        valueRange = 0f..255f,
                    )
                }
            }
        }
    }
}