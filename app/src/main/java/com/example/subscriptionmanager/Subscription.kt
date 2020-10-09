package com.example.subscriptionmanager

data class Subscription(
    var subName: String = "",
    var subCost: String = "",
    var subFrequency: String = "",
    var subType: String = "",
    var subImportance: String = "",
    var subDueDate: String = "",
)