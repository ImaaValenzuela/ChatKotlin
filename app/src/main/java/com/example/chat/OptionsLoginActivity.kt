package com.example.chat

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class OptionsLoginActivity : AppCompatActivity() {

    private lateinit var binding:ActivityOptionsLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var mGoogleSignInCliente : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_options_login)

        binding = ActivityOptionsLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInCliente = GoogleSignIn.getClient(this, gso)



        checkSession()

        binding.optionEmail.setOnClickListener{
            startActivity(Intent(applicationContext, LoginEmailActivity::class.java))
        }

        binding.optionGoogle.setOnClickListener {
            initGoogle()
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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