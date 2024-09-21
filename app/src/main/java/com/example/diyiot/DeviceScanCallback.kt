package com.example.diyiot

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.lifecycle.MutableLiveData

class DeviceScanCallback(var updateDevices: (MutableMap<String, ScanResult> ) -> Unit) : ScanCallback() {

    var devicesData = MutableLiveData<MutableMap<String, ScanResult>>()
    var devices: MutableMap<String, ScanResult> = mutableMapOf()
    @SuppressLint("MissingPermission")
    public override fun onScanResult(callbackType: Int, result: ScanResult) {
        devices[result.device.address] = result
        this.updateDevices(devices)
        super.onScanResult(callbackType, result)
    }

    public override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        super.onBatchScanResults(results)

    }
}