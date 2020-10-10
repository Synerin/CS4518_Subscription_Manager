package com.example.subscriptionmanager

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val TAG = "SubListViewModel"

class SubListViewModel : ViewModel() {
    val subs = mutableListOf<Subscription>()

    init {
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        val myRef = userID?.let { FirebaseDatabase.getInstance().getReference(it) }

        myRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if(dataSnapshot.exists()){
                    val children= dataSnapshot.children
                    for(item in children) {
                        val retrieveSub = item.getValue(Subscription::class.java) //it crashes here
                        if (retrieveSub != null) {
                            val sub = Subscription();
                            sub.subName = retrieveSub.subName
                            sub.subCost = retrieveSub.subCost
                            sub.subDueDate = retrieveSub.subDueDate
                            sub.subFrequency = retrieveSub.subFrequency
                            sub.subImportance = retrieveSub.subImportance
                            sub.subType = retrieveSub.subType
                            subs += sub
                        }
                    }
                }
                Log.d(TAG, subs.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }
}