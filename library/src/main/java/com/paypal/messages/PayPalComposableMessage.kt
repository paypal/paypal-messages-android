package com.paypal.messages

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.PayPalMessageEventsCallbacks
import com.paypal.messages.config.message.PayPalMessageViewStateCallbacks
import com.paypal.messages.utils.PayPalErrors

// NOTE: This Compose implementation may have compatibility issues with certain Kotlin/Compose versions
// There were previously noted compatibility issues between Kotlin 1.8.22 and Compose compiler
// If you experience issues, please use the standard PayPalMessageView with AndroidView instead

/**
 * A Jetpack Compose component that displays PayPal Messages.
 *
 * This composable wraps the standard PayPalMessageView in a Compose-friendly way,
 * making it easy to use in a Compose UI hierarchy.
 *
 * @param clientId The PayPal client ID for the merchant
 * @param amount Optional amount for the transaction
 * @param buyerCountry Optional buyer country code
 * @param offerType Optional offer type as a string (e.g. "PAY_LATER_SHORT_TERM")
 * @param environment The PayPal environment (PRODUCTION, SANDBOX, etc.)
 * @param onLoading Callback invoked when the message is loading
 * @param onError Callback invoked when an error occurs
 * @param onSuccess Callback invoked when the message loads successfully
 * @param onClick Callback invoked when the message is clicked
 * @param onApply Callback invoked when the "Apply Now" button is clicked in the modal
 * @param modifier Modifier for the composable
 */
@Composable
fun PayPalComposableMessage(
	clientId: String,
	amount: Double? = null,
	buyerCountry: String? = null,
	offerType: String? = null,
	environment: PayPalEnvironment = PayPalEnvironment.LIVE,
	onLoading: () -> Unit = {},
	onError: (PayPalErrors.Base) -> Unit = {},
	onSuccess: () -> Unit = {},
	onClick: () -> Unit = {},
	onApply: () -> Unit = {},
	modifier: Modifier = Modifier,
	showFallbackIndicator: Boolean = true,
) {
	val context = LocalContext.current
	
	// Create a standard config
	val config = remember(clientId, environment, amount, buyerCountry, offerType) {
		val parsedOfferType = offerType?.let {
			try { PayPalMessageOfferType.valueOf(it) } catch (e: Exception) { null }
		}
		
		PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = clientId,
				environment = environment,
				amount = amount,
				buyerCountry = buyerCountry,
				offerType = parsedOfferType,
			),
			viewStateCallbacks = PayPalMessageViewStateCallbacks(
				onLoading = onLoading,
				onError = onError,
				onSuccess = onSuccess,
			),
			eventsCallbacks = PayPalMessageEventsCallbacks(
				onClick = { onClick() },
				onApply = { onApply() },
			),
		)
	}
	
	// Create the view using standard view
	val messageView = remember(context, config) {
		PayPalMessageView(context, config = config)
	}
	
	// Render the PayPal message view in the Compose UI
	Box(
		modifier = modifier
			.testTag("paypalComposableMessage"),
		contentAlignment = Alignment.Center,
	) {
		// Render the view using AndroidView
		AndroidView(
			factory = { messageView },
			modifier = Modifier.fillMaxWidth(),
		)
		
		// Show a fallback indicator if requested - helps identify when the view isn't displaying properly
		if (showFallbackIndicator) {
			Text(
				text = "PayPal Message (may not display in preview)",
				fontSize = 12.sp,
				color = Color.Gray,
				textAlign = TextAlign.Center,
				modifier = Modifier
					.fillMaxWidth()
					.height(20.dp),
			)
		}
	}
}

/**
 * Creates a PayPalMessageView with the given configuration
 *
 * @param context The Android context
 * @param config The configuration for the PayPal Message
 * @return A configured PayPalMessageView instance
 */
fun createPayPalMessageView(
	context: Context,
	config: PayPalMessageConfig,
): PayPalMessageView {
	return PayPalMessageView(context, config = config)
}
