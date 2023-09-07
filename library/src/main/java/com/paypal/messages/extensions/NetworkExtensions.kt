package com.paypal.messages.extensions

import com.google.gson.Gson
import com.paypal.messages.utils.LogCat
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.io.StringReader
import kotlin.coroutines.resumeWithException

/**
 * Allows a [Call] to be handled like a coroutine
 *
 * @param responseClass is the [Class] that represents the [Call]'s response.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal suspend inline fun <T> Call.await(responseClass: Class<T>): T {
	return suspendCancellableCoroutine { continuation ->
		enqueue(object : Callback {
			override fun onResponse(call: Call, response: Response) {
				val stringResponse = response.body?.string() ?: ""
				try {
					val gsonResponse = Gson().fromJson(StringReader(stringResponse), responseClass)
					continuation.resume(gsonResponse) {}
				}
				catch (e: Exception) {
					continuation.resumeWithException(e)
				}
			}

			override fun onFailure(call: Call, e: IOException) {
				if (!call.isCanceled()) {
					continuation.resumeWithException(e)
				}
			}
		})
		continuation.invokeOnCancellation(object : CompletionHandler {
			override fun invoke(cause: Throwable?) {
				this@await.cancel()
			}
		})
	}
}

/**
 *  This extension will execute the call in a suspended coroutine using the IO Dispatcher.
 *  It will also take care of  handling non-IOExceptions, such as SocketTimeoutException.
 *
 *  Even when we are using a blocking method such as execute(), whe are doing so in
 *  a suspended coroutine in the IO Dispatcher. It's synchronous nature lets us handle OkHttp issues
 *  with uncaught exceptions.
 *
 *  related to : https://github.com/square/okhttp/issues/4380
 *  https://github.com/square/retrofit/issues/3171
 */
@Throws(IOException::class)
suspend inline fun <reified T> Call.executeSuspending(): T {
	val TAG = "NetworkExtension.executeSuspending"
	val call = this
	val klass = T::class.java
	return withContext(Dispatchers.IO) {
		var response: Response? = null
		try {
			response = call.execute()
			if (response.isSuccessful) {
				Gson().fromJson(StringReader(response.body?.string().orEmpty()), klass)
			}
			else {
				val code = response.code
				response.close()
				LogCat.error(TAG, "Network call failed. HTTP code: $code")
				throw IOException("Network Error: $code ")
			}
		}
		catch (e: Throwable) {
			LogCat.error(TAG, "Network call failed. ${e::class.java.simpleName}: ${e.message}")
			if (e is IOException) {
				throw e
			}
			else {
				throw IOException(e)
			}
		}
		finally {
			response?.close()
		}
	}
}

/**
 * Allows a [Call] to be handled like a coroutine
 */
internal suspend inline fun <reified T> Call.await(): T {
	return await(T::class.java)
}
