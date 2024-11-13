package com.example.chat

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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

    private lateinit var binding:ActivityOptionsLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var crashlytics: FirebaseCrashlytics
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var progressDialog: ProgressDialog
    private lateinit var mGoogleSignInCliente : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_options_login)

        binding = ActivityOptionsLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        crashlytics = FirebaseCrashlytics.getInstance()
        remoteConfig = FirebaseRemoteConfig.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)  // 1 hora
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.setDefaultsAsync(mapOf("button_color" to "#FF5733"))

        fetchRemoteConfig()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInCliente = GoogleSignIn.getClient(this, gso)



        checkSession()

        binding.optionEmail.setOnClickListener {
            logLoginEvent("email")
            startActivity(Intent(applicationContext, LoginEmailActivity::class.java))
        }

        binding.optionGoogle.setOnClickListener {
            logLoginEvent("google")
            initGoogle()
        }

        binding.optionNumber.setOnClickListener {
            logLoginEvent("phone")
            initError()
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun logLoginEvent(method: String) {
        val bundle = Bundle()
        bundle.putString("login_method", method)
        firebaseAnalytics.logEvent("login_event", bundle)
    }

    private fun fetchRemoteConfig() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val buttonColor = remoteConfig.getString("button_color")
                    applyButtonColor(buttonColor)
                }
            }
    }

    private fun applyButtonColor(colorHex: String) {

        val optionEmailButton = findViewById<MaterialButton>(R.id.optionEmail)
        val optionGoogleButton = findViewById<MaterialButton>(R.id.optionGoogle)
        val optionNumberButton = findViewById<MaterialButton>(R.id.optionNumber)

        try {
            val color = Color.parseColor(colorHex)
            optionEmailButton.setBackgroundColor(color)
            optionGoogleButton.setBackgroundColor(color)
            optionNumberButton.setBackgroundColor(color)
        } catch (e: IllegalArgumentException) {
            // Manejo de errores por si el color no es válido, aprovecho crashlytics (consultar si el uso es correcto)
            FirebaseCrashlytics.getInstance().log("Invalid color format in Remote Config: $colorHex")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun initError() {
        crashlytics.log("Crash manual activado - Botón 1")
        throw RuntimeException("Crash Manual: Botón 1")
    }

    private fun initGoogle() {
        val googleSignInIntent = mGoogleSignInCliente.signInIntent
        googleSignInARL.launch(googleSignInIntent)
    }

    private val googleSignInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){ res ->
        if ( res.resultCode == RESULT_OK){
            val data = res.data

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                authAccountGoogle(account.idToken)
            }catch (e : Exception){
                Toast.makeText(this,
                    "${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this,
                "Cancelado",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun authAccountGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { resAuth ->
                if(resAuth.additionalUserInfo!!.isNewUser){
                    updateUserInfo()
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,
                    "${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserInfo() {
        progressDialog.setMessage("Guardando informacion")

        val uidU = firebaseAuth.uid
        val namesU = firebaseAuth.currentUser!!.displayName
        val emailU = firebaseAuth.currentUser!!.email
        val timeR = Const.getTimeD()

        val dataUser = HashMap<String, Any>()

        dataUser["uid"] = "$uidU"
        dataUser["names"] = "$namesU"
        dataUser["email"] = "$emailU"
        dataUser["time"] = "$timeR"
        dataUser["prov"] = "Google"
        dataUser["status"] = "online"
        dataUser["image"] = ""

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(uidU!!)
            .setValue(dataUser)
            .addOnSuccessListener {
                progressDialog.dismiss()

                startActivity(Intent(applicationContext, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener {
                    e-> progressDialog.dismiss()
                Toast.makeText(this, "Fallo la creacion de la cuenta debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkSession(){
        if(firebaseAuth.currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }
}