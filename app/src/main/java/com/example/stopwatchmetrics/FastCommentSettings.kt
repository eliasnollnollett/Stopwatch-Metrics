package com.example.stopwatchmetrics

data class FastCommentsSettings(
    val enabled: Boolean = false,
    val comments: List<String> = listOf(
        "In",
        "Transport",
        "Out",
        "Return",
        "Wait In",
        "Wait Out"
    )
)