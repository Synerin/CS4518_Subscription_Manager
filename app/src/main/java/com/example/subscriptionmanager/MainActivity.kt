package com.example.subscriptionmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> {
                    true
                }
                R.id.menu_calender -> {
                    true
                }
                R.id.menu_edit_payments -> {
                    true
                }
                R.id.menu_payments_breakdown -> {
                    true
                }
                R.id.menu_view_payments -> {
                    true
                }
                else -> false
            }
        }
    }
}