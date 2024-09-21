package com.example.diyiot

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

fun <T> makePairsFromList(list: List<T>?): List<Pair<T, T?>>? {
    if (list == null) {
        return null;
    }
    val resultList = mutableListOf<Pair<T, T?>>();
    for (i in list.indices step 2) {
        resultList.add(
            Pair(
                list[i],
                if (list.size > i + 1) list[i + 1] else null
            )
        )
    }
    return resultList
}

@Composable
fun DeviceView(
    onNavigateToDetails: (String) -> Unit,
    onNavigateToNewDevice: () -> Unit,
    context: Context,
    device: MutableLiveData<Light>
) {
//    val devicesList = MutableLiveData<List<Pair<Light, Light?>>>()
    var devices:List<Pair<Light, Light?>>? by remember {
        mutableStateOf(null)
    }
    var cookie by remember {
        mutableStateOf("")
    }
    LaunchedEffect(Unit) {
        println("Fetching devices")
        val client = OkHttpClient()
        cookie = getCookie(context).toString()
        val request = Request.Builder().url("http://frog01.mikr.us:22070/api/v1/full_devices")
            .header("Cookie", cookie)
            .get().build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle this
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.code == 200) {
                    val gson = Gson()
                    val pears = makePairsFromList(
                        gson.fromJson(
                            body,
                            GetDevicesResponse::class.java
                        ).lights
                    )
                    devices=pears
                } else {
                    println("Auth failure")
                }


                // Handle this
            }
        })
    }

    var showDialog by remember {
        mutableStateOf(false)
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {

    }
    if (showDialog) {
        PermissionAlertDialog(context, launcher, hideDialog = { showDialog = false })
    }
    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = {

            println("New device stuff")
            // TODO Check permissions here

            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                || context.checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                || context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                || context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                showDialog = true
            } else {
                onNavigateToNewDevice()
            }
        }) {
            Icon(Icons.Filled.Add, "Add new device")
        }
    }, floatingActionButtonPosition = FabPosition.End) {
        Column(modifier = Modifier.padding(it)) {

            Row {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Devices",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(0.dp, 8.dp, 0.dp, 16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            LazyColumn {
                if (devices != null) {
                    items(devices!!) { item ->
                        Row {
                            LightView(item.first, cookie, onNavigateToDetails, device)
                            if (item.second != null) {
                                LightView(item.second!!, cookie, onNavigateToDetails, device)
                            }

                        }
                    }
                }
            }
        }
    }


}