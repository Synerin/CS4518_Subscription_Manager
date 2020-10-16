package com.example.subscriptionmanager

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

private const val TAG = "AddPaymentFragment"
private const val ARG_SUB_ID = "sub_id"

class AddPaymentFragment : Fragment() {
    private lateinit var database: DatabaseReference

    private lateinit var picker: DatePickerDialog
    private lateinit var editTextSubName: EditText
    private lateinit var editTextSubCost: EditText
    private lateinit var editTextSubDueDate: EditText
    private lateinit var frequencySpinner: Spinner
    private lateinit var importanceSpinner: Spinner
    private lateinit var editTextSubType: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var buttonClear: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_payment, container, false)

        editTextSubName = view.findViewById(R.id.editTextSubName)
        editTextSubCost = view.findViewById(R.id.editTextSubCost)
        editTextSubDueDate = view.findViewById(R.id.editTextSubDueDate)
        frequencySpinner = view.findViewById(R.id.frequencySpinner)
        importanceSpinner = view.findViewById(R.id.importanceSpinner)
        editTextSubType = view.findViewById(R.id.editTextSubType)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)
        buttonClear = view.findViewById(R.id.buttonClear)

        if(arguments != null) {
            val subID: String = arguments?.getSerializable(ARG_SUB_ID) as String
            populateSub(subID)
        }

        editTextSubCost.addTextChangedListener(MoneyTextWatcher(editTextSubCost));

        // Creates DatePicker popup for getting subscription due date, and sets
        // editTextSubDueDate text value based on user interaction
        editTextSubDueDate.inputType = InputType.TYPE_NULL
        editTextSubDueDate.setOnClickListener(View.OnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
            val month: Int = calendar.get(Calendar.MONTH)
            val year: Int = calendar.get(Calendar.YEAR)
            // date picker dialog
            picker = context?.let { it1 ->
                DatePickerDialog(
                    it1,
                    { _, year, monthOfYear, dayOfMonth -> editTextSubDueDate.setText((monthOfYear + 1).toString() + "/" + dayOfMonth.toString() + "/" + year.toString()) },
                    year,
                    month,
                    day
                )
            }!!
            picker.show()
        })

        // Validates all fields are complete, adds the subscription to the database,
        // and clears all fields for adding additional subscription
        buttonSubmit.setOnClickListener {
            if (!validateInput()) {
                val toast: Toast =
                    Toast.makeText(context, "All fields must be completed", Toast.LENGTH_LONG)
                val view = toast.view
                view!!.setBackgroundResource(R.color.colorPrimaryDark)
                val text = view.findViewById(android.R.id.message) as TextView
                text.setTextColor(resources.getColor(R.color.colorWhite))
                toast.show()
            } else {
                addSubscription()
                clearFields()
            }
        }

        // Clears all input field text values
        buttonClear.setOnClickListener {
            clearFields()
        }

        return view
    }

    /**
     * When attempting to edit a specific subscription, the fields
     * will be auto-filled using this function, given the unique key
     * that corresponds to the subscription that was selected for editing
     *
     * @param subID The unique key of the subscription being passed in
     */
    private fun populateSub(subID: String) {
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        val myRef: DatabaseReference = userID?.let { FirebaseDatabase.getInstance().getReference(it).child(subID) }!!
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            // Given a DataSnapshot corresponding to a users specific subscription,
            // load in its data, and update it whenever its value changes
            override fun onDataChange(snapshot: DataSnapshot) {
                val sub = snapshot.getValue(Subscription::class.java)
                if (sub != null) {
                    editTextSubName.setText(sub.subName)
                    editTextSubCost.setText(sub.subCost)
                    editTextSubDueDate.setText(sub.subDueDate)
                    var adapter = frequencySpinner.adapter as ArrayAdapter<String>
                    frequencySpinner.setSelection(adapter.getPosition(sub.subFrequency))
                    adapter = importanceSpinner.adapter as ArrayAdapter<String>
                    importanceSpinner.setSelection(adapter.getPosition(sub.subImportance))
                    editTextSubType.setText(sub.subType)
                    Log.d(TAG, sub.uniqueId)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Validates all given fields are filled in
     *
     * @return      False if any fields are blank, else True
     */
    private fun validateInput(): Boolean {
        return when {
            editTextSubName.text.isBlank() -> false
            editTextSubCost.text.isBlank() -> false
            editTextSubDueDate.text.isBlank() -> false
            editTextSubType.text.isBlank() -> false
            else -> true
        }
    }

    /**
     * Given all fields are filled in, add or update a
     * subscription to/in the database using field values
     */
    private fun addSubscription() {
        val sub = Subscription();
        sub.subName = editTextSubName.text.toString()
        sub.subCost = editTextSubCost.text.toString().substring(1)
        sub.subDueDate = editTextSubDueDate.text.toString()
        sub.subFrequency = frequencySpinner.selectedItem.toString()
        sub.subImportance = importanceSpinner.selectedItem.toString()
        sub.subType = editTextSubType.text.toString()

        // {arguments} will be non-null if we are currently editing a subscription,
        // otherwise, we are adding a new subscription to the database. If we are
        // updating a subscription, we need its unique ID.  Otherwise, we will use
        // the randomly generated one assigned in the Subscription constructor
        if(arguments != null) {
            sub.uniqueId = arguments?.getSerializable(ARG_SUB_ID) as String
        }

        database = FirebaseDatabase.getInstance().reference
        val userID = FirebaseAuth.getInstance().currentUser?.uid

        if (userID != null) {
            database.child(userID).child(sub.uniqueId).setValue(sub)
        }
    }

    /**
     * Clear all EditText fields and clear arguments, which
     * changes mode from edit -> add subscription
     */
    private fun clearFields() {
        arguments = null
        editTextSubCost.text.clear()
        editTextSubDueDate.text.clear()
        editTextSubName.text.clear()
        editTextSubType.text.clear()
    }

    companion object {
        fun newInstance(): AddPaymentFragment = AddPaymentFragment()

        fun newInstance(subID: String): AddPaymentFragment {
            Log.d(TAG, "newInstance() called")

            val args = Bundle().apply {
                putSerializable(ARG_SUB_ID, subID)
            }

            return AddPaymentFragment().apply {
                arguments = args;
            }
        }
    }
}