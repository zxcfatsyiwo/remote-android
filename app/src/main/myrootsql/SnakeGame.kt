package com.example.myrootsql

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import java.util.*

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

    inner class GameView(context: android.content.Context?) : View(context) {
        private val paint = Paint()
        private val snake = LinkedList<Rect>()
        private var food = Rect()
        private var direction = 1 // 0=up,1=right,2=down,3=left
        private var nextDirection = 1
        private val blockSize = 30
        private var gridWidth = 0
        private var gridHeight = 0
        var score = 0
            private set

        fun reset() {
            snake.clear()
            val startX = 5
            val startY = 5
            for (i in 0..2) {
                snake.add(Rect(startX * blockSize - i * blockSize, startY * blockSize,
                    startX * blockSize - i * blockSize + blockSize, startY * blockSize + blockSize))
            }
            direction = 1
            nextDirection = 1
            score = 0
            spawnFood()
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            gridWidth = w / blockSize
            gridHeight = h / blockSize
            reset()
        }

        private fun spawnFood() {
            val rand = Random()
            var newX: Int
            var newY: Int
            var overlap: Boolean
            do {
                newX = rand.nextInt(gridWidth)
                newY = rand.nextInt(gridHeight)
                overlap = snake.any { it.left / blockSize == newX && it.top / blockSize == newY }
            } while (overlap)
            food = Rect(newX * blockSize, newY * blockSize,
                newX * blockSize + blockSize, newY * blockSize + blockSize)
        }

        fun update() {
            direction = nextDirection
            val head = snake.first
            var newX = head.left
            var newY = head.top
            when (direction) {
                0 -> newY -= blockSize
                1 -> newX += blockSize
                2 -> newY += blockSize
                3 -> newX -= blockSize
            }
            // Проверка столкновения с едой
            val eat = (newX == food.left && newY == food.top)
            if (eat) {
                score++
                spawnFood()
            }
            // Добавляем новую голову
            val newHead = Rect(newX, newY, newX + blockSize, newY + blockSize)
            snake.add(0, newHead)
            if (!eat) {
                snake.removeLast()
            }
            // Проверка столкновения со стенами
            if (newX < 0 || newX >= width || newY < 0 || newY >= height) {
                gameOver()
                return
            }
            // Проверка столкновения с телом
            for (i in 1 until snake.size) {
                if (snake[i].intersect(newHead)) {
                    gameOver()
                    return
                }
            }
        }

        private fun gameOver() {
            isRunning = false
            handler.removeCallbacks(gameRunnable)
            Toast.makeText(context, "Game Over! Score: $score", Toast.LENGTH_LONG).show()
            restartBtn.visibility = View.VISIBLE
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawColor(Color.BLACK)
            paint.color = Color.GREEN
            for (block in snake) {
                canvas.drawRect(block, paint)
            }
            paint.color = Color.RED
            canvas.drawRect(food, paint)
            // Глаза на голове (для красоты)
            paint.color = Color.WHITE
            val head = snake.first
            canvas.drawCircle(head.left + blockSize / 4f, head.top + blockSize / 4f, 4f, paint)
            canvas.drawCircle(head.left + 3 * blockSize / 4f, head.top + blockSize / 4f, 4f, paint)
            paint.color = Color.BLACK
            canvas.drawCircle(head.left + blockSize / 4f, head.top + blockSize / 4f, 2f, paint)
            canvas.drawCircle(head.left + 3 * blockSize / 4f, head.top + blockSize / 4f, 2f, paint)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y
            val screenWidth = width
            val screenHeight = height
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Определяем свайп
                // Для простоты: при касании меняем направление в зависимости от координат
                // или можно использовать детектор жестов, но ограничимся простым
                // Используем простое управление: если касание в верхней половине -> вверх, в нижней -> вниз и т.д.
                // Но лучше реализовать свайп, но для простоты используем координаты
                val centerX = screenWidth / 2
                val centerY = screenHeight / 2
                val diffX = x - centerX
                val diffY = y - centerY
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (diffX > 0) nextDirection = 1 else nextDirection = 3
                } else {
                    if (diffY > 0) nextDirection = 2 else nextDirection = 0
                }
                return true
            }
            return super.onTouchEvent(event)
        }
    }
}