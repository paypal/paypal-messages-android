package com.paypal.messages.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.paypal.messages.ModalFragment
import com.paypal.messages.analytics.AnalyticsEvent
import com.paypal.messages.analytics.EventType
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.modal.ModalConfig
import com.paypal.messages.config.modal.ModalEvents
import com.paypal.messages.io.Api
import com.paypal.messages.io.ApiMessageData
import com.paypal.messages.io.ApiResult
import com.paypal.messages.io.OnActionCompleted
import com.paypal.messages.utils.LogCat
import com.paypal.messages.utils.PayPalErrors
import java.util.UUID
import java.util.WeakHashMap

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
class PayPalMessageDataProvider {
	private val TAG = "PayPalMessageDataProvider"
	private val handler = Handler(Looper.getMainLooper())
	
	// Track modal instances by instanceId to prevent leaks and ensure cleanup
	private val modalInstances = WeakHashMap<UUID, ModalFragment>()

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
	
	/**
	 * Creates a click handler for the message
	 *
	 * @param context Android context
	 * @param config Message configuration
	 * @param instanceId Unique identifier for this message instance
	 * @param logEventCallback Optional callback to log analytics events
	 * @return PayPalMessageClickHandler implementation
	 */
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
				// Invoke onClick callback
				onClick.invoke()

				// Log click event if log callback is provided
				logEventCallback?.invoke(
					AnalyticsEvent(
						eventType = EventType.MESSAGE_CLICKED,
						pageViewLinkName = response.content?.default?.disclaimer ?: "Learn more",
						pageViewLinkSource = "learn_more",
					),
				)

				// Show modal
				showWebView(context, response, config, instanceId, onApply, onClick, onError)
			}

			override fun onCleanup() {
				// Clean up modal if it exists
				modalInstances[instanceId]?.dismiss()
				modalInstances.remove(instanceId)
			}
		}
	}
	
	/**
	 * Shows the web view modal
	 */
	private fun showWebView(
		context: Context,
		response: ApiMessageData.Response,
		config: PayPalMessageConfig,
		instanceId: UUID,
		onApply: () -> Unit,
		onClick: () -> Unit,
		onError: (PayPalErrors.Base) -> Unit,
	) {
		// Cast context to AppCompatActivity
		val activity = context as? AppCompatActivity
		if (activity == null) {
			LogCat.error(TAG, "Context is not an AppCompatActivity, cannot show modal")
			return
		}

		val modal = modalInstances[instanceId] ?: run {
			val newModal = ModalFragment(config.data.clientID)

			// Build modal config
			val modalConfig = ModalConfig(
				amount = config.data.amount,
				buyerCountry = config.data.buyerCountry,
				offer = response.meta?.offerType,
				ignoreCache = false,
				devTouchpoint = false,
				stageTag = null,
				events = ModalEvents(
					onApply = onApply,
					onClick = onClick,
					onError = onError,
				),
				modalCloseButton = response.meta?.modalCloseButton!!,
			)

			newModal.init(modalConfig)
			newModal.show(activity.supportFragmentManager, newModal.tag)

			// Store the modal instance
			modalInstances[instanceId] = newModal

			newModal
		}

		// modal.show() above will display the modal on initial view, but if the user closes the modal
		// it will become visually hidden and this method will re-display the modal without
		// attempting to reattach it
		// the delay prevents noticeable shift when the offer type is changed
		handler.postDelayed({
			modal.expand()
		}, 250)
	}
}
