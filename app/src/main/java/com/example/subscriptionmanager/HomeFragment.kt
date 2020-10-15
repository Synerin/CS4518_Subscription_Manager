package com.example.subscriptionmanager

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_sub_list.*
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "HomeFragment"
class HomeFragment: Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var firedatabase: FirebaseDatabase
    private lateinit var upcomingExpenses: TextView
    private lateinit var endOfMonth: TextView
    private lateinit var soonestExpenseOne: TextView
    private lateinit var soonestExpenseTwo: TextView
    private lateinit var soonestExpenseThree: TextView
    private lateinit var filterSpinner: Spinner
    private lateinit var filteredList: RecyclerView
    private var subList: MutableList<Subscription> = mutableListOf()

    private var filterAlreadySet: Boolean = false

    private lateinit var myRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        upcomingExpenses = view.findViewById(R.id.upcoming_expenses_value_text_view)
        endOfMonth = view.findViewById(R.id.end_of_month)
        soonestExpenseOne = view.findViewById(R.id.soonest_expense_one_text_view)
        soonestExpenseTwo = view.findViewById(R.id.soonest_expense_two_text_view)
        soonestExpenseThree = view.findViewById(R.id.soonest_expense_three_text_view)

        filterSpinner = view.findViewById(R.id.filter_spinner)
        filteredList = view.findViewById(R.id.filtered_list_view) as RecyclerView
        filteredList.layoutManager = LinearLayoutManager(context)

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
                    updateList()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        return view
    }

    private fun updateList() {
        val adapter = SubAdapter(subList)
        filteredList.adapter = adapter

        if(!filterAlreadySet) {
            context?.let {
                ArrayAdapter.createFromResource(
                    it,
                    R.array.filters,
                    android.R.layout.simple_spinner_item
                ).also { filterAdapter ->
                    filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    filterSpinner.adapter = filterAdapter
                    filterSpinner.onItemSelectedListener = this
                }
            }
        }

        filterAlreadySet = true
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

        var ordinal: String
        ordinal = when (month) {
            1, 3, 5, 6, 8, 10, 12 -> "31st"
            2 -> "28th" // Leap years ehhhhhh
            else -> "30th"
        }

        upcomingExpenses.text = "$$expenses" // TODO: Format string so cents are accurately represented
        endOfMonth.text = "Due by ${calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())} $ordinal"
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
    }

    private fun timeFromNow(date: String): Double {
        val calendar: Calendar = Calendar.getInstance()
        val month: Int = calendar.get(Calendar.MONTH) + 1
        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
        val dateValues: List<String> = date.split("/")
        val givenMonth: Double = dateValues[0].toDouble()
        val givenDay: Double = dateValues[1].toDouble()

        var result: Double = 0.0

        // I can almost feel the edge case issues
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

    private fun filterSubs(filter: String) {
        val resources = resources

        when (filter) {
            resources.getString(R.string.due_date) -> {
                subList.sortBy { timeFromNow(it.subDueDate) }
            }
            resources.getString(R.string.cost_low_high) -> {
                subList.sortBy { it.subCost.toDouble() }
            }
            resources.getString(R.string.cost_high_low) -> {
                subList.sortByDescending { it.subCost.toDouble() }
            }
            resources.getString(R.string.importance) -> {
                subList.sortByDescending { it.subImportance }
            }
            resources.getString(R.string.alphabetically) -> {
                subList.sortBy { it.subName }
            }
        }

        updateList()
    }

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    private inner class SubHolder(view: View):
        RecyclerView.ViewHolder(view) {
        val miniSubName: TextView = itemView.findViewById(R.id.mini_sub_name)
        val miniSubCost: TextView = itemView.findViewById(R.id.mini_sub_cost)
        val miniSubDue: TextView = itemView.findViewById(R.id.mini_sub_due)

        fun bind(sub: Subscription) {
            miniSubName.text = sub.subName
            miniSubCost.text = "$${sub.subCost}"
            miniSubDue.text = getNextDue(sub.subDueDate, sub.subFrequency)
        }
    }

    private inner class SubAdapter(var subs: List<Subscription>): RecyclerView.Adapter<SubHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubHolder {
            val view = layoutInflater.inflate(R.layout.list_item_sub_mini, parent, false)
            return SubHolder(view)
        }

        override fun getItemCount() = subs.size

        override fun onBindViewHolder(holder: SubHolder, position: Int) {
            val sub = subs[position]
            holder.bind(sub)
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        Log.d(TAG, "Hey fuck face, I hope you selected a filter because this got activated")
        filterSubs(filterSpinner.selectedItem.toString())
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}