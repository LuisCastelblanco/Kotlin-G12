package com.example.explorandes.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Parcelize
data class Event(
    val id: Long,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val type: String?,
    val startTime: String,
    val endTime: String,
    val locationId: Long?,
    val locationName: String?
) : Parcelable {

    fun getFormattedStartTime(): String {
        return formatDateTime(startTime)
    }

    fun getFormattedEndTime(): String {
        return formatDateTime(endTime)
    }

    fun getFormattedDate(): String {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val dateTime = LocalDateTime.parse(startTime, formatter)
        return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    }

    fun getFormattedTimeRange(): String {
        val startFormatter = DateTimeFormatter.ISO_DATE_TIME
        val startDateTime = LocalDateTime.parse(startTime, startFormatter)
        val endDateTime = LocalDateTime.parse(endTime, startFormatter)

        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        return "${startDateTime.format(timeFormatter)} - ${endDateTime.format(timeFormatter)}"
    }

    // Check if this event is happening now
    fun isHappeningNow(): Boolean {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val now = LocalDateTime.now()
        val start = LocalDateTime.parse(startTime, formatter)
        val end = LocalDateTime.parse(endTime, formatter)

        return (now.isAfter(start) || now.isEqual(start)) &&
                (now.isBefore(end) || now.isEqual(end))
    }

    // Check if this event is in the future
    fun isUpcoming(): Boolean {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val now = LocalDateTime.now()
        val start = LocalDateTime.parse(startTime, formatter)

        return start.isAfter(now)
    }

    // Check if this event is in the past
    fun isPast(): Boolean {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val now = LocalDateTime.now()
        val end = LocalDateTime.parse(endTime, formatter)

        return end.isBefore(now)
    }

    private fun formatDateTime(dateTimeStr: String): String {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val dateTime = LocalDateTime.parse(dateTimeStr, formatter)
        return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' h:mm a"))
    }

    companion object {
        const val TYPE_EVENT = "event"
        const val TYPE_MOVIE = "movies"
        const val TYPE_SPORTS = "sports"

        // For filtering purposes
        val TYPES = listOf(TYPE_EVENT, TYPE_MOVIE, TYPE_SPORTS)
    }
}