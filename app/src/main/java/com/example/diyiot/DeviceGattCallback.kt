package com.example.diyiot

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import androidx.lifecycle.MutableLiveData

class DeviceGattCallback : BluetoothGattCallback() {
    var services: MutableLiveData<MutableList<BluetoothGattService>> = MutableLiveData<MutableList<BluetoothGattService>>(mutableListOf())
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
                gatt.readCharacteristic(gatt.services[0].characteristics.get(0))
            }
            super.onServicesDiscovered(gatt, status)
        } else {
            println("onServicesDiscovered received: $status")
        }


    }

    @SuppressLint("MissingPermission")
    override fun onServiceChanged(gatt: BluetoothGatt) {
        println("Discovered services")
        if (gatt != null) {
            services.postValue(gatt.services)
            this.gatt = gatt
            gatt.readCharacteristic(gatt.services[0].characteristics.get(0))
        }
        super.onServiceChanged(gatt)
    }
}