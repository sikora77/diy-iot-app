package com.example.diyiot

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.Manifest
import android.bluetooth.BluetoothGattCharacteristic
import android.widget.ScrollView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable()
fun AddDeviceView(context: Context) {

    val bluetoothManager: BluetoothManager? =
        context.getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.getAdapter()
    if (bluetoothAdapter == null) {
        // Device doesn't support Bluetooth
        // TODO maybe actually do something if we dont have bluetooth
    }
    if (bluetoothAdapter?.isEnabled == false) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        context.startActivity(enableBtIntent)
    }

    val leScanner = bluetoothAdapter?.bluetoothLeScanner
    if (leScanner != null) {
        var builder = ScanFilter.Builder()
        // TODO Find a suitable uuid
        val filter: ScanFilter =
            builder.setServiceUuid(ParcelUuid.fromString("00001809-0000-1000-8000-00805f9b34fb"))
                .build()
        val callback = DeviceScanCallback()
        val availableDevices by callback.devicesData.observeAsState(mutableMapOf())
        var isScanning by remember {
            mutableStateOf(true)
        }
        val settingsBuilder = ScanSettings.Builder()
        CoroutineScope(Dispatchers.IO).launch {
            leScanner.startScan(listOf(filter), settingsBuilder.build(), callback)
            Thread.sleep(3 * 1000)
            leScanner.stopScan(callback)
            println("Done scanning")
            callback.devices.forEach { entry ->
                val result = entry.value
                println(result.device.name)
                println(result.device.address)
                println(result.scanRecord?.serviceUuids)
            }
            isScanning = false
        }
        val gattCallback = DeviceGattCallback()
        val services by gattCallback.services.observeAsState(mutableListOf())
        Scaffold(topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        text = "Available devices"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                })
        }
        ) {
            Column(
                modifier = Modifier.padding(it),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isScanning) {
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        CircularProgressIndicator(
                            modifier = Modifier
                                .width(64.dp),
                            strokeWidth = 10.dp,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }

                }
                if (services.isNotEmpty()) {
                    val characteristic =
                        services[0].characteristics.filter { bluetoothGattCharacteristic ->
                            bluetoothGattCharacteristic.uuid == UUID.fromString(
                                "00002137-0000-1000-8000-00805F9B34FB"
                            )
                        }[0]

                    gattCallback.gatt?.readCharacteristic(characteristic)
                }
                LazyColumn(
                    Modifier.padding(0.dp, 0.dp, 0.dp, 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (availableDevices != null) {
                        items(availableDevices.values.toMutableList()) { result ->


                            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = if (result.device.name == null) "n/a" else result.device.name,
                                    fontSize = 20.sp,
                                    modifier = Modifier
                                        .weight(2.5f)
                                        .align(Alignment.CenterVertically)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(
                                    onClick =
                                    {
                                        val connection =
                                            result.device.connectGatt(
                                                context,
                                                false,
                                                gattCallback
                                            )
                                        if (
                                            connection.connect()
                                        ) {
                                            println(connection.services)

                                        }
//                                        connection.disconnect()
                                    },
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                ) {
                                    Text("Connect", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }


        }
    } else {
        println("Scanner is null")
    }
}

@Composable()
fun PermissionAlertDialog(
    context: Context,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    hideDialog: () -> Unit
) {
    val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_CONNECT,

        )
    val singularPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        println("Permission :${it}")
    }
    AlertDialog(onDismissRequest = {},
        title = { Text("Permission") },
        text = { Text(text = "The app needs the location permission to show you the timetable for the nearest stop") },
        confirmButton = {
            Button(onClick = {
//                val newPermissions: Array<String> =
//                    permissions.filter { permission -> context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED }
//                        .toTypedArray()
//                if (newPermissions.contentEquals(
//                        arrayOf(
//                            Manifest.permission.BLUETOOTH_SCAN,
//                            Manifest.permission.BLUETOOTH_CONNECT
//                        )
//                    )
//                ) {
//                    println("Single permission")
//                    singularPermissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN)
//                } else {
                launcher.launch(permissions.toTypedArray())
//                }

                hideDialog()
            }) { Text("Give permission") }
        },
        dismissButton = {
            Button(onClick = {
                hideDialog()
            }) { Text("No") }
        })
}