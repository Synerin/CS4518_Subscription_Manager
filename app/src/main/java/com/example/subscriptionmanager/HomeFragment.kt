package com.example.subscriptionmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.temporal.ChronoUnit
import java.util.*

class HomeFragment: Fragment() {
    private lateinit var firedatabase: FirebaseDatabase
    private lateinit var budgetTextView: TextView
    private lateinit var budgetBar: ProgressBar
    private lateinit var upcomingExpenses: TextView
    private lateinit var soonestExpenseOne: TextView
    private lateinit var soonestExpenseTwo: TextView
    private lateinit var soonestExpenseThree: TextView
    private lateinit var filteredList: ListView
    private var subList: MutableList<Subscription> = mutableListOf()

    private lateinit var myRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        budgetTextView = view.findViewById(R.id.budget_percentage_text_view)
        budgetBar = view.findViewById(R.id.budget_bar)
        upcomingExpenses = view.findViewById(R.id.upcoming_expenses_value_text_view)
        soonestExpenseOne = view.findViewById(R.id.soonest_expense_one_text_view)
        soonestExpenseTwo = view.findViewById(R.id.soonest_expense_two_text_view)
        soonestExpenseThree = view.findViewById(R.id.soonest_expense_three_text_view)
        filteredList = view.findViewById(R.id.filtered_list_view)
        firedatabase = FirebaseDatabase.getInstance()
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        myRef = userID?.let { FirebaseDatabase.getInstance().getReference(it) }!!

        myRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(user in dataSnapshot.children) {
                        val sub = user.getValue(Subscription::class.java)

                        if(sub != null) {
                            subList.add(sub)
                        }
                    }
                    updateUpcoming()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        return view
    }

    private fun updateUpcoming() {
        subList.sortBy { timeFromNow(it.subDueDate) }

        // TODO: Fix
        soonestExpenseOne.text = "${subList[0].subName} on ${subList[0].subDueDate}"
        // TODO: Calculate next possible due date for each subscription
    }

    private fun timeFromNow(date: String): Double {
        val month: Int = Calendar.MONTH
        val day: Int = Calendar.DAY_OF_MONTH
        val dateValues: List<String> = date.split("/")
        val givenMonth: Int = dateValues[0].toInt()
        val givenDay: Int = dateValues[1].toInt()

        var result: Double = 0.0

        // I can almost feel that this will cause edge case issues
        if(month < givenMonth) {
            result += givenMonth - month
        } else {
            result += (12 - givenMonth) + month
        }

        if(day < givenDay) {
            result += (givenDay - day) / 100
        } else {
            result += (31 - givenDay) + day // Generalization, may need to change later
        }

        return result
    }

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}