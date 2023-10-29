package com.example.newapp


import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.common.api.ApiException
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task

class MainActivity : AppCompatActivity() {
    private lateinit var signInButton: Button
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001  // Request code for onActivityResult

    private val signInActivityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if the user is already signed in
        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (lastSignedInAccount != null) {
            // If the user is already signed in, go directly to the HeartRateActivity
            navigateToHeartRateActivity()
        } else {
            // If not, set up the sign-in button
            signInButton = findViewById(R.id.signInButton)
            signInButton.setOnClickListener {
                val signInIntent = googleSignInClient.signInIntent
                signInActivityResult.launch(signInIntent)
            }

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope("https://www.googleapis.com/auth/fitness.body.read"))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // You now have a valid GoogleSignInAccount
            if (account != null) {
                // You now have a valid GoogleSignInAccount
                // You can proceed to use the GoogleSignInAccount to access Google Fit or other APIs.

                // Start the new activity to display heart rate data
                navigateToHeartRateActivity()
            }

            // You can proceed to use the GoogleSignInAccount to access Google Fit or other APIs.
        } catch (e: ApiException) {
            Toast.makeText(this, "Please try to log in with correct credentials", Toast.LENGTH_SHORT).show()
            // Handle sign-in failure (e.g., user canceled or error)
        }
    }

    private fun navigateToHeartRateActivity() {
        val intent = Intent(this, HeartRateActivity::class.java)
        startActivity(intent)
        finish() // Optional: Finish the current activity to prevent the user from going back to the login screen.
    }
}





