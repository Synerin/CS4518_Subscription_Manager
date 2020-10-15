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
import java.util.*

private const val TAG = "HomeFragment"
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
                    updateSoonest()
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
        var expenses: Double = 0.0
        val calendar: Calendar = Calendar.getInstance()
        val month: Int = calendar.get(Calendar.MONTH) + 1
        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)

        for(sub in subList) {
            val dueDate = getNextDue(sub.subDueDate, sub.subFrequency).split("/")
            val m: Int = dueDate[0].toInt()
            val d: Int = dueDate[1].toInt()
            if(m == month && day < d) expenses += sub.subCost.toDouble()
        }

        upcomingExpenses.text = "$$expenses" // TODO: Format string so cents are accurately represented
    }

    private fun updateSoonest() {
        subList.sortBy { timeFromNow(getNextDue(it.subDueDate, it.subFrequency)) }
        val size: Int = subList.size
        var name: String
        var due: String

        // TODO: Fix
        if(size > 0) {
            name = subList[0].subName
            due = getNextDue(subList[0].subDueDate, subList[0].subFrequency)
            soonestExpenseOne.text = "$name due on $due"
        }
        if(size > 1) {
            name = subList[1].subName
            due = getNextDue(subList[1].subDueDate, subList[1].subFrequency)
            soonestExpenseTwo.text ="$name due on $due"
        }
        if(size > 2) {
            name = subList[2].subName
            due = getNextDue(subList[2].subDueDate, subList[2].subFrequency)
            soonestExpenseThree.text = "$name due on $due"
        }
        // TODO: Calculate next possible due date for each subscription
    }

    private fun timeFromNow(date: String): Double {
        val calendar: Calendar = Calendar.getInstance()
        val month: Int = calendar.get(Calendar.MONTH) + 1
        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
        val dateValues: List<String> = date.split("/")
        val givenMonth: Double = dateValues[0].toDouble()
        val givenDay: Double = dateValues[1].toDouble()

        var result: Double = 0.0

        // I can almost feel that this will cause edge case issues
        if(month <= givenMonth) {
            result += givenMonth - month
        } else {
            result += (12.0 - givenMonth) + month
        }

        if(day <= givenDay) {
            result += (givenDay - day) / 100
        } else {
            result += ((31.0 - givenDay) + day) / 100 // Generalization, may need to change later
        }

        return result
    }

    private fun getNextDue(dueDate: String, frequency: String): String {
        val dateValues: List<String> = dueDate.split("/")
        val givenMonth: Int = dateValues[0].toInt()
        val givenDay: Int = dateValues[1].toInt()

        var calendar: Calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        var resultCal: Calendar = Calendar.getInstance()

        when (frequency) {
            "Yearly" -> {
                if(month < givenMonth) {
                    resultCal.set(Calendar.YEAR, year)
                } else {
                    resultCal.set(Calendar.YEAR, year + 1)
                }

                resultCal.set(Calendar.MONTH, givenMonth)
                resultCal.set(Calendar.DAY_OF_MONTH, givenDay)
            }
            "Monthly" -> {
                if(day < givenDay) {
                    resultCal.set(Calendar.MONTH, month)
                } else {
                    resultCal.set(Calendar.MONTH, month + 1)
                }

                resultCal.set(Calendar.DAY_OF_MONTH, givenDay)
            }
            "Weekly" -> {
                // TODO: Update values accurately
                resultCal.set(Calendar.MONTH, givenMonth)
                resultCal.set(Calendar.DAY_OF_MONTH, givenDay)
            }
        }

        val resultDate = "${resultCal.get(Calendar.MONTH)}/" +
                "${resultCal.get(Calendar.DAY_OF_MONTH)}/" +
                "${resultCal.get(Calendar.YEAR)}"

        return resultDate
    }

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}