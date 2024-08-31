package com.example.chat

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chat.databinding.ActivityMainBinding
import com.example.chat.fragments.FragmentChats
import com.example.chat.fragments.FragmentProfile
import com.example.chat.fragments.FragmentUsers
import com.google.firebase.auth.FirebaseAuth
import java.net.Authenticator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        if(firebaseAuth.currentUser == null) optionsLogin()

        binding.bottomNV.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.item_profile -> {
                    seeFragmentProfile()
                    true
                }
                R.id.item_users -> {
                    seeFragmentUsers()
                    true
                }
                R.id.item_chats -> {
                    seeFragmentChats()
                    true
                }
                else ->{
                    false
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun optionsLogin() {
        startActivity(Intent(applicationContext, OptionsLoginActivity::class.java))
    }

    private fun seeFragmentProfile() {
        binding.tvTitle.text = "Perfil"

        val fragment = FragmentProfile()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentFL.id, fragment, "Fragment Profile")
        fragmentTransaction.commit()
    }

    private fun seeFragmentUsers () {
        binding.tvTitle.text = "Usuarios"

        val fragment = FragmentUsers()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentFL.id, fragment, "Fragment Users")
        fragmentTransaction.commit()
    }

    private fun seeFragmentChats () {
        binding.tvTitle.text = "Chats"

        val fragment = FragmentChats()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentFL.id, fragment, "Fragment Chats")
        fragmentTransaction.commit()
    }
}