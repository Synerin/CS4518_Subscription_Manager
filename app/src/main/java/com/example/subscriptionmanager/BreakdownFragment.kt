package com.example.subscriptionmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class BreakdownFragment: Fragment() {
    private lateinit var firedatabase: FirebaseDatabase
    private lateinit var monthlySpending: TextView
    private lateinit var expenseOne: TextView
    private lateinit var expenseTwo: TextView
    private lateinit var expenseThree: TextView
    private lateinit var myRef : DatabaseReference
    private var monthlyTotal: Double = 0.0
    private var subList: MutableList<Subscription> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_breakdown, container, false)

        monthlySpending = view.findViewById(R.id.monthly_total_value)
        expenseOne = view.findViewById(R.id.expense_one)
        expenseTwo = view.findViewById(R.id.expense_two)
        expenseThree = view.findViewById(R.id.expense_three)
        firedatabase = FirebaseDatabase.getInstance()
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        myRef = userID?.let { FirebaseDatabase.getInstance().getReference(it) }!!

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if(dataSnapshot.exists()) {
                    for(user in dataSnapshot.children) {
                        val sub = user.getValue(Subscription::class.java)

                        if(sub != null) {
                            calculateMonthly(sub.subCost, sub.subFrequency)
                            subList.add(sub)
                        }
                    }

                    calculateTop()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        return view
    }

    /**
     * Calculate the monthly expense for a given subscription, adding to the monthlyTotal
     *
     * @param subCost The cost of the subscription
     * @param subFrequency The frequency of the subscription, either Weekly, Monthly, or Yearly
     * @return Nothing, but add the calculated monthly cost to the monthlyTotal
     */
    private fun calculateMonthly(subCost: String, subFrequency: String) {
        // Calculate monthly cost based on subscription cost and subscription frequency
        monthlyTotal += trueCost(costToDouble(subCost), subFrequency)

        // Format string to monetary units, e.g. 1234.5 => 1,234.50
        val textTotal = String.format("%,.2f", monthlyTotal)

        monthlySpending.text = "$${textTotal}"
    }

    /**
     * Calculate and set the top three expenses from the list of subscriptions
     */
    private fun calculateTop() {
        val size: Int = subList.size
        var subscription: Subscription? = null
        var name: String? = null

        subList.sortByDescending { trueCost(costToDouble(it.subCost), it.subFrequency) }

        if(size > 0) {
            subscription = subList[0]
            // 13 chars seems to be a reasonable maximum length given a textSize of 28sp
            name = if (subscription.subName.length > 13) "${subscription.subName.substring(0, 13)}..." else subscription.subName
            expenseOne.text = "1. $name,\n\t\t\t$${subscription.subCost} ${subscription.subFrequency}"
        }

        if(size > 1) {
            subscription = subList[1]
            name = if (subscription.subName.length > 13) "${subscription.subName.substring(0, 13)}..." else subscription.subName
            expenseTwo.text = "2. $name,\n\t\t\t$${subscription.subCost} ${subscription.subFrequency}"
        }

        if(size > 2) {
            subscription = subList[2]
            name = if (subscription.subName.length > 13) "${subscription.subName.substring(0, 13)}..." else subscription.subName
            expenseThree.text = "3. $name,\n\t\t\t$${subscription.subCost} ${subscription.subFrequency}"
        }
    }

    /**
     * Convert a String cost to a double, removing dollar signs and commas
     *
     * @param subCost The cost of the subscription
     * @return The parsed subCost as a double (i.e. without commas)
     */
    private fun costToDouble(subCost: String): Double {
        if(subCost.isEmpty()) return 0.0

        var costDouble = subCost
        if(subCost[0] == '$') { costDouble = subCost.substring(1) }

        return costDouble.replace(",", "").toDouble()
    }

    /**
     * Convert a cost to its true monthly value based on payment frequency
     *
     * @param cost The cost value of the subscription
     * @param freq The frequency of the subscription, either Weekly, Monthly, or Yearly
     * @return The true monthly cost of the subscription
     */
    private fun trueCost(cost: Double, freq: String): Double {
        val costMultiplier = when(freq) {
            "Weekly" -> 4.0
            "Monthly" -> 1.0
            else -> 1.0 / 12.0
        }

        return cost * costMultiplier
    }

    companion object {
        fun newInstance(): BreakdownFragment = BreakdownFragment()
    }
}