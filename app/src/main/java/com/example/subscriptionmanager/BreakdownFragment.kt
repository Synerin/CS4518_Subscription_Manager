package com.example.subscriptionmanager

import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference

class BreakdownFragment: Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var monthlySpending: TextView
    private lateinit var expensesList: ListView

    companion object {
        fun newInstance(): BreakdownFragment = BreakdownFragment()
    }
}