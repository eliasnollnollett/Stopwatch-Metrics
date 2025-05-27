package com.example.stopwatchmetrics

import kotlin.math.sqrt

fun calculateMedian(times: List<Long>): Double {
    if (times.isEmpty()) return 0.0
    val sorted = times.sorted()
    return if (sorted.size % 2 == 1) {
        sorted[sorted.size / 2].toDouble()
    } else {
        (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
    }
}

fun calculateStdDev(times: List<Long>): Double {
    if (times.isEmpty()) return 0.0
    val avg = times.average()
    val variance = times.map { (it - avg) * (it - avg) }.average()
    return sqrt(variance)
}