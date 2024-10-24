package com.example.diyiot

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import androidx.lifecycle.MutableLiveData
import java.util.UUID

class DeviceGattCallback(private val context: Context,private val onSuccesfulRegister:()->Unit) : BluetoothGattCallback() {
    var services: MutableLiveData<MutableList<BluetoothGattService>> =
        MutableLiveData<MutableList<BluetoothGattService>>(mutableListOf())
    var gatt: BluetoothGatt? = null
    private var deviceId:String? = null
    private var deviceSecret:String? = null

    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        gatt?.discoverServices()
        super.onConnectionStateChange(gatt, status, newState)
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        println(value.decodeToString())
        if(characteristic.uuid==UUID.fromString("987312e0-2354-11eb-9f10-fbc30a62cf38")){
            println("Set secret")
            deviceSecret = String(value,Charsets.US_ASCII)
        }
        else if (characteristic.uuid == UUID.fromString("00002137-0000-1000-8000-00805F9B34FB")){
            println("Set id")
            deviceId = String(value,Charsets.US_ASCII)
        }
        super.onCharacteristicRead(gatt, characteristic, value, status)
    }

    @SuppressLint("MissingPermission")
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            println("Discovered services")
            if (gatt != null) {
                services.postValue(gatt.services)
                this.gatt = gatt
                gatt.readCharacteristic(gatt.services[0].characteristics[0])
            }
            super.onServicesDiscovered(gatt, status)
        } else {
            println("onServicesDiscovered received: $status")
        }


    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        println("Writing to characteristic: ${characteristic?.uuid}")
        super.onCharacteristicWrite(gatt, characteristic, status)
        println(status == BluetoothGatt.GATT_SUCCESS)
    }

    @SuppressLint("MissingPermission")
    override fun onServiceChanged(gatt: BluetoothGatt) {
        println("Discovered services")
        services.postValue(gatt.services)
        this.gatt = gatt
        gatt.readCharacteristic(gatt.services[0].characteristics[0])
        super.onServiceChanged(gatt)
    }

    @SuppressLint("MissingPermission")
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        val notifyUUID = UUID.fromString("987312e0-2354-11eb-9f10-fbc30a62cf50")
        if (characteristic.uuid == notifyUUID) {
            println("Notification happened")
            println(value.decodeToString())
            if (value.filter { byte -> byte != 0.toByte() }.toTypedArray().toByteArray()
                    .decodeToString() == "true"
            ) {
                gatt.writeCharacteristic(
                    characteristic,
                    "true".encodeToByteArray(),
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                )
                Thread.sleep(200)
                registerDevice(deviceId, deviceSecret, "App light", context,{onSuccesfulRegister()})
            }

        }
        super.onCharacteristicChanged(gatt, characteristic, value)
    }
}