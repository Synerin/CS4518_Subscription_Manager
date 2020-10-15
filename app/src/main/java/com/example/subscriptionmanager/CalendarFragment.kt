package com.example.subscriptionmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import java.text.DateFormat

private const val TAG = "CalendarFragment"

class CalendarFragment: Fragment(){
    private lateinit var firedatabase: FirebaseDatabase
    private lateinit var calendarView: CalendarView
    private lateinit var editTextDate: TextView
    private lateinit var expenses: RecyclerView
    private lateinit var dateSelected: Button
    private lateinit var calendarTextAbove: TextView
    private var subList: MutableList<Subscription> = mutableListOf()
    val calendar: Calendar = Calendar.getInstance()
    private lateinit var databaseRef: DatabaseReference
    private var subsOnThisDay: MutableList<Subscription> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_calendar, container, false)

        calendarView = view.findViewById(R.id.full_calendar_view)
        calendarTextAbove = view.findViewById(R.id.Calendar)
        editTextDate = view.findViewById(R.id.edit_text_date)
        expenses = view.findViewById(R.id.list_of_expenses)
        dateSelected = view.findViewById(R.id.date_selected)

        firedatabase = FirebaseDatabase.getInstance()
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        databaseRef = userID?.let { FirebaseDatabase.getInstance().getReference(it) }!!

        databaseRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(user in dataSnapshot.children) {
                        val sub = user.getValue(Subscription::class.java)

                        if(sub != null) {
                            subList.add(sub)
                        }
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
                //and probably wont be
            }

        })

        return view
    }

    override fun onStart() {
        super.onStart()
        calendarView.apply{
            setOnDateChangeListener { view, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                calendarView.date = calendar.timeInMillis
                val dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM)
                editTextDate.append(dateFormatter.format(calendar.time))
            }
        }

        dateSelected.apply {
            setOnClickListener{
                val selectedDate:Long = calendarView.date
                calendar.timeInMillis = selectedDate
                val dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM)
                editTextDate.append(dateFormatter.format(calendar.time))
            }

        }

    }

    private fun updateSubList(){
        val selectedDate:Long = calendarView.date
        val selectedDateString = selectedDate.toString()

        for (sub in subList){
            val dueDate = sub.subDueDate
            if (dueDate.equals(selectedDateString)){
                subsOnThisDay.add(sub)
            }
        }
    }

    private fun updateList(){
    }

    companion object {
        fun newInstance(): CalendarFragment {
            return CalendarFragment()
        }
    }
}