package com.example.subscriptionmanager

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_sub_list.*
import java.util.*
import kotlin.collections.ArrayList
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

        return view
    }

    override fun onStart() {
        super.onStart()
        calendarView.setOnDateChangeListener{
                view, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            calendarView.date = calendar.timeInMillis
            val dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM)
            editTextDate.append(dateFormatter.format(calendar.time))
        }

        dateSelected.setOnClickListener {
            val selectedDate:Long = calendarView.date
            calendar.timeInMillis = selectedDate
            val dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM)
            editTextDate.append(dateFormatter.format(calendar.time))
        }

    }



}