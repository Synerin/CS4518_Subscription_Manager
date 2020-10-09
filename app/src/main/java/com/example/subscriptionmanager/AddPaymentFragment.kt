package com.example.subscriptionmanager

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

private const val TAG = "AddPaymentFragment"

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

        editTextSubCost.addTextChangedListener(MoneyTextWatcher(editTextSubCost));

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
                    { _, year, monthOfYear, dayOfMonth -> editTextSubDueDate.setText((monthOfYear + 1).toString() + "/" + dayOfMonth.toString()) },
                    year,
                    month,
                    day
                )
            }!!
            picker.show()
        })

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

        return view
    }

    private fun validateInput(): Boolean {
        return when {
            editTextSubName.text.isBlank() -> false
            editTextSubCost.text.isBlank() -> false
            editTextSubDueDate.text.isBlank() -> false
            editTextSubType.text.isBlank() -> false
            else -> true
        }
    }

    private fun addSubscription() {
        val sub = Subscription();
        sub.subName = editTextSubName.text.toString()
        sub.subCost = editTextSubCost.text.toString()
        sub.subDueDate = editTextSubDueDate.text.toString()
        sub.subFrequency = frequencySpinner.selectedItem.toString()
        sub.subImportance = importanceSpinner.selectedItem.toString()
        sub.subType = editTextSubType.text.toString()

        database = FirebaseDatabase.getInstance().reference
        val userID = FirebaseAuth.getInstance().currentUser?.uid

        if (userID != null) {
            database.child(userID).child(sub.uniqueId).setValue(sub)
        }
    }

    private fun clearFields() {
        editTextSubCost.text.clear()
        editTextSubDueDate.text.clear()
        editTextSubName.text.clear()
        editTextSubType.text.clear()
    }

    companion object {
        fun newInstance(): AddPaymentFragment = AddPaymentFragment()
    }
}