package com.example.myrootsql

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class SnakeGame : Activity() {
    private lateinit var gameView: GameView
    private lateinit var scoreText: TextView
    private lateinit var restartBtn: Button
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snake)
        gameView = findViewById(R.id.gameView)
        scoreText = findViewById(R.id.scoreText)
        restartBtn = findViewById(R.id.restartBtn)
        restartBtn.setOnClickListener { restartGame() }
        startGame()
    }

    fun onGameOver() {
        isRunning = false
        handler.removeCallbacks(gameRunnable)
        Toast.makeText(this, "Game Over! Score: ${gameView.score}", Toast.LENGTH_LONG).show()
        restartBtn.visibility = Button.VISIBLE
    }

    private fun startGame() {
        isRunning = true
        gameView.reset()
        handler.removeCallbacks(gameRunnable)
        handler.postDelayed(gameRunnable, 200)
    }

    private val gameRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                gameView.update()
                gameView.invalidate()
                scoreText.text = "Score: ${gameView.score}"
                handler.postDelayed(this, 200)
            }
        }
    }

    private fun restartGame() {
        isRunning = false
        handler.removeCallbacks(gameRunnable)
        gameView.reset()
        scoreText.text = "Score: 0"
        startGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(gameRunnable)
        isRunning = false
    }
}
