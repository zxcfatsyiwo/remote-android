package com.example.myrootsql

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import java.util.*

class GameView(context: Context) : View(context) {
    private val paint = Paint()
    private val snake = LinkedList<Rect>()
    private var food = Rect()
    private var direction = 1
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
        val eat = (newX == food.left && newY == food.top)
        if (eat) {
            score++
            spawnFood()
        }
        val newHead = Rect(newX, newY, newX + blockSize, newY + blockSize)
        snake.add(0, newHead)
        if (!eat) {
            snake.removeLast()
        }
        if (newX < 0 || newX >= width || newY < 0 || newY >= height) {
            gameOver()
            return
        }
        for (i in 1 until snake.size) {
            if (snake[i].intersect(newHead)) {
                gameOver()
                return
            }
        }
    }

    private fun gameOver() {
        (context as? SnakeGame)?.onGameOver()
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
