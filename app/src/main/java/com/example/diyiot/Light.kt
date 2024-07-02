package com.example.diyiot

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException

data class Light(
    val brightness: Int,
    val id: String,
    var is_on: Boolean,
    var name: String,
    val nicknames: List<Any>,
    val rgb: Int,
    val type_: String
) {
    fun setOn(cookie: String) {
        val device = this
        val JSON = "application/json".toMediaType()
        val client = OkHttpClient()
        val body = "{\n" +
                "\t\"device_id\": \"${device.id}\",\n" +
                "\t\"is_on\": ${!device.is_on}\n" +
                "}"
        val request = Request.Builder().url("http://frog01.mikr.us:22070/api/v1/set_on")
            .header("Cookie", cookie)
            .post(body = body.toRequestBody(JSON)).build()
        val response = client.newCall(request).execute()


        val respBody = response.body?.string()

        println(respBody)
        device.is_on = !device.is_on
        // Handle this

    }
    fun setNewName(cookie:String,newName:String):String{
        val JSON = "application/json".toMediaType()
        val client = OkHttpClient()
        val body = "{\n" +
                "\t\"device_id\": \"${this.id}\",\n" +
                "\t\"new_name\": \"${newName}\"\n" +
                "}"
        val request = Request.Builder().url("http://frog01.mikr.us:22070/api/v1/rename_device")
            .header("Cookie", cookie)
            .post(body = body.toRequestBody(JSON)).build()
        val response = client.newCall(request).execute()
        val respBody = response.body?.string()
        println("New name:")
        println(respBody)
        val gson = Gson()
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        val responseMap: Map<String, Any> = gson.fromJson(respBody, mapType)
        return responseMap["new_name"] as String;
    }
    fun getOnline(cookie: String): Boolean? {
        val device = this
        val client = OkHttpClient()
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        val request =
            Request.Builder().url("http://frog01.mikr.us:22070/api/v1/is_online/${device.id}")
                .header("Cookie", cookie).get().build()
        val response = client.newCall(request).execute()


        val respBody = response.body?.string()
        val gson = Gson()
        val responseMap: Map<String, Any> = gson.fromJson(respBody, mapType)
        if (responseMap["isOnline"] != null) {
            return responseMap["isOnline"] as Boolean?
        }
        return null

    }

    fun setBrightness(cookie: String, brightness: Int) {
        val device = this
        val JSON = "application/json".toMediaType()
        val client = OkHttpClient()
        val body = "{\n" +
                "\t\"device_id\": \"${device.id}\",\n" +
                "\t\"brightness\": ${brightness}\n" +
                "}"
        val request = Request.Builder().url("http://frog01.mikr.us:22070/api/v1/set_on")
            .header("Cookie", cookie)
            .post(body = body.toRequestBody(JSON)).build()
        val response = client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle this
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.code == 200) {

                }

                // Handle this
            }
        })
    }
}