package com.example.subscriptionmanager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


private const val TAG = "SubListFragment"

class SubListFragment : Fragment(){

    interface Callbacks {
        fun onSubSelected(subID: String);
    }

    private var callbacks: Callbacks? = null

    private lateinit var subListRecyclerView: RecyclerView
    private lateinit var myRef: DatabaseReference
    private lateinit var subList: ArrayList<Subscription>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach() Called");
        callbacks = context as Callbacks?
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach() called");
        callbacks = null;
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sub_list, container, false)

        subListRecyclerView = view.findViewById(R.id.sub_recycler_view) as RecyclerView
        subListRecyclerView.layoutManager = LinearLayoutManager(context)

        subList = arrayListOf<Subscription>()

        val userID = FirebaseAuth.getInstance().currentUser?.uid
        myRef = userID?.let { FirebaseDatabase.getInstance().getReference(it) }!!

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                subList.clear()

                if (dataSnapshot.exists()) {
                    for (item in dataSnapshot.children) {
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

        myRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val removedBook: Subscription? = dataSnapshot.getValue(Subscription::class.java)
                for (i in 0 until subList.size) {
                    if (removedBook != null) {
                        if (subList[i].uniqueId == removedBook.uniqueId) {
                            subList.removeAt(i)
                            subListRecyclerView.adapter?.notifyItemRemoved(i)
                            break
                        }
                    }
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        setUpSwipe()

        return view
    }

    private inner class SubHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = itemView.findViewById(R.id.sub_name);
        val costTextView: TextView = itemView.findViewById(R.id.sub_cost);
        val typeTextView: TextView = itemView.findViewById(R.id.sub_type);
        val importanceTextView: TextView = itemView.findViewById(R.id.sub_importance);
        val dueDateTextView: TextView = itemView.findViewById(R.id.sub_duedate);

        lateinit var subID: String

        fun bind(sub: Subscription) {
            nameTextView.text = sub.subName
            costTextView.text = "${sub.subCost}/${sub.subFrequency}"
            typeTextView.text = "${sub.subType}"
            importanceTextView.text = sub.subImportance
            dueDateTextView.text = sub.subDueDate
            subID = sub.uniqueId
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

    private fun setUpSwipe() {
        val swipeDeleteCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                myRef.child(subList[viewHolder.adapterPosition].uniqueId).removeValue()
                subListRecyclerView.adapter?.notifyDataSetChanged()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val deleteIcon = resources.getDrawable(
                    R.drawable.delete_icon,
                    null
                )

                val display = activity!!.windowManager.defaultDisplay

                c.clipRect(
                    display.width.toFloat() + dX, viewHolder.itemView.top.toFloat(),
                    display.width.toFloat(), viewHolder.itemView.bottom.toFloat()
                )

                if(-dX < display.width / 2)
                    c.drawColor(Color.GRAY)
                else
                    c.drawColor(Color.RED)

                val offset = 60
                val top = viewHolder.itemView.top + offset
                val bottom = viewHolder.itemView.bottom - offset
                val right = display.width - 100
                val left = right - (bottom - top)

                deleteIcon.bounds = Rect(
                    left,
                    top,
                    right,
                    bottom
                )

                deleteIcon.draw(c)

                super.onChildDraw(
                    c, recyclerView, viewHolder,
                    dX, dY, actionState, isCurrentlyActive
                )
            }

        }

        val swipeEditCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                callbacks?.onSubSelected(subList[viewHolder.adapterPosition].uniqueId);
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val editIcon = resources.getDrawable(
                    R.drawable.edit_icon,
                    null
                )

                val display = activity!!.windowManager.defaultDisplay

                c.clipRect(
                    0f, viewHolder.itemView.top.toFloat(),
                    dX, viewHolder.itemView.bottom.toFloat()
                )

                if(dX < display.width / 2)
                    c.drawColor(Color.GRAY)
                else
                    c.drawColor(Color.rgb(234, 181, 15))

                val offset = 60
                val top = viewHolder.itemView.top + offset
                val bottom = viewHolder.itemView.bottom - offset
                val left = 100
                val right = left + bottom - top

                editIcon.bounds = Rect(
                    left,
                    top,
                    right,
                    bottom
                )

                editIcon.draw(c)

                super.onChildDraw(
                    c, recyclerView, viewHolder,
                    dX, dY, actionState, isCurrentlyActive
                )
            }

        }

        val swipeEditHelper = ItemTouchHelper(swipeEditCallback)
        swipeEditHelper.attachToRecyclerView(subListRecyclerView)
        val swipeDeleteHelper = ItemTouchHelper(swipeDeleteCallback)
        swipeDeleteHelper.attachToRecyclerView(subListRecyclerView)
    }

    companion object {
        fun newInstance(): SubListFragment {
            return SubListFragment()
        }
    }
}