package com.example.subscriptionmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav.selectedItemId = R.id.menu_home

        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_calender -> {
                    title="Calender"
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_payments_breakdown -> {
                    title="Payments Breakdown"
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_home -> {
                    title="Subscription Manager"
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_view_payments -> {
                    title="View Payments"
                    loadFragment(SubListFragment.newInstance())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_edit_payments -> {
                    title="Edit Payments"
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        // load fragment
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}