package com.example.LetMeEat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.util.*

class MainActivity : Activity() {

    private lateinit var highestScoreTextView: TextView
    private var highestScore = 0
    private lateinit var sharedPreferences: SharedPreferences
    private var isGameEnded = false // Declare boolean flag to track game end

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        highestScore = sharedPreferences.getInt("highestScore", 0)

        val resetButton = findViewById<Button>(R.id.reset_highest_score)
        highestScoreTextView = findViewById(R.id.highest_score)
        highestScoreTextView.text = "Highest Score: $highestScore"

        val board = findViewById<RelativeLayout>(R.id.board)
        val upButton = findViewById<Button>(R.id.up)
        val downButton = findViewById<Button>(R.id.down)
        val leftButton = findViewById<Button>(R.id.left)
        val rightButton = findViewById<Button>(R.id.right)
        val pauseButton = findViewById<Button>(R.id.pause)
        val mainMenu = findViewById<Button>(R.id.MainMenu)
        val newgame = findViewById<Button>(R.id.new_game)
        val resume = findViewById<Button>(R.id.resume)
        val playagain = findViewById<RelativeLayout>(R.id.board1)
        val score2 = findViewById<Button>(R.id.score2)
        val endGameButton = findViewById<Button>(R.id.end_game)
        highestScoreTextView = findViewById(R.id.highest_score)
        highestScoreTextView.text = " Highest --- Score: $highestScore"

        val carrot = ImageView(this)
        val rabbit = ImageView(this)
        val fox = ImageView(this) // Add fox ImageView
        val rabbitSegments = mutableListOf(rabbit)
        val handler = Handler()
        var delayMillis = 25L
        var currentDirection = "right"
        var scorex = -1

        board.visibility = View.INVISIBLE
        playagain.visibility = View.INVISIBLE
        score2.visibility = View.VISIBLE

        resetButton.setOnClickListener {
            // Reset the highest score to 0
            highestScore = 0
            highestScoreTextView.text = " Highest --- Score: $highestScore"
            saveHighestScore()
        }

        newgame.setOnClickListener {
            board.visibility = View.VISIBLE
            newgame.visibility = View.INVISIBLE
            resume.visibility = View.INVISIBLE
            score2.visibility = View.VISIBLE
            resetButton.visibility = View.INVISIBLE
            endGameButton.visibility = View.VISIBLE

            val rabbitWidth = 172 // Snake width in pixels
            val rabbitHeight = 150 // Snake height in pixels
            val carrotWidth = 90 // Meat width in pixels
            val carrotHeight = 90 // Meat height in pixels
            val foxWidth = 200 // fox width in pixels
            val foxHeight = 240 // fox height in pixels
            fox.scaleX=-1f

            rabbit.setImageResource(R.drawable.rabbit)
            rabbit.setPadding(10, 10, 10, 10) // Add padding to increase touch-sensitive area
            rabbit.layoutParams = ViewGroup.LayoutParams(rabbitWidth, rabbitHeight)
            board.addView(rabbit)
            rabbitSegments.add(rabbit)

            var rabbitX = rabbit.x
            var rabbitY = rabbit.y

            carrot.setImageResource(R.drawable.carrot)
            carrot.setPadding(-10, -80, -10, -60) // Add padding to increase touch-sensitive area
            carrot.layoutParams = ViewGroup.LayoutParams(carrotWidth, carrotHeight)
            board.addView(carrot)

            fox.setImageResource(R.drawable.fox) // Assuming "fox" is the name of your vector drawable
            fox.layoutParams = ViewGroup.LayoutParams(foxWidth, foxHeight)
            board.addView(fox)

            // Function to generate random coordinates for fox within the board bounds
            fun generateRandomPosition(): Pair<Float, Float> {
                //val randomX = Random().nextInt(500 - foxWidth)
                //val randomY = Random().nextInt(500 - foxHeight)
                return Pair(400f, 550f)
            }

            // Position the fox at a random location initially
            var (foxX, foxY) = generateRandomPosition()
            fox.x = foxX
            fox.y = foxY

            // Add logic to position the fox within the board layout

            fun checkFoodCollision() {
                val carrotBounds = Rect()
                carrot.getHitRect(carrotBounds)

                for (segment in rabbitSegments) {
                    val segmentBounds = Rect()
                    segment.getHitRect(segmentBounds)

                    if (Rect.intersects(carrotBounds, segmentBounds)) {
                        val randomX = Random().nextInt(board.width - 200)
                        val randomY = Random().nextInt(board.height - 200)

                        carrot.x = randomX.toFloat()
                        carrot.y = randomY.toFloat()



                        delayMillis--
                        scorex++
                        score2.text = "score : $scorex"

                        if (!isGameEnded) { // Check the flag before updating the highest score
                            if (scorex > highestScore) {
                                highestScore = scorex
                                highestScoreTextView.text = " Highest --- Score: $highestScore"
                                saveHighestScore()
                            }
                        }

                        break // Exit the loop once collision is detected
                    }
                }
            }

            fun checkfoxCollision() {
                val foxBounds = Rect()
                fox.getHitRect(foxBounds)

                for (segment in rabbitSegments) {
                    val segmentBounds = Rect()
                    segment.getHitRect(segmentBounds)

                    if (Rect.intersects(foxBounds, segmentBounds)) {
                        isGameEnded = true // End the game if fox collision detected
                        playagain.visibility = View.VISIBLE
                        board.visibility = View.INVISIBLE
                        newgame.visibility = View.INVISIBLE
                        mainMenu.visibility = View.VISIBLE

                        return // Exit the function once collision is detected
                    }
                }
            }

            // Define a function to move the fox
            fun movefox() {
                // Implement your logic to move the fox here
                // For example, you can move it randomly or towards a specific direction
                // Here's a simple example of moving the fox towards the rabbit's current position
                val dx = (rabbit.x) - fox.x
                val dy = rabbit.y - fox.y

                // Move fox towards the rabbit's position
                fox.x += dx / 340
                fox.y += dy / 340
            }

            val foxMovementHandler = Handler()
            val foxMovementRunnable = object : Runnable {
                override fun run() {
                    movefox()
                    checkfoxCollision()
                    foxMovementHandler.postDelayed(this, delayMillis)
                }
            }
            // Start moving the fox
            foxMovementHandler.postDelayed(foxMovementRunnable, delayMillis)

            val runnable = object : Runnable {
                override fun run() {
                    for (i in rabbitSegments.size - 1 downTo 1) {
                        rabbitSegments[i].x = rabbitSegments[i - 1].x
                        rabbitSegments[i].y = rabbitSegments[i - 1].y
                    }

                    when (currentDirection) {
                        "up" -> {
                            rabbitY -= 3
                            if (rabbitY < -600) {
                                rabbitY = 760f
                            }
                            rabbit.translationY = rabbitY
                        }
                        "down" -> {
                            rabbitY += 3
                            if (rabbitY > 1020 - rabbit.height) {
                                rabbitY = -650f
                            }
                            rabbit.translationY = rabbitY
                        }
                        "left" -> {
                            rabbitX -= 3
                            if (rabbitX < -500) {
                                rabbitX = 560f
                            }
                            rabbit.scaleX = 1f // Flip the rabbit horizontally
                            rabbit.translationX = rabbitX
                        }
                        "right" -> {
                            rabbitX += 3
                            if (rabbitX > 600 - rabbit.width) {
                                rabbitX = -500f
                            }
                            rabbit.scaleX = -1f
                            rabbit.translationX = rabbitX
                        }
                        "pause" -> {
                            // No need to update position when paused
                        }
                    }

                    checkFoodCollision()
                    checkfoxCollision() // Check for fox collision
                    handler.postDelayed(this, delayMillis)
                }
            }

            handler.postDelayed(runnable, delayMillis)

            upButton.setOnClickListener {
                currentDirection = "up"
            }
            downButton.setOnClickListener {
                currentDirection = "down"
            }
            leftButton.setOnClickListener {
                currentDirection = "left"
            }
            rightButton.setOnClickListener {
                currentDirection = "right"
            }
            pauseButton.setOnClickListener {
                currentDirection = "pause"
                board.visibility = View.INVISIBLE
                resume.visibility = View.VISIBLE
                endGameButton.visibility = View.VISIBLE
            }
            resume.setOnClickListener {
                currentDirection = "right"
                board.visibility = View.VISIBLE
                resume.visibility = View.INVISIBLE
            }
            endGameButton.setOnClickListener {
                // Create an AlertDialog
                val alertDialogBuilder = AlertDialog.Builder(this)

                // Set the title and message
                alertDialogBuilder.setTitle("Confirm Exit")
                alertDialogBuilder.setMessage("Are you sure you want to exit the game?")

                // Set a positive button and its click listener
                alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
                    // Finish the current activity (exit the game)
                    finishAffinity()
                    System.exit(0)
                }

                // Set a negative button and its click listener
                alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
                    // Dismiss the dialog if "No" is clicked
                    dialog.dismiss()
                }

                // Create and show the AlertDialog
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        }

        mainMenu.setOnClickListener {
            val Intent = Intent(this, newGame::class.java)
            startActivity(Intent)
        }

        hideSystemUI()
    }

    private fun saveHighestScore() {
        val editor = sharedPreferences.edit()
        editor.putInt("highestScore", highestScore)
        editor.apply()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
