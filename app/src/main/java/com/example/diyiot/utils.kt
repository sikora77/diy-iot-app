package com.example.diyiot

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

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
fun showToast(context: Context,text:String){
    val duration = Toast.LENGTH_SHORT

    val toast = Toast.makeText(context, text, duration) // in Activity
    toast.show()

}