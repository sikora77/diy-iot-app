package com.example.diyiot

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.MutableLiveData
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


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "storedData")


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
    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            isDeviceOnline = device.getOnline(cookie) == true
        }
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

