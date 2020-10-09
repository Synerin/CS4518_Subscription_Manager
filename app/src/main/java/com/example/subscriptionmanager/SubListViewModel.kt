package com.example.subscriptionmanager

import androidx.lifecycle.ViewModel

class SubListViewModel : ViewModel() {
    val subs = mutableListOf<Subscription>()

    init {
        for(i in 0 until 20) {
            val sub = Subscription();
            sub.subName = "Prime"
            sub.subCost = "49.99"
            sub.subDueDate = "5/12"
            sub.subFrequency = "year"
            sub.subImportance = "!!!"
            sub.subType = "Shopping"
            subs += sub
        }
    }
}