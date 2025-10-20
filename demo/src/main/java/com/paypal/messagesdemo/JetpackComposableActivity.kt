package com.paypal.messagesdemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.paypal.messages.PayPalComposableMessage
import com.paypal.messages.PayPalMessageView
import com.paypal.messages.PayPalModalActivity
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.PayPalMessageEventsCallbacks
import com.paypal.messages.config.message.PayPalMessageViewStateCallbacks
import com.paypal.messagesdemo.composables.CircularIndicator
import com.paypal.messagesdemo.composables.InputField
import com.paypal.messagesdemo.ui.BasicTheme
import java.util.UUID

class JetpackComposableActivity : ComponentActivity() {
	private val TAG = "PPM:JetpackComposableActivity"
	private val environment = PayPalEnvironment.SANDBOX

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			BasicTheme {
				val context = LocalContext.current

				// State variables for modal configuration
				var clientId: String by remember { mutableStateOf(getString(R.string.client_id)) }
				var amount: String by remember { mutableStateOf("100.00") }
				var buyerCountry: String by remember { mutableStateOf("US") }
				var offerType: String? by remember { mutableStateOf(PayPalMessageOfferType.PAY_LATER_SHORT_TERM.name) }
				
				// Loading state
				var isLoading by remember { mutableStateOf(false) }

				// No longer need a state for the modal since we use an Activity

				// Surface container
				Surface(
					color = MaterialTheme.colorScheme.background,
					modifier = Modifier
						.fillMaxSize()
						.padding(16.dp),
				) {
					Column(
						modifier = Modifier.verticalScroll(state = rememberScrollState()),
					) {
						Text(
							text = "PayPal Custom Modal Demo",
							fontSize = 20.sp,
							fontWeight = FontWeight.Bold,
							modifier = Modifier.padding(vertical = 16.dp),
						)

						Text(
							text = "This demo shows how to use the PayPalComposableModal composable directly in your Compose UI.",
							modifier = Modifier.padding(bottom = 16.dp),
						)

						InputField(
							text = "Client ID",
							value = clientId,
							onChange = { clientId = it },
							padding = 8.dp,
						)

						InputField(
							text = "Amount",
							value = amount,
							onChange = { amount = it },
							keyboardType = KeyboardType.Number,
							padding = 8.dp,
						)

						InputField(
							text = "Buyer Country",
							value = buyerCountry,
							onChange = { buyerCountry = it },
							padding = 8.dp,
						)

						// Offer Type selection
						Row(
							horizontalArrangement = Arrangement.SpaceBetween,
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 8.dp),
						) {
							Text(
								text = "Offer Type:",
								modifier = Modifier.align(Alignment.CenterVertically),
							)

							Button(
								onClick = {
									offerType = PayPalMessageOfferType.PAY_LATER_SHORT_TERM.name
								},
							) {
								Text("Short Term")
							}

							Button(
								onClick = {
									offerType = PayPalMessageOfferType.PAY_LATER_LONG_TERM.name
								},
							) {
								Text("Long Term")
							}
						}

						// PayPal Message View - shows a clickable message
						Text(
							text = "PayPal Message Implementations:",
							fontWeight = FontWeight.Bold,
							modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
						)
						
						// Legacy Implementation
						Text(
							text = "1. Legacy Implementation (AndroidView):",
							fontWeight = FontWeight.Medium,
							modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
						)

						// Using the actual PayPalMessageView that can be clicked to show the modal
						var backgroundColor by remember { mutableStateOf(Color.White) }
						val messageView = remember {
							PayPalMessageView(
								context,
								config = PayPalMessageConfig(
									// Configure the message data
									data = PayPalMessageData(clientID = clientId, environment = environment),
									// Optional: Add callbacks to receive state updates
									viewStateCallbacks = PayPalMessageViewStateCallbacks(
										onLoading = {
											Log.d(TAG, "Loading message content...")
											isLoading = true
										},
										onError = {
											Log.d(TAG, "Error loading message: $it")
											isLoading = false
											// Safe Toast handling
											val activity = context as? ComponentActivity
											if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
												Toast.makeText(context, "Error: $it", Toast.LENGTH_SHORT).show()
											}
										},
										onSuccess = {
											Log.d(TAG, "Message loaded successfully")
											isLoading = false
										},
									),
									// Optional: Add callbacks for click events
									eventsCallbacks = PayPalMessageEventsCallbacks(
										onClick = {
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

						// Update message configuration when inputs change
						messageView.amount = amount.toDoubleOrNull()
						messageView.buyerCountry = buyerCountry
						messageView.offerType = offerType?.let { PayPalMessageOfferType.valueOf(it) }

						// Render the PayPal message view in the Compose UI
						AndroidView(
							modifier = Modifier
								.padding(vertical = 8.dp)
								.background(color = backgroundColor)
								.height(40.dp)
								.fillMaxWidth(),
							factory = { messageView },
							update = { view ->
								// Add visual feedback when touched
								view.foreground = android.graphics.drawable.RippleDrawable(
									android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#30000000")),
									null,
									android.graphics.drawable.ColorDrawable(android.graphics.Color.WHITE),
								)
							},
						)
						
						// Show loading indicator when messages are loading
						CircularIndicator(progressBar = isLoading)
						
						Divider(modifier = Modifier.padding(vertical = 16.dp))
						
						// Composable Implementation
						Text(
							text = "2. Composable Implementation:",
							fontWeight = FontWeight.Medium,
							modifier = Modifier.padding(bottom = 4.dp),
						)
						
						// Use our new composable for a cleaner integration
						// Note: This composable implementation might not display properly due to compatibility issues
						// The fallback text will be shown to indicate this
						PayPalComposableMessage(
							clientId = clientId,
							amount = amount.toDoubleOrNull(),
							buyerCountry = buyerCountry,
							offerType = offerType,
							environment = environment,
							onLoading = {
								Log.d(TAG, "Composable message loading...")
								isLoading = true
							},
							onError = { error ->
								Log.d(TAG, "Composable message error: $error")
								isLoading = false
								// Safe Toast handling
								val activity = context as? ComponentActivity
								if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
									Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
								}
							},
							onSuccess = {
								Log.d(TAG, "Composable message loaded successfully")
								isLoading = false
							},
							onClick = {
								Log.d(TAG, "Composable message clicked")
							},
							onApply = {
								Log.d(TAG, "Apply clicked in composable message modal")
								// Safe Toast handling
								val activity = context as? ComponentActivity
								if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
									Toast.makeText(context, "Apply clicked in modal", Toast.LENGTH_SHORT).show()
								}
							},
							modifier = Modifier
								.padding(vertical = 8.dp)
								.background(Color.White)
								.height(40.dp)
								.fillMaxWidth(),
							showFallbackIndicator = true,
						)

						// Direct modal button (alternative to clicking the message)
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 24.dp),
							contentAlignment = Alignment.Center,
						) {
							Button(
								onClick = {
									Log.d(TAG, "Showing custom modal with amount: $amount, country: $buyerCountry, offer: $offerType")
									
									// Create intent for the PayPalModalActivity
									val intent = Intent(context, PayPalModalActivity::class.java).apply {
										putExtra("CLIENT_ID", clientId)
										putExtra("INSTANCE_ID", UUID.randomUUID().toString())
										amount.toDoubleOrNull()?.let { amountValue ->
											putExtra("AMOUNT", amountValue)
										}
										putExtra("BUYER_COUNTRY", buyerCountry)
										putExtra("OFFER_TYPE", offerType)
									}
									
									// Start the activity to show the modal
									context.startActivity(intent)
									
									// Add animation override if using an Activity context
									if (context is Activity) {
										context.overridePendingTransition(android.R.anim.fade_in, 0)
									}
								},
								modifier = Modifier.fillMaxWidth(0.8f),
							) {
								Text("Show Custom Modal Directly", fontSize = 16.sp)
							}
						}
					}
				}
			}
		}
	}
}
