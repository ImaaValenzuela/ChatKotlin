package com.example.chat.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

        binding.etSearchUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(user: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchUser(user.toString())
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        listUsers()

        return binding.root
    }

    private fun listUsers() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("names")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (userList as ArrayList<User>).clear()
                if(binding.etSearchUser.text.toString().isEmpty()){
                    for(sn in snapshot.children){
                        val user : User? = sn.getValue(User::class.java)
                        if((user!!.uid) != firebaseUser){
                            (userList as ArrayList<User>).add(user)
                        }
                    }

                    userAdapter = UserAdapter(mContext, userList!!)
                    binding.RVUsers.adapter = userAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }

    private fun searchUser(user : String){
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("names")
            .startAt(user).endAt(user+"\uf8ff")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (userList as ArrayList<User>).clear()
                for (ss in snapshot.children){
                    val user : User ?= ss.getValue(User::class.java)
                    if((user!!.uid) != firebaseUser){
                        (userList as ArrayList<User>).add(user)
                    }
                }
                userAdapter = UserAdapter(context!!, userList!!)
                binding.RVUsers.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}