package com.example.chat.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chat.R
import com.example.chat.User
import com.example.chat.UserAdapter
import com.example.chat.databinding.FragmentUsersBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UsersFragment : Fragment() {

    private lateinit var binding : FragmentUsersBinding

    private lateinit var mContext : Context
    private var userAdapter : UserAdapter?= null
    private var userList : List<User>?= null


    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentUsersBinding.inflate(layoutInflater, container, false)

        binding.RVUsers.setHasFixedSize(true)
        binding.RVUsers.layoutManager = LinearLayoutManager(mContext)

        userList = ArrayList()

        listUsers()

        return binding.root
    }

    private fun listUsers() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("names")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (userList as ArrayList<User>).clear()
                for(sn in snapshot.children){
                    val user : User? = sn.getValue(User::class.java)
                    if(!(user!!.uid).equals(firebaseUser)){
                        (userList as ArrayList<User>).add(user)
                    }
                }

                userAdapter = UserAdapter(mContext, userList!!)
                binding.RVUsers.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }
}