package com.example.chat.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chat.databinding.FragmentChatsBinding
import com.example.chat.R
import com.example.chat.adapters.ChatsAdapter
import com.example.chat.models.Chats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatsFragment : Fragment() {

    private lateinit var binding : FragmentChatsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var myUid = ""
    private lateinit var chatsArrayList : ArrayList<Chats>
    private lateinit var adapterChats : ChatsAdapter
    private lateinit var mContext : Context


    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        myUid = "${firebaseAuth.uid}"
        loadChats()
    }

    private fun loadChats() {
        chatsArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                chatsArrayList.clear()
                for (ds in snapshot.children){
                    val chatKey = "${ds.key}"
                    if (chatKey.contains(myUid)){
                        val chatsModel = Chats()
                        chatsModel.keyChat = chatKey
                        chatsArrayList.add(chatsModel)
                    }
                }

                adapterChats = ChatsAdapter(mContext, chatsArrayList)
                binding.chatsRV.adapter = adapterChats
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}