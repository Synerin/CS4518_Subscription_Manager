package com.example.subscriptionmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.DecimalFormat
import java.text.NumberFormat

class BreakdownFragment: Fragment() {
    private lateinit var firedatabase: FirebaseDatabase
    private lateinit var monthlySpending: TextView
    private lateinit var expensesList: ListView
    private lateinit var myRef : DatabaseReference

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

        calculateMonthly()

        return view
    }

    private fun calculateMonthly() {
        var spending: Double = 0.0

        spending += 5000
        // TODO: Actually calculate monthly spending

        val formatter: NumberFormat = DecimalFormat("#,###")
        val formattedSpending: String = formatter.format(spending)

        monthlySpending.text = "$${formattedSpending}"
    }

    companion object {
        fun newInstance(): BreakdownFragment = BreakdownFragment()
    }
}