package com.example.chat.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.chat.ChangePassword
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

    private lateinit var binding: FragmentProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        loadInfo()

        binding.btnUpdateInfo.setOnClickListener {
            startActivity(Intent(requireContext(), EditInformation::class.java))
        }

        binding.btnChangePass.setOnClickListener {
            startActivity(Intent(requireContext(), ChangePassword::class.java))
        }

        binding.btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(requireContext(), OptionsLoginActivity::class.java))
            requireActivity().finishAffinity()
        }
    }

    private fun loadInfo() {
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.uid ?: "")

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val names = snapshot.child("names").value?.toString() ?: "N/A"
                val email = snapshot.child("email").value?.toString() ?: "N/A"
                val prov = snapshot.child("prov").value?.toString() ?: "N/A"
                var time = snapshot.child("time").value?.toString() ?: "0"
                val image = snapshot.child("image").value?.toString()

                // Format time if it isn't null
                val formattedTime = if (time == "null") "0" else time
                val date = Const.dateFormat(formattedTime.toLong())

                // Set the values to the UI
                binding.tvNames.text = names
                binding.tvEmail.text = email
                binding.tvProv.text = prov
                binding.tvTimeReg.text = date

                // Load profile image
                image?.let {
                    Glide.with(requireContext())
                        .load(it)
                        .placeholder(R.drawable.ic_img_profile)
                        .into(binding.ivProfile)
                } ?: run {
                    binding.ivProfile.setImageResource(R.drawable.ic_img_profile)
                }

                // Show Change Password button if prov is "Email"
                binding.btnChangePass.visibility = if (prov == "Email") View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error loading profile data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
