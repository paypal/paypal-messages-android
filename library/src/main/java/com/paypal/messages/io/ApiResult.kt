package com.paypal.messages.io

import com.google.gson.Gson
import com.paypal.messages.utils.PayPalErrors
import okhttp3.Headers

sealed class ApiResult {
	open class ApiResponse {
		fun toJson(): String {
			return Gson().toJson(this)
		}
	}

	data class Success<T : ApiResponse>(val response: T) : ApiResult()
	data class Failure<T : PayPalErrors.Base?>(val error: T) : ApiResult()

	companion object {
		fun getFailureWithDebugId(headers: Headers): ApiResult {
			val headersMap = headers.toMultimap()
			val error = headersMap["Paypal-Debug-Id"]?.firstOrNull()?.let {
				PayPalErrors.InvalidResponseException("", it)
			}
			return Failure(error)
		}
	}
}
