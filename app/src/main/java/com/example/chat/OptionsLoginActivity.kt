package com.example.chat

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chat.databinding.ActivityOptionsLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class OptionsLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOptionsLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var crashlytics: FirebaseCrashlytics
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOptionsLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeFirebase()
        setupEdgeToEdge()
        setupRemoteConfig()
        initializeGoogleSignIn()

        checkSession()

        binding.optionEmail.setOnClickListener {
            logLoginEvent("email")
            navigateToLoginActivity(LoginEmailActivity::class.java)
        }

        binding.optionGoogle.setOnClickListener {
            logLoginEvent("google")
            initGoogleSignIn()
        }

        binding.optionNumber.setOnClickListener {
            logLoginEvent("phone")
            simulateCrash()
        }
    }

    private fun initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        crashlytics = FirebaseCrashlytics.getInstance()
        remoteConfig = FirebaseRemoteConfig.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRemoteConfig() {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // 1 hora
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.setDefaultsAsync(mapOf("button_color" to "#FF5733"))

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val buttonColor = remoteConfig.getString("button_color")
                applyButtonColor(buttonColor)
            }
        }
    }

    private fun applyButtonColor(colorHex: String) {
        try {
            val color = Color.parseColor(colorHex)
            listOf(binding.optionEmail, binding.optionGoogle, binding.optionNumber).forEach { button ->
                button.setBackgroundColor(color)
            }
        } catch (e: IllegalArgumentException) {
            crashlytics.log("Invalid color format in Remote Config: $colorHex")
            crashlytics.recordException(e)
        }
    }

    private fun initializeGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun initGoogleSignIn() {
        val googleSignInIntent = googleSignInClient.signInIntent
        googleSignInARL.launch(googleSignInIntent)
    }

    private val googleSignInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { authWithGoogle(it) }
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Sign-in canceled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun authWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { resAuth ->
                if (resAuth.additionalUserInfo?.isNewUser == true) {
                    updateUserInfo()
                } else {
                    navigateToMainActivity()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserInfo() {
        val uid = firebaseAuth.uid ?: return
        val user = firebaseAuth.currentUser
        val userData = mapOf(
            "uid" to uid,
            "names" to (user?.displayName ?: ""),
            "email" to (user?.email ?: ""),
            "time" to System.currentTimeMillis().toString(),
            "prov" to "Google",
            "status" to "online",
            "image" to ""
        )

        FirebaseDatabase.getInstance().getReference("Users").child(uid)
            .setValue(userData)
            .addOnSuccessListener {
                navigateToMainActivity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error creating account: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkSession() {
        if (firebaseAuth.currentUser != null) {
            navigateToMainActivity()
        }
    }

    private fun simulateCrash() {
        crashlytics.log("Manual crash triggered - Button")
        throw RuntimeException("Manual crash triggered")
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun navigateToLoginActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }

    private fun logLoginEvent(method: String) {
        firebaseAnalytics.logEvent("login_event", Bundle().apply {
            putString("login_method", method)
        })
    }
}
