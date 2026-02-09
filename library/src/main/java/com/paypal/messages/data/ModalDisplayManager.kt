package com.paypal.messages.data

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.paypal.messages.ModalFragment
import com.paypal.messages.PayPalModalActivity
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.modal.ModalConfig
import com.paypal.messages.config.modal.ModalEvents
import com.paypal.messages.io.ApiMessageData
import com.paypal.messages.utils.LogCat
import com.paypal.messages.utils.PayPalErrors
import java.util.UUID
import java.util.WeakHashMap

/**
 * Handles displaying modal views for PayPal messages.
 * This class is responsible for all UI/Activity/Fragment interactions
 * and is excluded from code coverage as it requires instrumentation tests.
 *
 * Supports multiple context types:
 * - AppCompatActivity: Uses ModalFragment
 * - ComponentActivity/JetpackActivity: Uses PayPalModalActivity
 * - Application context: Uses PayPalModalActivity with FLAG_ACTIVITY_NEW_TASK
 */
class ModalDisplayManager {
	private val TAG = "ModalDisplayManager"
	private val handler = Handler(Looper.getMainLooper())

	// Track modal instances by instanceId to prevent leaks and ensure cleanup
	private val modalInstances = WeakHashMap<UUID, ModalFragment>()

	/**
	 * Shows the modal for a PayPal message.
	 *
	 * @param context Android context
	 * @param response API response containing modal data
	 * @param config Message configuration
	 * @param instanceId Unique identifier for this message instance
	 * @param onApply Callback when user applies for credit
	 * @param onClick Callback when modal is clicked
	 * @param onError Callback when an error occurs
	 */
	fun showModal(
		context: Context,
		response: ApiMessageData.Response,
		config: PayPalMessageConfig,
		instanceId: UUID,
		onApply: () -> Unit,
		onClick: () -> Unit,
		onError: (PayPalErrors.Base) -> Unit,
	) {
		LogCat.debug(TAG, "Showing modal with context: ${context.javaClass.simpleName}")
		// Try to find a usable AppCompatActivity from the context
		val appCompatContext = com.paypal.messages.utils.ContextCompatWrapper.findAppCompatActivity(context)
		LogCat.debug(TAG, "Found AppCompatActivity: $appCompatContext")

		when {
			// For JetpackActivity or other ComponentActivity, use PayPalModalActivity
			context.javaClass.simpleName == "JetpackActivity" ||
				(context is androidx.activity.ComponentActivity && context !is androidx.appcompat.app.AppCompatActivity) -> {
				showWithModalActivity(context, response, config, instanceId, onApply, onClick, onError)
			}
			// For AppCompatActivity contexts, use ModalFragment
			appCompatContext != null -> {
				showWithFragment(appCompatContext, response, config, instanceId, onApply, onError)
			}
			// For other ComponentActivity contexts
			context is androidx.activity.ComponentActivity &&
				context !is androidx.fragment.app.FragmentActivity &&
				context !is androidx.appcompat.app.AppCompatActivity &&
				context.javaClass.simpleName != "JetpackActivity" -> {
				showWithModalActivityForComponentActivity(context, response, config, instanceId, onApply, onClick, onError)
			}
			// Fallback for any other context type
			else -> {
				showWithModalActivityFallback(context, response, config, instanceId, onApply, onClick, onError)
			}
		}
	}

	/**
	 * Cleans up modal for the given instance.
	 */
	fun cleanupModal(instanceId: UUID) {
		val modal = modalInstances[instanceId]
		if (modal != null) {
			try {
				if (modal.isAdded) {
					modal.dismiss()
				}
			} catch (e: IllegalStateException) {
				LogCat.debug(TAG, "Fragment no longer associated with fragment manager during cleanup: ${e.message}")
			} catch (e: Exception) {
				LogCat.error(TAG, "Error dismissing modal during cleanup: ${e.message}")
			}
			modalInstances.remove(instanceId)
		}
	}

	private fun showWithModalActivity(
		context: Context,
		response: ApiMessageData.Response,
		config: PayPalMessageConfig,
		instanceId: UUID,
		onApply: () -> Unit,
		onClick: () -> Unit,
		onError: (PayPalErrors.Base) -> Unit,
	) {
		LogCat.debug(TAG, "Using PayPalModalActivity approach for ${context.javaClass.simpleName}")

		try {
			val intent = Intent(context, PayPalModalActivity::class.java).apply {
				putExtra("CLIENT_ID", config.data.clientID)
				putExtra("AMOUNT", config.data.amount)
				putExtra("BUYER_COUNTRY", config.data.buyerCountry)
				putExtra("OFFER_TYPE", response.meta?.offerType?.toString())
				putExtra("INSTANCE_ID", instanceId.toString())
				addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
				putExtra("FORCE_NEW", true)
			}

			context.startActivity(intent)

			PayPalModalActivity.registerCallbacks(
				instanceId = instanceId,
				onApply = onApply,
				onClick = onClick,
				onError = onError,
			)

			LogCat.debug(TAG, "Successfully launched modal activity")
		} catch (e: Exception) {
			LogCat.error(TAG, "Failed to show modal: ${e.message}")
			onError.invoke(PayPalErrors.ModalFailedToLoad("Failed to show modal: ${e.message}", null))
		}
	}

	private fun showWithFragment(
		appCompatContext: androidx.appcompat.app.AppCompatActivity,
		response: ApiMessageData.Response,
		config: PayPalMessageConfig,
		instanceId: UUID,
		onApply: () -> Unit,
		onError: (PayPalErrors.Base) -> Unit,
	) {
		val existingModal = modalInstances[instanceId]
		val modal = if (existingModal != null && existingModal.isAdded) {
			existingModal
		} else {
			if (existingModal != null) {
				modalInstances.remove(instanceId)
			}

			val newModal = ModalFragment.newInstance(config.data.clientID)

			val modalConfig = ModalConfig(
				amount = config.data.amount,
				buyerCountry = config.data.buyerCountry,
				offer = response.meta?.offerType,
				ignoreCache = false,
				devTouchpoint = false,
				stageTag = null,
				language = config.data.language,
				locale = config.data.locale,
				events = ModalEvents(
					onApply = onApply,
					onClick = { /* onClick is handled earlier */ },
					onError = onError,
				),
				modalCloseButton = response.meta?.modalCloseButton!!,
			)

			newModal.init(modalConfig)
			newModal.show(appCompatContext.supportFragmentManager, newModal.tag)

			modalInstances[instanceId] = newModal

			newModal
		}

		handler.postDelayed({
			modal.expand()
		}, 250)
	}

	private fun showWithModalActivityForComponentActivity(
		context: androidx.activity.ComponentActivity,
		response: ApiMessageData.Response,
		config: PayPalMessageConfig,
		instanceId: UUID,
		onApply: () -> Unit,
		onClick: () -> Unit,
		onError: (PayPalErrors.Base) -> Unit,
	) {
		val intent = Intent(context, PayPalModalActivity::class.java).apply {
			putExtra("CLIENT_ID", config.data.clientID)
			putExtra("AMOUNT", config.data.amount)
			putExtra("BUYER_COUNTRY", config.data.buyerCountry)
			putExtra("OFFER_TYPE", response.meta?.offerType)
			putExtra("INSTANCE_ID", instanceId.toString())
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
			addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
		}
		context.startActivity(intent)

		PayPalModalActivity.registerCallbacks(
			instanceId = instanceId,
			onApply = onApply,
			onClick = onClick,
			onError = onError,
		)
	}

	private fun showWithModalActivityFallback(
		context: Context,
		response: ApiMessageData.Response,
		config: PayPalMessageConfig,
		instanceId: UUID,
		onApply: () -> Unit,
		onClick: () -> Unit,
		onError: (PayPalErrors.Base) -> Unit,
	) {
		try {
			val intent = Intent(context, PayPalModalActivity::class.java).apply {
				putExtra("CLIENT_ID", config.data.clientID)
				putExtra("AMOUNT", config.data.amount)
				putExtra("BUYER_COUNTRY", config.data.buyerCountry)
				putExtra("OFFER_TYPE", response.meta?.offerType)
				putExtra("INSTANCE_ID", instanceId.toString())
				addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
				addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
			}
			context.startActivity(intent)

			PayPalModalActivity.registerCallbacks(
				instanceId = instanceId,
				onApply = onApply,
				onClick = onClick,
				onError = onError,
			)
		} catch (e: Exception) {
			LogCat.error(TAG, "Failed to show modal with context: ${context.javaClass.simpleName}: ${e.message}")
			onError.invoke(PayPalErrors.UnsupportedContextException("Cannot show modal: ${e.message}"))
		}
	}
}
