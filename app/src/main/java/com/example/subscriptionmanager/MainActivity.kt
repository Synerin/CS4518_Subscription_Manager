package com.example.subscriptionmanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), SubListFragment.Callbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handleSignIn();

        bottomNav.selectedItemId = R.id.menu_home

        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_calender -> {
                    title = "Calender"
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_payments_breakdown -> {
                    title = "Payments Breakdown"
                    loadFragment(BreakdownFragment.newInstance())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_home -> {
                    title = "Subscription Manager"
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_view_payments -> {
                    title = "View Payments"
                    loadFragment(SubListFragment.newInstance())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_edit_payments -> {
                    title = "Add/Edit Payments"
                    loadFragment(AddPaymentFragment.newInstance())
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_top_navigation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_signout -> {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        // load fragment
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun handleSignIn() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if(currentUser == null) {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {}

    override fun onSubSelected(subID: String) {
        bottomNav.selectedItemId = R.id.menu_edit_payments
        loadFragment(AddPaymentFragment.newInstance(subID))
    }

    override fun onAddSelected() {
        bottomNav.selectedItemId = R.id.menu_edit_payments
        loadFragment(AddPaymentFragment())
    }
}