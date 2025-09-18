package com.paypal.messagesdemo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.paypal.messages.PayPalMessageView
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.PayPalMessageEventsCallbacks
import com.paypal.messages.config.message.PayPalMessageViewStateCallbacks
import com.paypal.messages.config.message.style.PayPalMessageAlignment
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import com.paypal.messages.io.Api
import com.paypal.messagesdemo.composables.CircularIndicator
import com.paypal.messagesdemo.composables.InputField
import com.paypal.messagesdemo.ui.BasicTheme

/**
 * Converts a string to sentence case (first letter capitalized, rest lowercase)
 */

fun toSentenceCase(input: String): String {
	return input.lowercase().replaceFirstChar { it.titlecase() }
}

class JetpackActivity : ComponentActivity() {
	private val TAG = "PPM:JetpackActivity"
	private val environment = PayPalEnvironment.SANDBOX

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			BasicTheme {
				val context = LocalContext.current

				var clientId: String by remember { mutableStateOf(getString(R.string.client_id)) }

				// Style Color
				var backgroundColor by remember { mutableStateOf(Color.White) }
				val colorGroupOptions = listOf(
					PayPalMessageColor.BLACK,
					PayPalMessageColor.WHITE,
					PayPalMessageColor.MONOCHROME,
					PayPalMessageColor.GRAYSCALE,
				)
				var messageColor by remember { mutableStateOf(colorGroupOptions[0]) }

				// Style Logo
				val logoGroupOptions = listOf(
					PayPalMessageLogoType.PRIMARY,
					PayPalMessageLogoType.INLINE,
					PayPalMessageLogoType.ALTERNATIVE,
					PayPalMessageLogoType.NONE,
				)

				var messageLogo by remember { mutableStateOf(logoGroupOptions[0]) }

				// Style Alignment
				val alignmentGroupOptions = listOf(
					PayPalMessageAlignment.LEFT,
					PayPalMessageAlignment.CENTER,
					PayPalMessageAlignment.RIGHT,
				)
				var messageAlignment by remember { mutableStateOf(alignmentGroupOptions[0]) }

				val offerGroupOptions = listOf(
					"Short Term",
					"Long Term",
					"Pay In 1",
					"Credit",
				)
				var offerType: String? by remember { mutableStateOf(null) }

				var amount: String by remember { mutableStateOf("") }
				var buyerCountry: String? by remember { mutableStateOf(null) }
				var stageTag: String by remember { mutableStateOf("") }
				var ignoreCache: Boolean by remember { mutableStateOf(false) }
				var devTouchpoint: Boolean by remember { mutableStateOf(false) }
				var buttonEnabled: Boolean by remember { mutableStateOf(true) }

				// State for the PayPal message
				var progressBar by remember { mutableStateOf(false) }

				// Create and configure the PayPal message view
				// This is the standard, recommended way to use PayPal Messages
				// The library will automatically handle clicks and show modals without any additional code
				val messageView = remember {
					PayPalMessageView(
						context,
						config = PayPalMessageConfig(
							// Configure the message data
							data = PayPalMessageData(clientID = clientId, environment = environment),
							// Optional: Add callbacks to receive state updates
							viewStateCallbacks = PayPalMessageViewStateCallbacks(
								onLoading = {
									progressBar = true
									buttonEnabled = false
									// Safe Toast handling
									val activity = context as? ComponentActivity
									if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
										Toast.makeText(context, "Loading Content...", Toast.LENGTH_SHORT).show()
									}
								},
								onError = {
									Log.d(TAG, "onError $it")
									progressBar = false
									buttonEnabled = true
									// Safe Toast handling
									val activity = context as? ComponentActivity
									if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
										Toast.makeText(context, it.javaClass.toString() + ":" + it.message, Toast.LENGTH_LONG).show()
									}
								},
								onSuccess = {
									Log.d(TAG, "onSuccess")
									progressBar = false
									buttonEnabled = true
									// Safe Toast handling
									val activity = context as? ComponentActivity
									if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
										Toast.makeText(context, "Success Getting Content", Toast.LENGTH_SHORT).show()
									}
								},
							),
							// Optional: Add callbacks for click events
							// Note: You don't need to show the modal manually - the library handles it automatically!
							eventsCallbacks = PayPalMessageEventsCallbacks(
								onClick = {
									// This is called when the message is clicked
									// No need to manually show any modals - the library does this for you
									Log.d(TAG, "Message clicked callback invoked")
								},
								onApply = {
									Log.d(TAG, "Apply clicked in modal")
									// Safe Toast handling
									val activity = context as? ComponentActivity
									if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
										Toast.makeText(context, "Apply clicked in modal", Toast.LENGTH_SHORT).show()
									}
								},
							),
						),
					)
				}

				fun updateMessageData() {
					messageView.clientID = clientId

					backgroundColor = if (messageColor === PayPalMessageColor.WHITE) Color.Black else Color.White
					messageView.color = messageColor
					messageView.logoType = messageLogo
					messageView.textAlignment = messageAlignment

					messageView.offerType = when (offerType) {
						offerGroupOptions[0] -> PayPalMessageOfferType.PAY_LATER_SHORT_TERM
						offerGroupOptions[1] -> PayPalMessageOfferType.PAY_LATER_LONG_TERM
						offerGroupOptions[2] -> PayPalMessageOfferType.PAY_LATER_PAY_IN_1
						offerGroupOptions[3] -> PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST
						else -> null
					}

					messageView.amount = amount.takeIf { it.isNotBlank() }?.toDouble()

					messageView.buyerCountry = buyerCountry?.takeIf { it.isNotBlank() }

					Api.stageTag = stageTag
					Api.ignoreCache = ignoreCache
					Api.devTouchpoint = devTouchpoint
				}

				fun resetButton() {
					messageColor = colorGroupOptions[0]
					messageLogo = logoGroupOptions[0]
					messageAlignment = alignmentGroupOptions[0]

					offerType = null
					amount = ""
					buyerCountry = ""
					stageTag = ""
					ignoreCache = false
					devTouchpoint = false

					updateMessageData()
				}

				// A surface container using the 'background' color from the theme
				Surface(
					color = MaterialTheme.colorScheme.background,
					modifier = Modifier
						.fillMaxSize()
						.padding(start = 12.dp, end = 12.dp),
				) {
					Column(
						modifier = Modifier.verticalScroll(state = rememberScrollState()),
					) {
						Text(
							text = "Message Configuration",
							fontSize = 20.sp,
							fontWeight = FontWeight.Bold,
							modifier = Modifier.padding(top = 8.dp),
						)

						InputField(
							text = "Client ID",
							value = clientId,
							onChange = {
								clientId = it
							},
							padding = 16.dp,
						)

						Text(
							text = "Style Options",
							fontSize = 14.sp,
							fontWeight = FontWeight.Bold,
							modifier = Modifier
								.width(125.dp)
								.height(intrinsicSize = IntrinsicSize.Max),
						)

						RadioOptions(
							logoGroupOptions = logoGroupOptions,
							selected = messageLogo,
							onSelected = { text: PayPalMessageLogoType ->
								messageLogo = text
							},
						)

						RadioOptions(
							logoGroupOptions = colorGroupOptions,
							selected = messageColor,
							onSelected = { text: PayPalMessageColor ->
								messageColor = text
							},
						)

						RadioOptions(
							logoGroupOptions = alignmentGroupOptions,
							selected = messageAlignment,
							onSelected = { text: PayPalMessageAlignment ->
								messageAlignment = text
							},
						)

						Row(
							horizontalArrangement = Arrangement.SpaceBetween,
							modifier = Modifier.fillMaxWidth(),
						) {
							Text(
								text = "Offer Type",
								fontSize = 14.sp,
								fontWeight = FontWeight.Bold,
								modifier = Modifier
									.padding(top = 8.dp)
									.width(125.dp)
									.height(intrinsicSize = IntrinsicSize.Max),
							)
							FilledButton(text = "Clear", onClick = { offerType = null }, buttonEnabled = buttonEnabled)
						}

						OfferOptions(
							offerGroupOptions = offerGroupOptions,
							selected = offerType,
							onSelected = { text: String ->
								offerType = text
							},
						)

						InputField(
							text = "Amount",
							value = amount,
							onChange = { amount = it },
							keyboardType = KeyboardType.Number,
						)

						InputField(
							text = "Buyer Country",
							value = buyerCountry ?: "",
							onChange = { buyerCountry = it },
						)

						InputField(
							text = "Stage Tag",
							value = stageTag,
							onChange = { stageTag = it },
						)

						Row(
							horizontalArrangement = Arrangement.SpaceBetween,
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 8.dp),
						) {
							SwitchOption(
								checked = ignoreCache,
								onChange = { ignoreCache = it },
								text = " Ignore Cache",
							)
							SwitchOption(
								checked = devTouchpoint,
								onChange = { devTouchpoint = it },
								text = "Dev Touchpoint",
							)
						}

						// Show loading indicator when messages are loading
						CircularIndicator(progressBar = progressBar)

						// This is the recommended way to use PayPal Messages in Jetpack Compose
						// Simply create a PayPalMessageView and place it in your Compose UI using AndroidView
						// The library handles all click events and modal display automatically!
						AndroidView(
							modifier = Modifier
								.padding(top = 16.dp, bottom = 32.dp, start = 8.dp, end = 8.dp)
								.background(color = backgroundColor)
								.height(40.dp)
								.fillMaxWidth(),
							factory = {
								// The messageView is created and configured earlier in this file
								messageView
							},
							update = { view ->
								// Add visual feedback when touched - ripple effect
								view.foreground = android.graphics.drawable.RippleDrawable(
									android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#30000000")),
									null,
									android.graphics.drawable.ColorDrawable(android.graphics.Color.WHITE),
								)

								// Note: We don't need to set any click listeners here.
								// The PayPalMessageView already handles clicks and shows the modal automatically.
							},
						)

						Row(
							horizontalArrangement = Arrangement.SpaceBetween,
							modifier = Modifier.fillMaxWidth(),
						) {
							FilledButton(text = "Reset", onClick = { resetButton() }, buttonEnabled = buttonEnabled)
							FilledButton(text = "Submit", onClick = { updateMessageData() }, buttonEnabled = buttonEnabled)
						}
					}
				}
			}
		}
	}
}
