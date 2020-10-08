package com.example.subscriptionmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SubListFragment : Fragment() {
    private lateinit var subListRecyclerView: RecyclerView
    private var adapter: SubAdapter? = null

    private val subListViewModel: SubListViewModel by lazy {
        ViewModelProviders.of(this).get(SubListViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sub_list, container, false)

        subListRecyclerView = view.findViewById(R.id.sub_recycler_view) as RecyclerView
        subListRecyclerView.layoutManager = LinearLayoutManager(context)

        updateUI()

        return view
    }

    private fun updateUI() {
        val subs = subListViewModel.subs
        adapter = SubAdapter(subs)
        subListRecyclerView.adapter = adapter
    }

    private inner class SubHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView : TextView = itemView.findViewById(R.id.sub_name);
        val costTextView : TextView = itemView.findViewById(R.id.sub_cost);
        val typeTextView : TextView = itemView.findViewById(R.id.sub_type);
        val importanceTextView : TextView = itemView.findViewById(R.id.sub_importance);
        val dueDateTextView : TextView = itemView.findViewById(R.id.sub_duedate);
    }

    private inner class SubAdapter(var subs: List<Subscription>) : RecyclerView.Adapter<SubHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubHolder {
            val view = layoutInflater.inflate(R.layout.list_item_sub, parent, false)
            return SubHolder(view)
        }

        override fun onBindViewHolder(holder: SubHolder, position: Int) {
            val sub = subs[position]
            holder.apply {
                nameTextView.text = sub.subName
                costTextView.text = "${sub.subCost}/${sub.subFrequency}"
                typeTextView.text = "Type: ${sub.subType}"
                importanceTextView.text = sub.subImportance
                dueDateTextView.text = sub.subDueDate
            }
        }

        override fun getItemCount() = subs.size

    }

    companion object {
        fun newInstance(): SubListFragment {
            return SubListFragment()
        }
    }
}