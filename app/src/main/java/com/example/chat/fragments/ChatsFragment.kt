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
    ): View? {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        // Verificar si el usuario está autenticado
        myUid = firebaseAuth.uid ?: ""
        if (myUid.isEmpty()) {
            // Usuario no autenticado, redirigir a la pantalla de inicio de sesión
            FirebaseAuth.getInstance().signOut() // En caso de que haya alguna sesión activa
            val loginIntent = Intent(requireContext(), OptionsLoginActivity::class.java)
            startActivity(loginIntent)
            requireActivity().finish() // Finaliza esta actividad para evitar volver a esta pantalla
            return
        }

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
                binding.retryButton.visibility = View.GONE // Ocultar el botón de reintento si la carga fue exitosa
            }

            override fun onCancelled(error: DatabaseError) {
                // Enviar el error a Crashlytics para monitoreo
                FirebaseCrashlytics.getInstance().log("Error loading chats: ${error.message}")
                FirebaseCrashlytics.getInstance().recordException(error.toException())

                // Mostrar un mensaje de error al usuario
                Toast.makeText(requireContext(), "Failed to load chats: ${error.message}", Toast.LENGTH_LONG).show()

                // Mostrar el botón de reintento
                binding.retryButton.visibility = View.VISIBLE
            }
        })
    }

    // Función para reintentar cargar los chats en caso de error
    fun retryLoadChats(view: View) {
        binding.retryButton.visibility = View.GONE // Ocultar el botón de reintento
        loadChats() // Volver a intentar cargar los chats
    }
}
