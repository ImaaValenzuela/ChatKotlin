package com.example.chat.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chat.models.User
import com.example.chat.adapters.UserAdapter
import com.example.chat.databinding.FragmentUsersBinding
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.crashlytics.FirebaseCrashlytics

class UsersFragment : Fragment() {

    private lateinit var binding: FragmentUsersBinding
    private var userAdapter: UserAdapter? = null
    private var userList = ArrayList<User>()
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var usersListener: ValueEventListener? = null // Variable para almacenar el listener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUsersBinding.inflate(inflater, container, false)
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        logFragmentVisit()

        setupRecyclerView()
        setupSearchListener()

        listUsers()

        return binding.root
    }

    private fun logFragmentVisit() {
        val bundle = Bundle().apply {
            putString("users_on_usersUI", "UsersFragment")
        }
        firebaseAnalytics.logEvent("fragment_visit", bundle)
    }

    private fun setupRecyclerView() {
        binding.RVUsers.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSearchListener() {
        binding.etSearchUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                searchUser(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
    }

    private fun listUsers() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("names")

        usersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    userList.clear()
                    if (binding.etSearchUser.text.toString().isEmpty()) {
                        for (sn in snapshot.children) {
                            val user = sn.getValue(User::class.java)
                            user?.let {
                                if (it.uid != firebaseUser) {
                                    userList.add(it)
                                }
                            }
                        }
                        updateRecyclerView()
                    }
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().log("Error in onDataChange")
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                try {
                    FirebaseCrashlytics.getInstance().log("Firebase database read cancelled")
                    FirebaseCrashlytics.getInstance().recordException(Exception("Database read cancelled: ${error.message}"))
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().log("Error in onCancelled")
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }
        }
        ref.addValueEventListener(usersListener!!)
    }

    private fun searchUser(query: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().reference.child("Users")
            .orderByChild("names")
            .startAt(query)
            .endAt("$query\uf8ff")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    userList.clear()
                    for (ss in snapshot.children) {
                        val user = ss.getValue(User::class.java)
                        user?.let {
                            if (it.uid != firebaseUser) {
                                userList.add(it)
                            }
                        }
                    }
                    updateRecyclerView()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().log("Error in searchUser")
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                try {
                    FirebaseCrashlytics.getInstance().log("Firebase database search cancelled")
                    FirebaseCrashlytics.getInstance().recordException(Exception("Search cancelled: ${error.message}"))
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().log("Error in onCancelled in searchUser")
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }
        })
    }

    private fun updateRecyclerView() {
        userAdapter = UserAdapter(requireContext(), userList)
        binding.RVUsers.adapter = userAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        usersListener?.let {
            FirebaseDatabase.getInstance().reference.removeEventListener(it)
        }
    }
}
