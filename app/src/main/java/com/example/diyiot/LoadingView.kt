package com.example.diyiot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun LoadingView(
    userData: MutableLiveData<User>,
    navController: NavHostController = rememberNavController(),
    cookie: String? = null
) {
    val user by userData.observeAsState()
    LaunchedEffect(Unit){
        fetchUser(cookie, userData)
        println("${userData.isInitialized}")
    }

    if (userData.isInitialized && user?.id != -1) {
        println("Stopped loading")
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