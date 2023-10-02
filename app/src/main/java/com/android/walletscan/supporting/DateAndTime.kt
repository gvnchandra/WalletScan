package com.android.walletscan.supporting

import java.text.SimpleDateFormat
import java.util.*

object DateAndTime {
    private var DATE_TIME_FORMAT =
        SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale.getDefault())
    var DAY_MONTH_YEAR_FORMAT = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    var MONTH_YEAR_FORMAT = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    var YEAR_FORMAT = SimpleDateFormat("yyyy", Locale.getDefault())

    val pastSevenDates = arrayOf(
        getSpecificDayBefore(-6),
        getSpecificDayBefore(-5),
        getSpecificDayBefore(-4),
        getSpecificDayBefore(-3),
        getSpecificDayBefore(-2),
        getSpecificDayBefore(-1),
        getSpecificDayBefore(0)
    )

    fun getDateTime(dateAndTime: Date): String {
        DATE_TIME_FORMAT.timeZone = TimeZone.getDefault()
        return DATE_TIME_FORMAT.format(dateAndTime)
    }

    private fun getSpecificDayBefore(days: Int): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, days)
        return cal.time
    }
}