package com.example.subscriptionmanager

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class BreakdownFragment: Fragment() {
    private lateinit var firedatabase: FirebaseDatabase
    private lateinit var annualSpending: TextView
    private lateinit var monthlySpending: TextView
    private lateinit var weeklySpending: TextView
    private lateinit var myRef : DatabaseReference
    private var annualTotal: Double = 0.0
    private var monthlyTotal: Double = 0.0
    private var weeklyTotal: Double = 0.0
    private lateinit var dailyCostList: RecyclerView
    private var subList: MutableList<Subscription> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_breakdown, container, false)

        annualSpending = view.findViewById(R.id.annual_total_value)
        monthlySpending = view.findViewById(R.id.monthly_total_value)
        weeklySpending = view.findViewById(R.id.weekly_total_value)

        dailyCostList = view.findViewById(R.id.daily_cost_list) as RecyclerView
        dailyCostList.layoutManager = LinearLayoutManager(context)

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
                            calculateStats(sub.subCost, sub.subFrequency)
                            subList.add(sub)
                        }
                    }

                    calculateTop()

                    val adapter = SubAdapter(subList)
                    dailyCostList.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        return view
    }

    /**
     * Calculate the annual, monthly, and weekly expense for a given subscription, adding to the totals
     *
     * @param subCost The cost of the subscription
     * @param subFrequency The frequency of the subscription, either Weekly, Monthly, or Yearly
     * @return Nothing, but add the calculated stats cost to their totals
     */
    private fun calculateStats(subCost: String, subFrequency: String) {
        // Calculate costs for time periods based on subscription cost and subscription frequency
        val trueCost = trueCost(costToDouble(subCost), subFrequency)
        annualTotal += trueCost * 365
        monthlyTotal += trueCost * 30
        weeklyTotal += trueCost * 7

        // Format string to monetary units, e.g. 1234.5 => 1,234.50
        val annualTextTotal = moneyToStr(annualTotal)
        val monthlyTextTotal = moneyToStr(monthlyTotal)
        val weeklyTextTotal = moneyToStr(weeklyTotal)

        annualSpending.text = "$${annualTextTotal}"
        monthlySpending.text = "$${monthlyTextTotal}"
        weeklySpending.text = "$${weeklyTextTotal}"
    }

    /**
     * Take a double and format it into a USD string, i.e. x,xxx.xx
     *
     * @param value The money value as a double
     * @return The formatted money string (no dollar sign)
     */
    private fun moneyToStr(value: Double): String {
        return String.format("%,.2f", value)
    }
    /**
     * Calculate and set the top three expenses from the list of subscriptions
     */
    private fun calculateTop() {
        subList.sortByDescending { trueCost(costToDouble(it.subCost), it.subFrequency) }
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
            "Weekly" -> 1.0 / 7.0
            "Monthly" -> 1.0 / 30.0
            else -> 1.0 / 365.0
        }

        return cost * costMultiplier
    }

    private inner class SubHolder(view: View):
        RecyclerView.ViewHolder(view) {
        val miniSubName: TextView = itemView.findViewById(R.id.mini_sub_name)
        val miniSubCost: TextView = itemView.findViewById(R.id.mini_sub_cost)
        val miniSubDue: TextView = itemView.findViewById(R.id.mini_sub_due)

        fun bind(sub: Subscription) {
            miniSubName.text = sub.subName
            miniSubCost.text = "$${moneyToStr(trueCost(costToDouble(sub.subCost), sub.subFrequency))} per Day"
            miniSubCost.gravity = Gravity.END
            miniSubCost.textSize = 20.0F
            miniSubDue.visibility = View.GONE
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
        fun newInstance(): BreakdownFragment = BreakdownFragment()
    }
}