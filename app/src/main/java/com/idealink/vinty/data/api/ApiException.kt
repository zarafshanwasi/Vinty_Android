package com.idealink.vinty.data.api

class ApiException(
    val code: Int,
    val errorBody: String?,
    message: String
) : Exception(message)
