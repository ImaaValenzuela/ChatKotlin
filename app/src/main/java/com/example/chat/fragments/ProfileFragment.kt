package com.example.chat.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.chat.Const
import com.example.chat.EditInformation
import com.example.chat.OptionsLoginActivity
import com.example.chat.R
import com.example.chat.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private lateinit var binding : FragmentProfileBinding
    private lateinit var mContext : Context
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        loadInfo()

        binding.btnUpdateInfo.setOnClickListener {
            startActivity(Intent(mContext, EditInformation::class.java))
        }

        binding.btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(mContext, OptionsLoginActivity::class.java))
            activity?.finishAffinity()
        }
    }

    private fun loadInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val names = "${snapshot.child("names").value}"
                    val email = "${snapshot.child("email").value}"
                    val prov = "${snapshot.child("prov").value}"
                    var time = "${snapshot.child("time").value}"
                    val image = "${snapshot.child("image").value}"

                    if(time == "null"){
                        time = "0"
                    }

                    val date = Const.dateFormat(time.toLong())

                    binding.tvNames.text = names
                    binding.tvEmail.text = email
                    binding.tvProv.text = prov
                    binding.tvTimeReg.text = date


                    try {
                        Glide.with(mContext)
                            .load(image)
                            .placeholder(R.drawable.ic_img_profile)
                            .into(binding.ivProfile)

                    }catch (e:Exception){
                        Toast.makeText(
                            mContext,
                            "${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
}