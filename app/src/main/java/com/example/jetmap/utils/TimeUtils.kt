package com.example.jetmap.utils

object TimeUtils {
    private fun parseTime(timeStr: String): Int {
        return try {
            val parts = timeStr.split(AppConstants.TimePatterns.TIME_HOURS_MINS_SEPARATOR)
            if (parts.size == 2) {
                val hours = parts[0].toIntOrNull() ?: 0
                val minutes = parts[1].toIntOrNull() ?: 0
                hours * 60 + minutes
            } else {
                0
            }
        } catch (_: Exception) {
            0
        }
    }

    private fun isOvernightRange(start: String, end: String): Boolean {
        val startMinutes = parseTime(start)
        val endMinutes = parseTime(end)
        return endMinutes < startMinutes
    }

    private fun is24HoursOperation(timeRanges: List<String>): Boolean {
        // Check for direct 24/7 patterns
        val fullDayRanges = AppConstants.TimePatterns.FULL_DAY_RANGES

        if (timeRanges.any { it in fullDayRanges }) {
            return true
        }

        // Check for day/night ranges indicating 24/7 operation
        if (timeRanges.size >= 2) {
            val overnightRanges = timeRanges.filter { range ->
                val parts = range.split(AppConstants.TimePatterns.TIME_RANGE_DELIMITER)
                if (parts.size == 2) {
                    val start = parts[0].trim()
                    val end = parts[1].trim()
                    isOvernightRange(start, end)
                } else false
            }

            val daytimeRanges = timeRanges.filter { range ->
                val parts = range.split(AppConstants.TimePatterns.TIME_RANGE_DELIMITER)
                if (parts.size == 2) {
                    val start = parts[0].trim()
                    val end = parts[1].trim()
                    !isOvernightRange(start, end)
                } else false
            }

            if (overnightRanges.isNotEmpty() && daytimeRanges.isNotEmpty()) {
                return true
            }

            // Check if ranges cover full day
            val sortedRanges = timeRanges.sorted()
            val firstRange = sortedRanges.first()
            val lastRange = sortedRanges.last()

            val startsAtMidnight = firstRange.startsWith(AppConstants.TimePatterns.MIDNIGHT_24_HOUR) ||
                    firstRange.startsWith(AppConstants.TimePatterns.MIDNIGHT_12_HOUR)
            val endsAtMidnight = lastRange.endsWith(AppConstants.TimePatterns.END_OF_DAY_24_00) ||
                    lastRange.endsWith(AppConstants.TimePatterns.END_OF_DAY_23_59)

            if (startsAtMidnight && endsAtMidnight) {
                return true
            }
        }

        return false
    }

    fun extractOpeningHoursFromCharge(charge: String): String {
        val timePattern = Regex(AppConstants.TimePatterns.TIME_PATTERN)
        val matches = timePattern.findAll(charge)

        val timeRanges = matches.map { match ->
            val startTime = match.groupValues[1]
            val endTime = match.groupValues[2]
            "$startTime${AppConstants.TimePatterns.TIME_RANGE_DELIMITER}$endTime"
        }.toList()

        return if (timeRanges.isNotEmpty()) {
            // Check if it's 24/7 operation
            if (is24HoursOperation(timeRanges)) {
                AppConstants.TimePatterns.TWENTY_FOUR_SEVEN
            } else {
                // Join multiple time ranges
                timeRanges.joinToString(AppConstants.TimePatterns.TIME_RANGE_SEPARATOR)
            }
        } else {
            ""
        }
    }
}