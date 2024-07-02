package com.example.diyiot

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException

@Composable
fun RegisterView(onNavigateToLoginView: () -> Unit, userData: MutableLiveData<User>) {
    println("register view")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        var email by remember { mutableStateOf("") }
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var repPassword by remember { mutableStateOf("") }
        val textFieldModifier = Modifier
            .padding(0.dp, 0.dp, 0.dp, 10.dp)
            .align(Alignment.CenterHorizontally)
            .fillMaxWidth(0.8f)
        Text(
            text = "Welcome to Diy IOT",
            modifier = Modifier
                .padding(0.dp, 60.dp, 0.dp, 20.dp)
                .align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp
        )
        OutlinedTextField(
            modifier = textFieldModifier,
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") })
        OutlinedTextField(
            value = firstName, modifier = textFieldModifier,
            onValueChange = { firstName = it },
            label = { Text("First name") })
        OutlinedTextField(
            value = lastName, modifier = textFieldModifier,
            onValueChange = { lastName = it },
            label = { Text("Last name") })
        OutlinedTextField(
            value = password, modifier = textFieldModifier,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        OutlinedTextField(
            value = repPassword, modifier = textFieldModifier,
            onValueChange = { repPassword = it },
            label = { Text("Repeat password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.8f), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = ({} /*TODO*/), modifier = Modifier
                .width(100.dp)
                .height(50.dp)
                .align(Alignment.CenterVertically)) {
                Text(text = "Register")
            }
            ClickableText(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(
                            "Login"
                        )
                    }
                },
                modifier = Modifier.align(Alignment.CenterVertically),
                onClick = ({ onNavigateToLoginView() })
            )
        }
    }
}

@Composable
fun LoginView(
    onNavigateToRegisterView: () -> Unit,
    onNavigateToDeviceView: () -> Unit,
    userData: MutableLiveData<User>
) {
    val ctx = LocalContext.current
    val user by userData.observeAsState(null)
    if(user?.id!=-1 && user!=null){
        onNavigateToDeviceView()
    }
    Column(modifier = Modifier.fillMaxWidth(0.8f)) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        Text(
            text = "Welcome back to Diy IOT",
            modifier = Modifier
                .padding(0.dp, 160.dp, 0.dp, 20.dp)
                .align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp
        )
        //}
        OutlinedTextField(modifier = Modifier
            .padding(0.dp, 0.dp, 0.dp, 20.dp)
            .align(Alignment.CenterHorizontally)
            .fillMaxWidth(0.8f),
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") })
        OutlinedTextField(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(0.dp, 0.dp, 0.dp, 20.dp)
                .fillMaxWidth(0.8f),
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.8f), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(modifier = Modifier
                .width(100.dp)
                .height(50.dp)
                .align(Alignment.CenterVertically), onClick = ({
                GlobalScope.launch {
                    val JSON = "application/json".toMediaType();

                    val client = OkHttpClient()
                    val body = "{\"email\":\"${email}\",\"password\":\"${password}\"}"

                    val request = Request.Builder().url("http://frog01.mikr.us:22070/api/v1/login")
                        .post(body = body.toRequestBody(JSON)).build()
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            // Handle this
                        }

                        override fun onResponse(call: Call, response: Response) {
                            println(response.body?.string())
                            val sharedPrefs =
                                ctx.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            val cookie = response.headers["Set-Cookie"]
                            runBlocking {
                                val writeSuccess = sharedPrefs.edit().putString("AuthCookie", cookie).commit()
                                val read_cookie = sharedPrefs.getString("AuthCookie", "")
                                println("Cookie: ${read_cookie}")
                                fetchUser(read_cookie.toString(), userData)
                            }
//                            userData.postValue(user)
                            // Handle this
                        }
                    })
                }

            } /*TODO*/)) {
                Text(text = "Login")
            }
            ClickableText(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(
                            "Register"
                        )
                    }
                },
                modifier = Modifier.align(Alignment.CenterVertically),
                onClick = ({ onNavigateToRegisterView() })
            )
        }
    }
}