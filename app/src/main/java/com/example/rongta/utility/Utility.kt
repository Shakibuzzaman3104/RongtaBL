package com.example.rongta.utility

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

object Utility {
    fun doubleFormatter(number: Double): String {
        val locale = Locale("en")
        Locale.setDefault(locale)
        val formatter: NumberFormat = DecimalFormat("#0.00")
        return formatter.format(number)
    }
}