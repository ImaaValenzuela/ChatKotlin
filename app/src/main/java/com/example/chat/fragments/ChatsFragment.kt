package com.example.chat.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.chat.OptionsLoginActivity
import com.example.chat.R
import com.example.chat.adapters.ChatsAdapter
import com.example.chat.databinding.FragmentChatsBinding
import com.example.chat.models.Chats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.crashlytics.FirebaseCrashlytics

class ChatsFragment : Fragment() {

    private lateinit var binding: FragmentChatsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var myUid = ""
    private lateinit var chatsArrayList: ArrayList<Chats>
    private lateinit var adapterChats: ChatsAdapter
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        // Verificar si el usuario está autenticado
        myUid = firebaseAuth.uid ?: ""
        if (myUid.isEmpty()) {
            FirebaseAuth.getInstance().signOut()
            val loginIntent = Intent(requireContext(), OptionsLoginActivity::class.java)
            startActivity(loginIntent)
            requireActivity().finish()
            return
        }

        FirebaseCrashlytics.getInstance().setUserId(myUid) // Asigna el ID del usuario
        loadChats()
    }

    private fun loadChats() {
        chatsArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatsArrayList.clear()
                for (ds in snapshot.children) {
                    val chatKey = ds.key.toString()
                    if (chatKey.contains(myUid)) {
                        val chatsModel = Chats()
                        chatsModel.keyChat = chatKey
                        chatsArrayList.add(chatsModel)
                    }
                }

                adapterChats = ChatsAdapter(mContext, chatsArrayList)
                binding.chatsRV.adapter = adapterChats
                binding.retryButton.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                FirebaseCrashlytics.getInstance().log("Error loading chats: ${error.message}")
                FirebaseCrashlytics.getInstance().recordException(error.toException())
                Toast.makeText(requireContext(), "Failed to load chats: ${error.message}", Toast.LENGTH_LONG).show()
                binding.retryButton.visibility = View.VISIBLE
            }
        })
    }

    fun retryLoadChats(view: View) {
        binding.retryButton.visibility = View.GONE
        loadChats()
    }
}
