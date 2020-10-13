package com.example.subscriptionmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.DecimalFormat
import java.text.NumberFormat


class BreakdownFragment: Fragment() {
    private lateinit var firedatabase: FirebaseDatabase
    private lateinit var monthlySpending: TextView
    private lateinit var expensesList: ListView
    private lateinit var myRef : DatabaseReference
    private var monthlyTotal: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_breakdown, container, false)

        monthlySpending = view.findViewById(R.id.monthly_total_value)
        expensesList = view.findViewById(R.id.expense_list)
        firedatabase = FirebaseDatabase.getInstance()
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        myRef = userID?.let { FirebaseDatabase.getInstance().getReference(it) }!!

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if(dataSnapshot.exists()) {
                    for(user in dataSnapshot.children) {
                        val sub = user.getValue(Subscription::class.java)

                        if(sub != null) {
                            calculateMonthly(sub.subCost)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        return view
    }

    private fun calculateMonthly(subCost: String) {
        monthlyTotal += subCost.replace(",", "").toDouble()

        val formatter: NumberFormat = DecimalFormat("#,###")
        val formattedSpending: String = formatter.format(monthlyTotal)

        monthlySpending.text = "$${formattedSpending}"
    }

    companion object {
        fun newInstance(): BreakdownFragment = BreakdownFragment()
    }
}