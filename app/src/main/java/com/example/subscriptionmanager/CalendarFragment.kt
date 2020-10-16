package com.example.subscriptionmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        calendarView = view.findViewById(R.id.full_calendar_view)
        expensesRecyclerView = view.findViewById(R.id.expenses_recycler_view)

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
                    val adapter = SubAdapter(subList)
                    expensesRecyclerView.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return view
    }

    override fun onStart() {
        super.onStart()
        calendarView.setOnDateChangeListener { _, year, monthOfYear, dayOfMonth ->
            selectedDate = (monthOfYear + 1).toString() + "/" + dayOfMonth.toString() + "/" + year.toString()
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

    private inner class SubAdapter(var subs: List<Subscription>): RecyclerView.Adapter<CalendarFragment.SubHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarFragment.SubHolder {
            val view = layoutInflater.inflate(R.layout.list_item_sub_mini, parent, false)
            return SubHolder(view)
        }

        override fun getItemCount() = subs.size

        override fun onBindViewHolder(holder: CalendarFragment.SubHolder, position: Int) {
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
