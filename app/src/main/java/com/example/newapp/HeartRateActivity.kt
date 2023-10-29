package com.example.newapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import java.util.Calendar
import java.util.concurrent.TimeUnit

class HeartRateActivity : AppCompatActivity() {
    private var countdownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 900000 // Default: 15 minutes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_rate) // Set the layout for heart rate display here

        // Retrieve and display the heart rate data
        retrieveAndDisplayHeartRateData()
        val startTimerButton = findViewById<Button>(R.id.startTimerButton)
        val customTimeEditText = findViewById<EditText>(R.id.customTimeEditText)
        val stopTimerButton = findViewById<Button>(R.id.stopTimerButton)
        startTimerButton.setOnClickListener {
            val inputTime = customTimeEditText.text.toString().toLongOrNull()
            if (inputTime != null && inputTime > 0) {
                // If a valid custom time is provided, set the timer to the custom value (in milliseconds)
                timeLeftInMillis = inputTime
            }

            // Start the timer
            startTimer()
        }
        stopTimerButton.setOnClickListener {
            // Stop the timer
            stopTimer()
        }
        val signOutButton = findViewById<Button>(R.id.signOutButton)

        signOutButton.setOnClickListener {
            signOut()
        }
    }
    private fun startTimer() {
        countdownTimer?.cancel() // Cancel any existing timer

        countdownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                // Update the timer display (e.g., textViewTimer)
            }

            override fun onFinish() {
                timeLeftInMillis = 0
                // Timer has finished, display the alert message
                showAlert("Time's up! Consider using focus-boosting glasses.")
            }
        }

        countdownTimer?.start()
    }
    private fun stopTimer() {
        countdownTimer?.cancel() // Cancel the active timer
        timeLeftInMillis = 0
        showAlert("Set the timer to avoid overusing!.")
        // Optionally, reset the timer display or perform other actions
    }

    // Rest of your code for retrieving and displaying heart rate data

    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Alert")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun signOut() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()
            .addOnCompleteListener {
                // Redirect to the login page or perform any other necessary actions
                navigateToLoginActivity()
            }
    }
    private fun navigateToLoginActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Optional: Finish the current activity to prevent going back.
    }
    private fun retrieveAndDisplayHeartRateData() {
        val endTime = Calendar.getInstance().timeInMillis
        val startTime = endTime - TimeUnit.HOURS.toMillis(1) // Retrieve data from the last hour

        val dataReadRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_HEART_RATE_BPM)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
            .readData(dataReadRequest)
            .addOnSuccessListener { dataReadResponse ->
                // Process and display heart rate data from dataReadResponse
                displayHeartRateData(dataReadResponse)
            }
            .addOnFailureListener { e ->
                // Handle the failure to retrieve data
                // You can display a message to the user
                Toast.makeText(this, "Failed to retrieve heart rate data", Toast.LENGTH_SHORT).show()
            }
    }
    // Display heart rate data in the layout
    // Display heart rate data in the layout
    private fun displayHeartRateData(dataReadResponse: DataReadResponse) {
        val heartRateTextView = findViewById<TextView>(R.id.textViewHeartRateValue)
        val heartRates = mutableListOf<Float>()

        // Process the dataReadResponse and collect heart rate values
        val dataSet = dataReadResponse.getDataSet(DataType.TYPE_HEART_RATE_BPM)
        for (dp in dataSet?.dataPoints ?: emptyList()) {
            for (field in dp.dataType.fields) {
                val value = dp.getValue(field)
                val heartRate = value.asFloat()
                heartRates.add(heartRate)
            }
        }

        // Calculate the average heart rate
        val averageHeartRate = if (heartRates.isNotEmpty()) {
            heartRates.average()
        } else {
            0.0 // Set the average to 0 if no data is available
        }

        // Update the heart rate TextView with the average heart rate value
        heartRateTextView.text = "Heart Rate: ${averageHeartRate} BPM"

        // Check if the average heart rate is greater than 75
        if (averageHeartRate > 75) {
            // Display an alert to the user
            showAlert("Your average heart rate is high. Consider using focus-boosting glasses.")
        } else {
            showAlert("You are doing good!")
        }
    }

}