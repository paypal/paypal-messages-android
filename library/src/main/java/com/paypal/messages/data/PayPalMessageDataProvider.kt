package com.paypal.messages.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.paypal.messages.analytics.AnalyticsEvent
import com.paypal.messages.analytics.EventType
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.io.Api
import com.paypal.messages.io.ApiMessageData
import com.paypal.messages.io.ApiResult
import com.paypal.messages.io.OnActionCompleted
import com.paypal.messages.utils.LogCat
import com.paypal.messages.utils.PayPalErrors
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

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
 * Interface for message click events
 */
interface PayPalMessageClickHandler {
	/**
	 * Called when a user clicks on the message
	 *
	 * @param response The message data response
	 * @param onClick Optional onClick callback supplied by the client
	 * @param onApply Optional onApply callback supplied by the client
	 * @param onError Optional onError callback supplied by the client
	 */
	fun onMessageClick(
		response: ApiMessageData.Response,
		onClick: () -> Unit,
		onApply: () -> Unit,
		onError: (PayPalErrors.Base) -> Unit,
	)

	/**
	 * Called when view is being detached or destroyed
	 */
	fun onCleanup()
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
 * - Modal display management
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
class PayPalMessageDataProvider(
	private val modalDisplayManager: ModalDisplayManager = ModalDisplayManager(),
) {
	private val TAG = "PayPalMessageDataProvider"
	private val handler = Handler(Looper.getMainLooper())

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
					handleApiResult(result, startTime, callback)
				}
			},
		)
	}

	/**
	 * Handles the API result and invokes the appropriate callback.
	 * Extracted as a separate method for better testability and coverage.
	 *
	 * @param result The API result (Success or Failure)
	 * @param startTime The timestamp when the request started
	 * @param callback The callback to invoke with the result
	 */
	internal fun handleApiResult(
		result: ApiResult,
		startTime: Long,
		callback: PayPalMessageDataCallback,
	) {
		val requestDuration = (System.currentTimeMillis() - startTime).toInt()
		when (result) {
			is ApiResult.Success<*> -> {
				LogCat.debug(TAG, "Message data fetch successful")
				@Suppress("UNCHECKED_CAST")
				callback.onSuccess(result.response as ApiMessageData.Response, requestDuration)
			}
			is ApiResult.Failure<*> -> {
				LogCat.debug(TAG, "Message data fetch failed: ${result.error?.message}")
				// Always invoke error callback to ensure terminal state
				val error = result.error ?: PayPalErrors.FailedToFetchDataException("Unknown error occurred", null)
				callback.onError(error)
			}
		}
	}
	
	/**
	 * Creates a click handler for the message
	 *
	 * @param context Android context
	 * @param config Message configuration
	 * @param instanceId Unique identifier for this message instance
	 * @param logEventCallback Optional callback to log analytics events
	 * @return PayPalMessageClickHandler implementation
	 */
	// Track active click handlers with timestamps to prevent duplicate modal displays
	private val activeClickHandlers = ConcurrentHashMap<UUID, Long>()
	
	fun createClickHandler(
		context: Context,
		config: PayPalMessageConfig,
		instanceId: UUID,
		logEventCallback: ((AnalyticsEvent) -> Unit)? = null,
	): PayPalMessageClickHandler {
		return object : PayPalMessageClickHandler {
			override fun onMessageClick(
				response: ApiMessageData.Response,
				onClick: () -> Unit,
				onApply: () -> Unit,
				onError: (PayPalErrors.Base) -> Unit,
			) {
				// Debugging call to verify handler is being invoked
				LogCat.debug(TAG, "onMessageClick called for instance: $instanceId with response: ${response != null}")
				
				// Check if we've processed a click recently (within 1 second) to prevent double-modals
				val currentTime = System.currentTimeMillis()
				val lastClickTime = activeClickHandlers[instanceId]
				
				if (lastClickTime != null && currentTime - lastClickTime < 1000) {
					LogCat.debug(TAG, "Ignoring click - too soon after previous click (${currentTime - lastClickTime}ms)")
					return
				}
				
				// Record this click time
				activeClickHandlers[instanceId] = currentTime
				
				try {
					// Generate a unique ID for tracing this click event
					val clickTraceId = UUID.randomUUID().toString().substring(0, 8)
					LogCat.debug(TAG, "[$clickTraceId] Processing message click for instanceId: $instanceId")
					
					// Invoke onClick callback once
					onClick.invoke()
	
					// Log click event if log callback is provided
					logEventCallback?.invoke(
						AnalyticsEvent(
							eventType = EventType.MESSAGE_CLICKED,
							pageViewLinkName = response.content?.default?.disclaimer ?: "Learn more",
							pageViewLinkSource = "learn_more",
						),
					)
	
					// Show modal - delegate to ModalDisplayManager
					LogCat.debug(TAG, "[$clickTraceId] Showing modal for instanceId: $instanceId")
					
					// Before showing the modal, clear any existing PayPalModalActivity with the same instance ID
					try {
						com.paypal.messages.PayPalModalActivity.resetAllModals()
						LogCat.debug(TAG, "[$clickTraceId] Reset all modal state")
					} catch (e: Exception) {
						LogCat.debug(TAG, "[$clickTraceId] Error resetting modal state: ${e.message}")
					}
					
					modalDisplayManager.showModal(context, response, config, instanceId, onApply, onClick, onError)
					LogCat.debug(TAG, "[$clickTraceId] Modal display request complete")
				} catch (e: Exception) {
					// Log any errors that occur
					LogCat.error(TAG, "Error showing modal: ${e.message}")
					onError.invoke(PayPalErrors.ModalFailedToLoad("Failed to show modal: ${e.message}", null))
				} finally {
					// Reset click handling state after a longer delay
					handler.postDelayed({
						// We don't remove the entry, just update it with the completion time
						// This helps prevent double-modals while still allowing future clicks
						activeClickHandlers[instanceId] = System.currentTimeMillis()
						LogCat.debug(TAG, "Click handler state updated for instanceId: $instanceId")
					}, 1000)
				}
			}

			override fun onCleanup() {
				// Delegate cleanup to ModalDisplayManager
				modalDisplayManager.cleanupModal(instanceId)
			}
		}
	}
}
