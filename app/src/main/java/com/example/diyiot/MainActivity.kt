package com.example.diyiot

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.ParcelUuid
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.diyiot.ui.theme.DiyiotTheme
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "storedData")


fun getCookie(context: Context): String? {
    val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    return prefs.getString("AuthCookie", "")
}

fun fetchUser(
    cookie: String?,
    userData: MutableLiveData<User>
) {
    val client = OkHttpClient()
    val request =
        Request.Builder().url("http://frog01.mikr.us:22070/api/v1/me").get()
            .header("Cookie", cookie.toString()).build()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = client.newCall(request).execute()
            val respBody = response.body?.string()
            println(respBody)

            val gson = Gson()
            val tmpUser = gson.fromJson(respBody, User::class.java)
            println("User fetched")
            userData.postValue(tmpUser)
        } catch (_: Exception) {
            println("Failed to fetch user")
            userData.postValue(User("", "", -1, ""))
        }
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DiyiotTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val userData = MutableLiveData<User>()
                    val context = applicationContext
                    val cookie = getCookie(context)
                    val deviceData = MutableLiveData<Light>()
                    val navController = rememberNavController()
                    println("Cookie: $cookie")
                    println("Hello")


                    NavHost(navController = navController, startDestination = "loading") {
                        composable("loading") {
                            LoadingView(userData, navController, cookie)
                        }
                        composable("login") {
                            LoginView(
                                onNavigateToRegisterView = { navController.navigate("register") },
                                onNavigateToDeviceView = { navController.navigate("deviceView") },
                                userData
                            )
                        }
                        composable("register") {
                            RegisterView(
                                onNavigateToLoginView = { navController.navigate("login") },
                                userData
                            )
                        }
                        composable("deviceView") {
                            DeviceView(
                                onNavigateToDetails = { deviceString: String ->
                                    navController.navigate("deviceDetails/${deviceString}")
                                },
                                onNavigateToNewDevice = {
                                    navController.navigate("addDeviceView")
                                },
                                context, deviceData
                            )
                        }
                        composable("addDeviceView") {
                            AddDeviceView(context)
                        }
                        composable("deviceDetails/{deviceJson}",
                            arguments = listOf(
                                navArgument("deviceJson") {
                                    type = NavType.StringType
//                                    nullable=true

                                }
                            )
                        ) {
                            val deviceString = it.arguments?.getString("deviceJson");
                            println(deviceString)
                            val gson = Gson()
                            val light: Light = gson.fromJson(deviceString, Light::class.java)
                            DeviceDetailsView(
                                light,
                                cookie.toString()
                            )
                        }
                    }

                }
            }
        }
    }


}

@Composable
fun LoadingView(
    userData: MutableLiveData<User>,
    navController: NavHostController = rememberNavController(),
    cookie: String? = null
) {
    val user by userData.observeAsState()
    fetchUser(cookie, userData)
    println("${userData.isInitialized}")

    if (userData.isInitialized && user?.id != -1) {
        navController.navigate("deviceView")
    } else if (userData.isInitialized) {
        navController.navigate("login")

    }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
        Text(
            text = "Welcome back to Diy IOT",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(0.dp, 0.dp, 0.dp, 32.dp),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp
        )
        if (user == null) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(64.dp)
                    .align(Alignment.CenterHorizontally),
                strokeWidth = 10.dp
            )
        }
    }

}

@Composable
fun LightView(
    device: Light,
    cookie: String,
    onNavigateToDetails: (String) -> Unit,
    deviceArg: MutableLiveData<Light>
) {
    var isDeviceOn: Boolean by remember {
        mutableStateOf(device.is_on)
    }
    var isDeviceOnline: Boolean by remember {
        mutableStateOf(false)
    }
    // rewrite this into alarmmanager

    CoroutineScope(Dispatchers.IO).launch {
        isDeviceOnline = device.getOnline(cookie) == true
    }

    Button(
        enabled = isDeviceOnline,
        onClick = ({
            CoroutineScope(Dispatchers.IO).launch {
                device.setOn(cookie)
                isDeviceOn = device.is_on
            }

        }), modifier = Modifier
            .width(200.dp)
            .height(100.dp)

    ) {
        Spacer(Modifier.weight(2.5f))
        Column {
            Text(text = device.name)
            var isOnText = if (isDeviceOn) "On" else "Off"
            if (!isDeviceOnline) {
                isOnText = "Offline"
            }
            Text(text = isOnText)

        }
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = {
                deviceArg.postValue(device)
                val gson = Gson()
                val devStr = gson.toJson(device)
                println(devStr)

                onNavigateToDetails(devStr)

            }
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "Device details"
            )
        }

    }

}

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

@Composable
fun DeviceView(
    onNavigateToDetails: (String) -> Unit,
    onNavigateToNewDevice: () -> Unit,
    context: Context,
    device: MutableLiveData<Light>
) {

    val client = OkHttpClient()
    val cookie = getCookie(context).toString()
    val devicesList = MutableLiveData<List<Light>>()
    val devices by devicesList.observeAsState()
    val request = Request.Builder().url("http://frog01.mikr.us:22070/api/v1/full_devices")
        .header("Cookie", cookie)
        .get().build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // Handle this
        }

        override fun onResponse(call: Call, response: Response) {
            val body = response.body?.string()
//            println(body)
            if (response.code == 200) {
                val gson = Gson()
                devicesList.postValue(
                    gson.fromJson(
                        body,
                        GetDevicesResponse::class.java
                    ).lights
                )
            } else {
                println("Auth failure")
            }


            // Handle this
        }
    })
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
                        LightView(item, cookie, onNavigateToDetails, device)
                    }
                }
            }
        }
    }


}


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

        val settingsBuilder = ScanSettings.Builder()
        CoroutineScope(Dispatchers.IO).launch {
            leScanner.startScan(listOf(filter), settingsBuilder.build(), callback)
            Thread.sleep(10 * 1000)
            leScanner.stopScan(callback)
            println("Done scanning")
            callback.devices.forEach { entry ->
                val result = entry.value
                println(result.device.name)
                println(result.device.address)
                println(result.scanRecord?.serviceUuids)
            }
        }
        LazyColumn() {
            if (availableDevices != null) {
                items(availableDevices.values.toMutableList()) { result ->
                    Text(text = result.device.name)
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