package com.example.subscriptionmanager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SubListFragment : Fragment() {
    private lateinit var subListRecyclerView: RecyclerView
    private lateinit var firedatabase : FirebaseDatabase
    private lateinit var myRef : DatabaseReference
    private lateinit var subList : ArrayList<Subscription>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sub_list, container, false)

        firedatabase = FirebaseDatabase.getInstance()
        subListRecyclerView = view.findViewById(R.id.sub_recycler_view) as RecyclerView
        subListRecyclerView.layoutManager = LinearLayoutManager(context)

        subList = arrayListOf<Subscription>()

        val userID = FirebaseAuth.getInstance().currentUser?.uid
        myRef = userID?.let { FirebaseDatabase.getInstance().getReference(it) }!!

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if(dataSnapshot.exists()){
                    for(item in dataSnapshot.children) {
                        val retrieveSub = item.getValue(Subscription::class.java)
                        if (retrieveSub != null) {
                            subList.add(retrieveSub)
                        }
                    }

                    val adapter = SubAdapter(subList)
                    subListRecyclerView.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })

        return view
    }

    private inner class SubHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = itemView.findViewById(R.id.sub_name);
        val costTextView: TextView = itemView.findViewById(R.id.sub_cost);
        val typeTextView: TextView = itemView.findViewById(R.id.sub_type);
        val importanceTextView: TextView = itemView.findViewById(R.id.sub_importance);
        val dueDateTextView: TextView = itemView.findViewById(R.id.sub_duedate);

        fun bind(sub: Subscription) {
            nameTextView.text = sub.subName
            costTextView.text = "${sub.subCost}/${sub.subFrequency}"
            typeTextView.text = "Type: ${sub.subType}"
            importanceTextView.text = sub.subImportance
            dueDateTextView.text = sub.subDueDate
        }
    }

    private inner class SubAdapter(var subs: ArrayList<Subscription>) :
        RecyclerView.Adapter<SubHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubHolder {
            val view = layoutInflater.inflate(R.layout.list_item_sub, parent, false)
            return SubHolder(view)
        }

        override fun onBindViewHolder(holder: SubHolder, position: Int) {
            holder.bind(subs[position])
        }

        override fun getItemCount() = subs.size
    }

    companion object {
        fun newInstance(): SubListFragment {
            return SubListFragment()
        }
    }
}