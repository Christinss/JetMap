package com.example.jetmap.ui

sealed class ErrorMessage {
    object LocationFailed : ErrorMessage()
    data class NetworkErrorInfo(val code: Int, val message: String) : ErrorMessage()
    object NetworkError : ErrorMessage()
    data class NetworkExceptionInfo(val message: String) : ErrorMessage()
    object NetworkException : ErrorMessage()
}
