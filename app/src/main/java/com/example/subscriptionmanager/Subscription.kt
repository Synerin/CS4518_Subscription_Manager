package com.example.subscriptionmanager

import java.util.*

data class Subscription(
    var uniqueId: String = UUID.randomUUID().toString(),
    var subName: String = "",
    var subCost: String = "",
    var subFrequency: String = "",
    var subType: String = "",
    var subImportance: String = "",
    var subDueDate: String = "",
)