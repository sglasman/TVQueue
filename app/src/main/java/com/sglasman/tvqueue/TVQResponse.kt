package com.sglasman.tvqueue

import retrofit2.Response

sealed class TVQResponse<T> {
    class Success<T>(val value: T) : TVQResponse<T>()
    class Error<T>(val errorCode: Int, val message: String) : TVQResponse<T>()

    fun <U> map(f: (T) -> U): TVQResponse<U> = when (this) {
        is Success -> Success(f(value))
        is Error -> Error(errorCode, message)
    }
}

fun <T> Response<T>.getTVQResponse(): TVQResponse<T> = body()?.let {
    if (this.isSuccessful) TVQResponse.Success(it)
    else TVQResponse.Error(code(), errorBody()?.toString().orEmpty())
} ?: TVQResponse.Error(code(), "Body was empty")