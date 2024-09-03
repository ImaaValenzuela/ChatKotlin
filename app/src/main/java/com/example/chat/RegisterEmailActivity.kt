package com.example.chat

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chat.databinding.ActivityRegisterEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterEmailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegisterEmailBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.btnRegister.setOnClickListener {
            validateInfo()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private var names = ""
    private var email = ""
    private var password = ""
    private var rPassword = ""

    private fun validateInfo() {
        names =  binding.etName.text.toString().trim()
        email = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString().trim()
        rPassword = binding.etRPassword.text.toString().trim()

        if(names.isEmpty()){
            binding.etName.error = "Ingrese nombre"
            binding.etName.requestFocus()
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmail.error = "Email invalido"
            binding.etEmail.requestFocus()
        } else if(email.isEmpty()){
            binding.etEmail.error = "Ingrese email"
            binding.etEmail.requestFocus()
        } else if (password.isEmpty()){
            binding.etPassword.error = "Ingrese contraseña"
            binding.etPassword.requestFocus()
        } else if (rPassword.isEmpty()){
            binding.etRPassword.error = "Repita contraseña"
            binding.etRPassword.requestFocus()
        } else if (password != rPassword){
            binding.etRPassword.error = "No coinciden las contraseñas"
            binding.etRPassword.requestFocus()
        } else registerUser()
    }

    private fun registerUser() {
        progressDialog.setMessage("Creando cuenta")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                updateInfo()
            }
            .addOnFailureListener{
                e-> progressDialog.dismiss()
                Toast.makeText(this, "Fallo la creacion de la cuenta debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateInfo(){
        progressDialog.setMessage("Guardando informacion")

        val uidU = firebaseAuth.uid
        val namesU = names
        val emailU = firebaseAuth.currentUser!!.email
        val timeR = Const.getTimeD()

        val dataUser = HashMap<String, Any>()

        dataUser["uid"] = "$uidU"
        dataUser["names"] = namesU
        dataUser["email"] = "$emailU"
        dataUser["time"] = "$timeR"
        dataUser["prov"] = "email"
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
}