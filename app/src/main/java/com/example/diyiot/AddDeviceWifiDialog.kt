package com.example.diyiot

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import java.util.UUID
import kotlin.concurrent.thread
import kotlin.math.min

@Composable
fun WifiDialog(
    onDismissRequest: () -> Unit,
    deviceConnection: BluetoothGatt?,
    context: Context,
    gattCallback: DeviceGattCallback
) {
    if (deviceConnection == null) {
        onDismissRequest()
        return
    }
    println("Services in popup: ${deviceConnection.services}")
    if (deviceConnection.services.isEmpty()) {
        onDismissRequest()
        return
    }
    var wifiList: List<android.net.wifi.ScanResult> by remember {
        mutableStateOf(listOf())
    }

    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val intentFilter = IntentFilter()
    intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
    val scanReceiver = WifiScanReceiver(wifiManager, context)
    context.registerReceiver(scanReceiver, intentFilter)

    val scanResultCallback = WifiScanResultCallback(context) { results -> wifiList = results }
    wifiManager.registerScanResultsCallback(context.mainExecutor, scanResultCallback)
    wifiManager.startScan()
    val ssidWriteUuid = UUID.fromString("937312e0-2354-11eb-9f10-fbc30a62cf39")
    val passWriteUuid = UUID.fromString("987312e0-2354-11eb-9f10-fbc30a62cf40")
    val ssidWriteChar = deviceConnection.services[0].getCharacteristic(ssidWriteUuid)
    val passWriteChar = deviceConnection.services[0].getCharacteristic(passWriteUuid)
    var getDataOnce = false
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
            val mWifiNetworks = wifiList.map { it -> it.SSID }
            val wifiExpandIcon = if (mExpanded)
                Icons.Filled.KeyboardArrowUp
            else
                Icons.Filled.KeyboardArrowDown
            val showPasswordIcon =
                if (mShowPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            if(!getDataOnce){
                getDeviceData(deviceConnection)
                getDataOnce=true
            }
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
                        .height(80.dp)
                ) {

                    if (mWifiNetworks.isEmpty()) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .width(64.dp)
                                .align(Alignment.CenterHorizontally),
                            strokeWidth = 10.dp
                        )
                    } else {
                        mWifiNetworks.forEach { label ->
                            DropdownMenuItem(onClick = {
                                mSelectedWifiText = label
                                mExpanded = false
                            }, text = { Text(text = label) })
                        }
                    }
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
//                val deviceUUID = deviceConnection.readCharacteristic()

//                while (gattCallback.deviceId.isNullOrBlank() || gattCallback.deviceSecret.isNullOrBlank()) {
//                }

                val notifyUUID = UUID.fromString("987312e0-2354-11eb-9f10-fbc30a62cf50")
                deviceConnection.setCharacteristicNotification(
                    deviceConnection.services[0].getCharacteristic(
                        notifyUUID
                    ), true
                )
                val ssidData = "....$mSelectedWifiText...."
                for (i in ssidData.indices step 20) {
                    println(i)
                    if (i >= ssidData.length) {
                        break
                    }
                    deviceConnection.writeCharacteristic(
                        ssidWriteChar,
                        ssidData.substring(i, min(i + 20, ssidData.length))
                            .encodeToByteArray(),
                        WRITE_TYPE_NO_RESPONSE
                    )
                    Thread.sleep(300)
                }
                val passData = "....$mSelectedPassText...."
                for (i in passData.indices step 20) {
                    if (i >= passData.length) {
                        break
                    }
                    deviceConnection.writeCharacteristic(
                        passWriteChar,
                        passData.substring(i, min(i + 20, passData.length))
                            .encodeToByteArray(),
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

@Composable
private fun getDeviceData(deviceConnection: BluetoothGatt) {
    val deviceIdReadUuid = UUID.fromString("00002137-0000-1000-8000-00805F9B34FB")
    val deviceSecretReadUuid = UUID.fromString("987312e0-2354-11eb-9f10-fbc30a62cf38")
    val deviceUUIDCharacteristic =
        deviceConnection.services[0].getCharacteristic(deviceIdReadUuid)
    val deviceSecretCharacteristic =
        deviceConnection.services[0].getCharacteristic(deviceSecretReadUuid)
    deviceConnection.readCharacteristic(deviceUUIDCharacteristic)
    Thread.sleep(300)
    val readSecret=deviceConnection.readCharacteristic(deviceSecretCharacteristic)
    println("Reading secret: ${readSecret}")
}
