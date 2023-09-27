package com.paypal.messages.io

import com.google.gson.Gson
import com.paypal.messages.errors.BaseException
import com.paypal.messages.errors.InvalidResponseException
import okhttp3.Headers

sealed class ApiResult {
	open class ApiResponse {
		fun toJson(): String {
			return Gson().toJson(this)
		}
	}

	val isSuccess: Boolean
		get() = this is Success<*>

	data class Success<T : ApiResponse>(val response: T) : ApiResult()
	data class Failure<T : BaseException?>(val error: T) : ApiResult()

	companion object {
		fun getFailureWithDebugId(headers: Headers): ApiResult {
			val headersMap = headers.toMultimap()
			val error = headersMap["Paypal-Debug-Id"]?.firstOrNull()?.let { InvalidResponseException(it) }
			return Failure(error)
		}
	}
}
