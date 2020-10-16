package com.example.subscriptionmanager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.DateFormat
import java.util.*

private const val TAG = "CalendarFragment"

class CalendarFragment : Fragment() {
    private lateinit var databaseRef: DatabaseReference
    private lateinit var calendarView: CalendarView
    private lateinit var expensesRecyclerView: RecyclerView
    private var subList: MutableList<Subscription> = mutableListOf()
    private var subsOnThisDay: MutableList<Subscription> = mutableListOf()

    private var selectedDate: String = ""
    private var calendar: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        calendarView = view.findViewById(R.id.full_calendar_view)
        expensesRecyclerView = view.findViewById(R.id.expenses_recycler_view) as RecyclerView
        expensesRecyclerView.layoutManager = LinearLayoutManager(context)

        val userID = FirebaseAuth.getInstance().currentUser?.uid
        databaseRef = userID?.let { FirebaseDatabase.getInstance().getReference(it) }!!

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (user in dataSnapshot.children) {
                        val sub = user.getValue(Subscription::class.java)

                        if (sub != null) {
                            subList.add(sub)
                        }
                    }
                    updateList()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return view
    }

    /**
     * Recreate the filteredList RecyclerView with data ordered by the filterSubs method
     */
    private fun updateList() {
        val adapter = SubAdapter(subsOnThisDay)
        expensesRecyclerView.adapter = adapter
    }

    /**
     * Filter the subList based on date
     *
     * @param date The selected filter
     */
    private fun filterSubsByDate(date: String) {
        subsOnThisDay.clear()

        for(item in subList) {
            if(getNextDue(date, item.subDueDate, item.subFrequency)) {
                subsOnThisDay.add(item)
            }
        }

        updateList()
    }

    /**
     * Get the next possible due date for a given date, based on frequency
     *
     * @param dueDate The supplied due date for a subscription
     * @param frequency The frequency for a subscription
     * @return The next possible due date for a subscription
     */
    private fun getNextDue(selectedDate: String, dueDate: String, frequency: String): Boolean {
        val dateValues: List<String> = dueDate.split("/")
        val givenMonth: Int = dateValues[0].toInt()
        val givenDay: Int = dateValues[1].toInt()
        val givenYear: Int = dateValues[2].toInt()

        val selectedValues: List<String> = selectedDate.split("/")
        val selectedMonth = selectedValues[0].toInt()
        val selectedDay = selectedValues[1].toInt()
        val selectedYear = selectedValues[2].toInt()

        when (frequency) {
            "Yearly" -> {
                if(givenMonth == selectedMonth && givenDay == selectedDay) return true
            }
            "Monthly" -> {
                if(givenDay == selectedDay) return true
            }
            "Weekly" -> {
                val givenCalendar: Calendar = Calendar.getInstance()
                givenCalendar.set(givenYear, givenMonth, givenDay)
                val givenDayOfWeek: Int = givenCalendar.get(Calendar.DAY_OF_WEEK)

                val currentCalendar: Calendar = Calendar.getInstance()
                currentCalendar.set(selectedYear, selectedMonth, selectedDay)
                val currentDayOfWeek: Int = currentCalendar.get(Calendar.DAY_OF_WEEK)

                if(currentDayOfWeek == givenDayOfWeek) return true
            }
        }

        return false
    }

    override fun onStart() {
        super.onStart()
        calendarView.setOnDateChangeListener { _, year, monthOfYear, dayOfMonth ->
            selectedDate = (monthOfYear + 1).toString() + "/" + dayOfMonth.toString() + "/" + year.toString()
            filterSubsByDate(selectedDate)
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
            miniSubDue.text = sub.subDueDate
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

    companion object {
        fun newInstance(): CalendarFragment {
            return CalendarFragment()
        }
    }
}
