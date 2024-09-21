package com.example.diyiot

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.min

@Composable
fun WifiDialog(onDismissRequest: () -> Unit, deviceConnection: BluetoothGatt?, context: Context) {
    if (deviceConnection == null) {
        onDismissRequest()
        return
    }
    println("Services in popup: ${deviceConnection.services}")
    if (deviceConnection.services.isEmpty()) {
        onDismissRequest()
        return
    }
    val ssidWriteUuid = UUID.fromString("937312e0-2354-11eb-9f10-fbc30a62cf39")
    val passWriteUuid = UUID.fromString("987312e0-2354-11eb-9f10-fbc30a62cf40")
    val ssidWriteChar = deviceConnection.services[0].getCharacteristic(ssidWriteUuid)
    val passWriteChar = deviceConnection.services[0].getCharacteristic(passWriteUuid)
    if (ssidWriteChar == null || passWriteChar == null) {
        println("A characteristic is null")
        deviceConnection.services[0].characteristics.forEach { characteristic ->
            if (characteristic.uuid == passWriteUuid) {
                print("The password one: ")
            } else if (characteristic.uuid == ssidWriteUuid) {
                print("The ssid one: ")
            }
            println(characteristic.uuid)
        }
        onDismissRequest()
        return

    }
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {

            var mExpanded by remember {
                mutableStateOf(false)
            }
            var mShowPassword by remember {
                mutableStateOf(false)
            }
            var mSelectedWifiText by remember { mutableStateOf("") }
            var mSelectedPassText by remember { mutableStateOf("") }

            var mTextFieldSize by remember { mutableStateOf(Size.Zero) }
            val mWifiNetworks = listOf("hakuna matata", "hakuna matata EXT")
            val wifiExpandIcon = if (mExpanded)
                Icons.Filled.KeyboardArrowUp
            else
                Icons.Filled.KeyboardArrowDown
            val showPasswordIcon =
                if (mShowPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff

            Column(Modifier.padding(20.dp)) {
                OutlinedTextField(
                    value = mSelectedWifiText,
                    onValueChange = { mSelectedWifiText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            // This value is used to assign to
                            // the DropDown the same width
                            mTextFieldSize = coordinates.size.toSize()
                        },
                    label = { Text("WiFi name") },
                    trailingIcon = {
                        Icon(wifiExpandIcon, "contentDescription",
                            Modifier.clickable { mExpanded = !mExpanded })
                    }
                )
                DropdownMenu(
                    expanded = mExpanded,
                    onDismissRequest = { mExpanded = false },
                    modifier = Modifier
                        .width(with(LocalDensity.current) { mTextFieldSize.width.toDp() })
                ) {
                    mWifiNetworks.forEach { label ->
                        DropdownMenuItem(onClick = {
                            mSelectedWifiText = label
                            mExpanded = false
                        }, text = { Text(text = label) })
                    }
                }
                OutlinedTextField(
                    value = mSelectedPassText,
                    onValueChange = { mSelectedPassText = it },
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = { Text("Password") },
                    trailingIcon = {
                        Icon(showPasswordIcon, "show password",
                            Modifier.clickable { mShowPassword = !mShowPassword })
                    }
                )
                Button(onClick = {
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
                        return@Button
                    }
                    val notifyUUID = UUID.fromString("987312e0-2354-11eb-9f10-fbc30a62cf50")
                    deviceConnection.setCharacteristicNotification(
                        deviceConnection.services[0].getCharacteristic(
                            notifyUUID
                        ), true
                    )
                    val ssidData = "....$mSelectedWifiText...."
                    for (i in ssidData.indices step 20) {
                        if (i >= ssidData.length) {
                            break
                        }
                        deviceConnection.writeCharacteristic(
                            ssidWriteChar,
                            ssidData.substring(i, min(i + 20, ssidData.length)).encodeToByteArray(),
                            WRITE_TYPE_NO_RESPONSE
                        )
                        Thread.sleep(200)
                    }
                    val passData = "....$mSelectedPassText...."
                    for (i in passData.indices step 20) {
                        if (i >= passData.length) {
                            break
                        }
                        deviceConnection.writeCharacteristic(
                            passWriteChar,
                            passData.substring(i, min(i + 20, passData.length)).encodeToByteArray(),
                            WRITE_TYPE_NO_RESPONSE
                        )
                        Thread.sleep(200)
                    }
                    // TODO Maybe close the popup and read the device secret and create the device
                }) {
                    Text("Connect")
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable()
fun AddDeviceView(context: Context) {
    var scanOnce by remember { mutableStateOf(false) }

    var showWifiDialog by remember { mutableStateOf(false) }
    val bluetoothManager: BluetoothManager? =
        context.getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    var deviceConnection: BluetoothGatt? by remember {
        mutableStateOf(null)
    }
    if (showWifiDialog) {
        WifiDialog({ showWifiDialog = false }, deviceConnection, context)
    }
    var availableDevices:MutableMap<String, ScanResult> by remember { mutableStateOf(mutableMapOf()) }
    val callback = DeviceScanCallback { devices -> availableDevices = devices }

//    var availableDevices:MutableMap<String,ScanResult> by remember {
//        mutableStateOf(mutableMapOf())
//    }

    var isScanning by remember {
        mutableStateOf(true)
    }
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
//                return
        }
        context.startActivity(enableBtIntent)
    }

    val leScanner = bluetoothAdapter?.bluetoothLeScanner
    if (leScanner != null) {
        val builder = ScanFilter.Builder()
        // TODO Find a suitable uuid
        val filter: ScanFilter =
            builder.setServiceUuid(ParcelUuid.fromString("00001809-0000-1000-8000-00805f9b34fb"))
                .build()

        val settingsBuilder = ScanSettings.Builder()
        if (!scanOnce) {
            println("Begin scan")
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    scanOnce = true
                }
                println("Scanning")
                leScanner.startScan(listOf(filter), settingsBuilder.build(), callback)
                Thread.sleep(5 * 1000)
                leScanner.stopScan(callback)
                println("Done scanning")
                withContext(Dispatchers.Main) {
                    callback.devicesData.value?.forEach { entry ->
                        val result = entry.value
                        println(result.device.name)
                        println(result.device.address)
                        println(result.scanRecord?.serviceUuids)
                    }
                    println(availableDevices)
                    isScanning = false
                }

            }
        }
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
                    text = "Available devices: ${availableDevices.size}"
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
                if (availableDevices.isNotEmpty()) {
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
                                            true,
                                            gattCallback
                                        )
                                    while (connection.services.isEmpty()) {
                                    }
                                    println(connection.services)
                                    deviceConnection = connection

                                    showWifiDialog = true
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