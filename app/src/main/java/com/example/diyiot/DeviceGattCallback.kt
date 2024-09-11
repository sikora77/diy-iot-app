package com.example.diyiot

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import androidx.lifecycle.MutableLiveData
import java.util.UUID

class DeviceGattCallback : BluetoothGattCallback() {
    var services: MutableLiveData<MutableList<BluetoothGattService>> =
        MutableLiveData<MutableList<BluetoothGattService>>(mutableListOf())
    var gatt: BluetoothGatt? = null

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
            }

        }
        super.onCharacteristicChanged(gatt, characteristic, value)
    }
}