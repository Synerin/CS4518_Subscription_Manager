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

        // Checking user's login status is the first thing done in the application
        // to avoid any security errors, and people being able to access the app
        // without signing in or signing up first
        handleSignIn();

        // Controls Bottom Navigation menu navigation, and triggers fragment
        // changes based on interaction with tabs
        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_calender -> {
                    title = "Calender"
                    loadFragment(CalendarFragment.newInstance())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_payments_breakdown -> {
                    title = "Payments Breakdown"
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_home -> {
                    title = "Subscription Manager"
                    loadFragment(HomeFragment.newInstance())
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

        bottomNav.selectedItemId = R.id.menu_home
    }

    /**
     * Sets up the Bottom Navigation menu
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu: this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_top_navigation, menu)
        return true
    }

    /**
     * Sign the user out upon clicking "Sign Out" button and triggers switch to SignInActivity
     */
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

    /**
     * A simple function which centralizes code required to switch fragments
     */
    private fun loadFragment(fragment: Fragment) {
        // load fragment
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /**
     * Checks to see if the user is currently signed in when they open the app.
     * If they are, nothing happens.  Otherwise, they are brought to the Sign In page
     */
    private fun handleSignIn() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if(currentUser == null) {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Disables the back button so that a user doesn't get brought to the login
     * screen upon clicking it.  This forces the user to use the Sign Out button
     * that is given.  The back button also isn't necessary given the structure
     * of our application, as it is all just one Activity with rotating fragments.
     */
    override fun onBackPressed() {}

    /**
     * Changes the currently selected tab to Add Payments and loads the
     * corresponding fragment with the given subscription ID.
     */
    override fun onSubSelected(subID: String) {
        bottomNav.selectedItemId = R.id.menu_edit_payments
        loadFragment(AddPaymentFragment.newInstance(subID))
    }

    /**
     * Changes the currently selected tab to Add Payments and loads the
     * corresponding fragment.
     */
    override fun onAddSelected() {
        bottomNav.selectedItemId = R.id.menu_edit_payments
        loadFragment(AddPaymentFragment())
    }
}