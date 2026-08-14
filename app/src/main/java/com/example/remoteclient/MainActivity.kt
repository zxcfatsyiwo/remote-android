package com.example.remoteclient

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : AppCompatActivity() {
    private val client=OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val status=findViewById<TextView>(R.id.statusText)

        findViewById<Button>(R.id.startBtn).setOnClickListener {
            startService(Intent(this,BotService::class.java))
            status.text="Сервис запущен"
        }

        findViewById<Button>(R.id.checkBtn).setOnClickListener {
            Thread {
                try {
                    val request=Request.Builder()
                        .url("http://10.0.2.2:3000/api/devices")
                        .build()
                    client.newCall(request).execute().use { response ->
                        runOnUiThread {
                            status.text="Сервер отвечает: HTTP ${response.code}"
                        }
                    }
                } catch(e:Exception) {
                    runOnUiThread { status.text="Ошибка: ${e.message}" }
                }
            }.start()
        }
    }
}