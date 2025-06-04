package com.paypal.messages.data

import android.content.Context
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.io.Api
import com.paypal.messages.io.ApiMessageData
import com.paypal.messages.io.ApiResult
import com.paypal.messages.io.OnActionCompleted
import com.paypal.messages.utils.LogCat
import com.paypal.messages.utils.PayPalErrors
import java.util.UUID

/**
 * Callback interface for message data fetch results.
 * This interface is implemented by both XML views and Compose components to receive
 * updates about the message data fetching process.
 *
 * The callback methods are always called in this order:
 * 1. [onLoading] - Called immediately when fetch starts
 * 2. Either:
 *    - [onSuccess] - When data is successfully fetched
 *    - [onError] - If an error occurs during fetch
 */
interface PayPalMessageDataCallback {
	/**
	 * Called when the data fetch process begins.
	 * Implementations should show a loading state.
	 */
	fun onLoading()

	/**
	 * Called when data is successfully fetched.
	 *
	 * @param response The message data from the API
	 * @param requestDuration The time taken to fetch the data in milliseconds
	 */
	fun onSuccess(response: ApiMessageData.Response, requestDuration: Int)

	/**
	 * Called when an error occurs during data fetch.
	 *
	 * @param error The error that occurred
	 */
	fun onError(error: PayPalErrors.Base)
}

/**
 * Handles fetching PayPal message data. This class encapsulates all data fetching logic
 * and provides a clean interface for both XML views and Jetpack Compose to request and
 * receive message data.
 *
 * Features:
 * - Thread-safe implementation
 * - Consistent error handling
 * - Performance monitoring
 * - Support for both XML and Compose UIs
 *
 * Example usage with XML:
 * ```
 * val provider = PayPalMessageDataProvider()
 * provider.fetchMessageData(context, config, UUID.randomUUID(), callback)
 * ```
 *
 * Example usage with Compose:
 * ```
 * val provider = remember { PayPalMessageDataProvider() }
 * LaunchedEffect(config) {
 *     provider.fetchMessageData(context, config, instanceId, callback)
 * }
 * ```
 */
class PayPalMessageDataProvider {
	private val TAG = "PayPalMessageDataProvider"

	/**
	 * Fetches message data using the provided configuration
	 * @param context Android context
	 * @param config Message configuration containing style, data, and callbacks
	 * @param instanceId Unique identifier for this message instance
	 * @param callback Interface to receive the data fetch results
	 */
	fun fetchMessageData(
		context: Context,
		config: PayPalMessageConfig,
		instanceId: UUID,
		callback: PayPalMessageDataCallback,
	) {
		callback.onLoading()
		LogCat.debug(TAG, "Fetching message data with config: $config")

		val startTime = System.currentTimeMillis()
		Api.getMessageWithHash(
			context,
			config,
			instanceId,
			object : OnActionCompleted {
				override fun onActionCompleted(result: ApiResult) {
					val requestDuration = (System.currentTimeMillis() - startTime).toInt()
					when (result) {
						is ApiResult.Success<*> -> {
							LogCat.debug(TAG, "Message data fetch successful")
							@Suppress("UNCHECKED_CAST")
							callback.onSuccess(result.response as ApiMessageData.Response, requestDuration)
						}
						is ApiResult.Failure<*> -> {
							LogCat.debug(TAG, "Message data fetch failed")
							result.error?.let { callback.onError(it) }
						}
					}
				}
			},
		)
	}
}
