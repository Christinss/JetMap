package com.example.jetmap.ui

sealed class ErrorMessage {
    object LocationFailed : ErrorMessage()
    data class NetworkErrorWithMessage(val code: Int, val message: String) : ErrorMessage()
    object NetworkError : ErrorMessage()
    data class NetworkExceptionWithMessage(val message: String) : ErrorMessage()
    object NetworkException : ErrorMessage()
}
