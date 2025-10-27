package com.example.jetmap.utils

/**
 * Application-wide constants organized by functionality.
 * This file contains all magic numbers, strings, and configuration values
 * used throughout the application.
 */
object AppConstants {
    
    // NETWORK CONFIGURATION
    object Network {
        const val CONNECT_TIMEOUT_MINUTES = 1L
        const val READ_TIMEOUT_MINUTES = 1L
        const val WRITE_TIMEOUT_MINUTES = 1L
    }
    
    // MAP CONFIGURATION
    object Map {
        const val USER_ZOOM_LEVEL = 13f
        const val ANIMATION_DURATION_MS = 1000
        const val CLUSTER_ZOOM_PADDING = 100
        const val GOOGLE_PLAY_SERVICES_ERROR_DIALOG_REQUEST_CODE = 9000
    }
    
    // PARKING API CONFIGURATION
    object ParkingApi {
        const val COORDINATE_SEPARATOR = ","
        const val PARKING_CATEGORIES = "parking.cars"
        const val PARKING_LIMIT = 20
        const val FILTER_PREFIX = "rect:"
        const val EXPECTED_COORDINATES_COUNT = 4
    }
    
    // DATA VALIDATION
    object Validation {
        const val PARKING_FILTER = "parking"
        const val UNKNOWN_NAME = "Unknown"
    }
    
    // FALLBACK VALUES
    object Fallbacks {
        const val DEFAULT_PARKING_NAME = "Unknown Parking"
        const val DEFAULT_ADDRESS = "Unknown Address"
        const val DEFAULT_CITY = "Unknown City"
        const val DEFAULT_COUNTRY = "Unknown Country"
        const val DEFAULT_HOURS = "Not specified"
    }
    
    // ERROR HANDLING
    object Errors {
        const val INVALID_BOUNDING_BOX_ERROR = "Invalid bounding box format"
        const val INVALID_BOUNDING_BOX_CODE = 400
    }
    
    // TIME PATTERNS
    object TimePatterns {
        // 24/7 Detection Patterns
        val FULL_DAY_RANGES = setOf(
            "00:00-24:00", "00:00-23:59", "24:00-24:00",
            "0:00-24:00", "0:00-23:59"
        )
        
        // Day Ranges
        const val MIDNIGHT_24_HOUR = "00:00"
        const val MIDNIGHT_12_HOUR = "0:00"
        const val END_OF_DAY_24_00 = "24:00"
        const val END_OF_DAY_23_59 = "23:59"
        
        // Time Pattern Regex
        const val TIME_PATTERN = "([0-9]{1,2}:[0-9]{2})\\s*-\\s*([0-9]{1,2}:[0-9]{2})"
        
        // 24/7 Display
        const val TWENTY_FOUR_SEVEN = "24/7"
        const val TIME_RANGE_SEPARATOR = "; "
        const val TIME_RANGE_DELIMITER = "-"
        const val TIME_HOURS_MINS_SEPARATOR = ":"
    }
}
