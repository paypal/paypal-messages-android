package com.paypal.messagesdemo

import android.content.Context
import android.content.Intent
import android.util.Log
import com.paypal.messages.PayPalModalActivity
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.utils.PayPalErrors
import java.util.UUID

/**
 * Helper utility to show modals in the Jetpack Compose environment.
 * This class simplifies the process of showing modals in Compose UI.
 */
object JetpackModalHelper {
	private const val TAG = "JetpackModalHelper"

	/**
	 * Shows a PayPal modal using the provided parameters.
	 *
	 * @param context The context used to start the modal activity
	 * @param clientId The PayPal client ID
	 * @param amount The payment amount (optional)
	 * @param buyerCountry The buyer's country code (optional)
	 * @param offerType The type of offer to display (optional)
	 * @param instanceId A unique identifier for this modal instance
	 * @param onClick Callback for when the modal is clicked
	 * @param onApply Callback for when the user clicks apply in the modal
	 * @param onError Callback for when an error occurs
	 */
	/**
	 * Check if the PayPalModalActivity is properly registered in the app's manifest
	 */
	private fun isModalActivityRegistered(context: Context): Boolean {
		val intent = Intent(context, PayPalModalActivity::class.java)
		// Handle deprecated method with the recommended replacement when available
		// For older Android versions, this still works but is deprecated
		return try {
			// Try to resolve the activity
			intent.resolveActivity(context.packageManager) != null
		} catch (e: Exception) {
			Log.e(TAG, "Error checking for registered activity: ${e.message}")
			false
		}
	}

	fun showModal(
		context: Context,
		clientId: String,
		amount: Double? = null,
		buyerCountry: String? = null,
		offerType: PayPalMessageOfferType? = null,
		instanceId: UUID,
		onClick: () -> Unit,
		onApply: () -> Unit,
		onError: (PayPalErrors.Base) -> Unit,
	) {
		Log.d(TAG, "Showing modal with instanceId: $instanceId")

		try {
			// First check if the activity is registered in the manifest
			if (!isModalActivityRegistered(context)) {
				throw RuntimeException("PayPalModalActivity is not registered in AndroidManifest.xml")
			}

			// Reset all modals to ensure clean state - this is critical to prevent double modals
			try {
				// This resets all tracked modals to ensure we start fresh
				PayPalModalActivity.resetAllModals()
				Log.d(TAG, "Reset all modals state to prevent duplicates")
			} catch (e: Exception) {
				Log.w(TAG, "Failed to reset modals: ${e.message}")
			}

			// Also clear any existing callbacks for this specific instance ID
			try {
				PayPalModalActivity.clearCallbacks(instanceId)
				Log.d(TAG, "Cleared existing callbacks for instanceId: $instanceId")
			} catch (e: Exception) {
				Log.w(TAG, "Failed to clear callbacks: ${e.message}")
			}

			// Launch the PayPalModalActivity directly
			val intent = Intent(context, PayPalModalActivity::class.java).apply {
				putExtra("CLIENT_ID", clientId)
				if (amount != null) {
					putExtra("AMOUNT", amount)
				}
				if (buyerCountry != null) {
					putExtra("BUYER_COUNTRY", buyerCountry)
				}
				if (offerType != null) {
					putExtra("OFFER_TYPE", offerType.toString())
				}
				putExtra("INSTANCE_ID", instanceId.toString())

				// Force a new modal instance for testing
				putExtra("FORCE_NEW", true)

				// Make sure it appears correctly
				addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
				// Clear top flag ensures the modal appears on top
				addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
			}

			// Start the activity
			context.startActivity(intent)

			// Register the callbacks - our onClick is called only ONCE in the PayPalModalActivity
			PayPalModalActivity.registerCallbacks(
				instanceId = instanceId,
				onApply = {
					// Wrap the onApply callback to ensure cleanup
					onApply.invoke()
					// Explicitly clear callbacks after operation
					PayPalModalActivity.clearCallbacks(instanceId)
				},
				onClick = {
					// Wrap the onClick callback to track state
					onClick.invoke()
				},
				onError = { error ->
					// Wrap the onError callback to ensure cleanup
					onError.invoke(error)
					// Explicitly clear callbacks after operation
					PayPalModalActivity.clearCallbacks(instanceId)
				},
			)

			Log.d(TAG, "Successfully launched modal activity")
		} catch (e: Exception) {
			Log.e(TAG, "Failed to show modal: ${e.message}")
			onError.invoke(PayPalErrors.ModalFailedToLoad(e.message ?: "Unknown error"))
		}
	}
}
