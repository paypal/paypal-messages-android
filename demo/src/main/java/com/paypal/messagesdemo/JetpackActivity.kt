package com.paypal.messagesdemo

import android.content.ContentValues.TAG
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
import com.paypal.messages.config.message.PayPalMessageViewStateCallbacks
import com.paypal.messages.config.message.style.PayPalMessageAlign
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import com.paypal.messages.io.Api
import com.paypal.messagesdemo.composables.InputField
import com.paypal.messagesdemo.ui.BasicTheme

fun toSentenceCase(input: String): String {
	return input.lowercase().replaceFirstChar { it.titlecase() }
}

class JetpackActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			BasicTheme {
				val context = LocalContext.current

				var clientId: String by remember { mutableStateOf("") }

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
					PayPalMessageAlign.LEFT,
					PayPalMessageAlign.CENTER,
					PayPalMessageAlign.RIGHT,
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
				var buyerCountry: String by remember { mutableStateOf("") }
				var stageTag: String by remember { mutableStateOf("") }
				var ignoreCache: Boolean by remember { mutableStateOf(false) }
				var devTouchpoint: Boolean by remember { mutableStateOf(false) }
				var buttonEnabled: Boolean by remember { mutableStateOf(true) }

				var progressBar by remember { mutableStateOf(false) }

				val messageView = PayPalMessageView(
					context,
					config = PayPalMessageConfig(
						data = PayPalMessageData(clientID = clientId, environment = PayPalEnvironment.SANDBOX),
						viewStateCallbacks = PayPalMessageViewStateCallbacks(
							onLoading = {
								progressBar = true
								buttonEnabled = false
								Toast.makeText(this, "Loading Content...", Toast.LENGTH_SHORT).show()
							},
							onError = {
								Log.d(TAG, "onError $it")
								progressBar = false
								runOnUiThread {
									buttonEnabled = true
									Toast.makeText(this, it.javaClass.toString() + ":" + it.message, Toast.LENGTH_LONG).show()
								}
							},
							onSuccess = {
								Log.d(TAG, "onSuccess")
								progressBar = false
								runOnUiThread {
									buttonEnabled = true
									Toast.makeText(this, "Success Getting Content", Toast.LENGTH_SHORT).show()
								}
							},
						),
					),
				)

				fun updateMessageData() {
					messageView.clientID = clientId

					if (messageColor === PayPalMessageColor.WHITE) {
						backgroundColor = Color.Black
					} else {
						backgroundColor = Color.White
					}

					messageView.color = messageColor
					messageView.logoType = messageLogo
					messageView.alignment = messageAlignment

					messageView.offerType = when (offerType) {
						offerGroupOptions[0] -> PayPalMessageOfferType.PAY_LATER_SHORT_TERM
						offerGroupOptions[1] -> PayPalMessageOfferType.PAY_LATER_LONG_TERM
						offerGroupOptions[2] -> PayPalMessageOfferType.PAY_LATER_PAY_IN_1
						offerGroupOptions[3] -> PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST
						else -> null
					}

					messageView.amount = amount.takeIf { it.isNotBlank() }?.toDouble()

					messageView.buyerCountry = buyerCountry.takeIf { it.isNotBlank() }?.toString()

					Api.stageTag = stageTag
					Api.ignoreCache = ignoreCache
					Api.devTouchpoint = devTouchpoint

					messageView.refresh()
				}

				fun resetButton() {
					messageColor = colorGroupOptions[0]
					messageLogo = logoGroupOptions[0]
					messageAlignment = alignmentGroupOptions[0]

					offerType = null
					messageView.data.offerType = null

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
							onSelected = { text: PayPalMessageAlign ->
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
							value = buyerCountry,
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

						CircularIndicator(progressBar = progressBar)

						AndroidView(
							modifier = Modifier
								.padding(top = 16.dp, bottom = 32.dp, start = 8.dp, end = 8.dp)
								.background(color = backgroundColor)
								.fillMaxWidth(),
							factory = {
								messageView
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
