package com.example.diyiot

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.ScanResultsCallback
import android.util.Log
import androidx.core.app.ActivityCompat

class WifiScanResultCallback(private var context: Context,private val setWifiResults:(List<android.net.wifi.ScanResult>)->Unit):ScanResultsCallback() {
    override fun onScanResultsAvailable() {
        // Get the scan results
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e("Permission error","No permission to scan")
            return
        }
        val results =  wifiManager.scanResults
        setWifiResults(results)
        // Process the scan results
//        for (result in results) {
//            Log.d("WifiScanner", "SSID: ${result.SSID}, BSSID: ${result.BSSID}")
//        }
    }

    fun onScanFailed() {
        // Handle scan failure
        Log.e("WifiScanner", "Scan failed")
    }

}