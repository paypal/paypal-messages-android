package com.paypal.messagesdemo

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.paypal.messages.PayPalMessageView
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import com.paypal.messagesdemo.ui.BasicTheme
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.PayPalMessageViewStateCallbacks
import com.paypal.messages.config.message.style.PayPalMessageAlign
import com.paypal.messages.io.Api

fun toSentenceCase(input: String): String {
	// Convert the string to lowercase and split it into words
	val words = input.lowercase().split(" ")

	// Capitalize the first letter of each word and join them back into a string
	return words.joinToString(" ") { it -> it.replaceFirstChar { it.titlecase() } }
}

class JetpackActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			BasicTheme {
				val context = LocalContext.current

				var progessBar by remember { mutableStateOf(false) }
				var clientId: String by remember { mutableStateOf("CLIENT_ID_HERE") }

				// Style Color
				var backgroundColor by remember { mutableStateOf(Color.White) }
				val colorGroupOptions = listOf(
					toSentenceCase(PayPalMessageColor.BLACK.name),
					toSentenceCase(PayPalMessageColor.WHITE.name),
					toSentenceCase(PayPalMessageColor.MONOCHROME.name),
					toSentenceCase(PayPalMessageColor.GRAYSCALE.name),
				)
				var messageColor by remember { mutableStateOf(colorGroupOptions[0]) }

				// Style Logo
				val logoGroupOptions = listOf(
					toSentenceCase(PayPalMessageLogoType.PRIMARY.name),
					toSentenceCase(PayPalMessageLogoType.INLINE.name),
					toSentenceCase(PayPalMessageLogoType.ALTERNATIVE.name),
					toSentenceCase(PayPalMessageLogoType.NONE.name),
				)
				var messageLogo by remember { mutableStateOf(logoGroupOptions[0]) }

				// Style Aligntment
				val alignmentGroupOptions = listOf(
					toSentenceCase(PayPalMessageAlign.LEFT.name),
					toSentenceCase(PayPalMessageAlign.CENTER.name),
					toSentenceCase(PayPalMessageAlign.RIGHT.name),
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

				val messageView = PayPalMessageView(
					context,
					config = PayPalMessageConfig(
						data = PayPalMessageData(clientID = clientId, environment = PayPalEnvironment.SANDBOX),
						viewStateCallbacks = PayPalMessageViewStateCallbacks(
							onLoading = {
								progessBar = true
								buttonEnabled = false
								Toast.makeText(this, "Loading Content...", Toast.LENGTH_SHORT).show()
							},
							onError = {
								Log.d(TAG, "onError $it")
								progessBar = false
								runOnUiThread {
									buttonEnabled = true
									Toast.makeText(this, it.javaClass.toString() + ":" + it.message, Toast.LENGTH_LONG).show()
								}
							},
							onSuccess = {
								Log.d(TAG, "onSuccess")
								progessBar = false
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

					if (PayPalMessageColor.valueOf(messageColor.uppercase()) === PayPalMessageColor.WHITE) {
						backgroundColor = Color.Black
					} else {
						backgroundColor = Color.White
					}

					messageView.color = PayPalMessageColor.valueOf(messageColor.uppercase())
					messageView.logoType = PayPalMessageLogoType.valueOf(messageLogo.uppercase())
					messageView.alignment = PayPalMessageAlign.valueOf((messageAlignment.uppercase()))

					messageView.offerType = when (offerType) {
						offerGroupOptions[0] -> PayPalMessageOfferType.PAY_LATER_SHORT_TERM
						offerGroupOptions[1] -> PayPalMessageOfferType.PAY_LATER_LONG_TERM
						offerGroupOptions[2] -> PayPalMessageOfferType.PAY_LATER_PAY_IN_1
						offerGroupOptions[3] -> PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST
						else -> null
					}

					messageView.amount = amount.let {
						if (it.isBlank()) {
							null
						} else {
							it.toDouble()
						}
					}
					messageView.buyerCountry = buyerCountry.let {
						if (it.isBlank()) {
							null
						} else {
							buyerCountry
						}
					}

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

						ClientIdField(
							text = "Client ID",
							clientId = clientId,
							onChange = {
								clientId = it
							},
						)

						Text(
							text = "Style Options",
							fontSize = 14.sp,
							fontWeight = FontWeight.Bold,
							modifier = Modifier
								.width(125.dp)
								.height(intrinsicSize = IntrinsicSize.Max),
						)

						LogoOptions(
							logoGroupOptions = logoGroupOptions,
							selected = messageLogo,
							onSelected = { text: String ->
								messageLogo = text
							},
						)

						ColorOptions(
							colorGroupOptions = colorGroupOptions,
							selected = messageColor,
							onSelected = { text: String ->
								messageColor = text
							},
						)

						AlignmentOptions(
							alignmentGroupOptions = alignmentGroupOptions,
							selected = messageAlignment,
							onSelected = { text: String ->
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
						}

						OfferOptions(
							offerGroupOptions = offerGroupOptions,
							selected = offerType,
							onSelected = { text: String ->
								offerType = text
							},
						)

						FilledButton(text = "Clear", onClick = { offerType = null }, buttonEnabled = buttonEnabled)

						AmountField(
							amount = amount,
							onChange = { amount = it },
						)

						BuyerCountryField(
							country = buyerCountry,
							onChange = { buyerCountry = it },
						)

						StageTagField(stageTag, onChange = { stageTag = it })

						Row(
							horizontalArrangement = Arrangement.SpaceBetween,
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 8.dp),
						) {
							IgnoreCacheSwitch(ignoreCache = ignoreCache, onChange = { ignoreCache = it })
							DevTouchpointSwitch(devTouchpoint = devTouchpoint, onChange = { devTouchpoint = it })
						}

						CircularIndicator(progressBar = progessBar)

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

@Composable
fun ClientIdField(clientId: String, onChange: (text: String) -> Unit, text: String) {
	Row(
		modifier = Modifier.padding(vertical = 16.dp),
	) {
		Text(
			text = text,
			fontSize = 14.sp,
			fontWeight = FontWeight.Bold,
			modifier = Modifier
				.width(125.dp)
				.height(intrinsicSize = IntrinsicSize.Max)
				.align(Alignment.CenterVertically),
		)

		StyledTextField(
			value = clientId,
			onChange = onChange,
		)
	}
}

@Composable
fun LogoOptions(logoGroupOptions: List<String>, selected: String, onSelected: (text: String) -> Unit) {
	Row(
		horizontalArrangement = Arrangement.SpaceEvenly,
		modifier = Modifier.padding(top = 8.dp),
	) {
		logoGroupOptions.forEach { text ->
			Row(
				modifier = Modifier
					.height(intrinsicSize = IntrinsicSize.Max)
					.padding(end = Dp(8.0F))
					.selectable(
						selected = (text == selected),
						onClick = { onSelected(text) },
					),
			) {
				RadioButton(
					selected = (text == selected),
					onClick = { onSelected(text) },
					modifier = Modifier
						.padding(end = Dp(0F))
						.size(Dp(24f)),
				)
				Text(
					text = text,
					fontSize = 14.sp,
					modifier = Modifier
						.align(Alignment.CenterVertically),
				)
			}
		}
	}
}

@Composable
fun ColorOptions(colorGroupOptions: List<String>, selected: String, onSelected: (text: String) -> Unit) {
	Row(
		horizontalArrangement = Arrangement.SpaceEvenly,
		modifier = Modifier.padding(vertical = 4.dp),
	) {
		colorGroupOptions.forEach { text ->
			Row(
				modifier = Modifier
					.height(intrinsicSize = IntrinsicSize.Max)
					.padding(end = Dp(8.0F))
					.selectable(
						selected = (text == selected),
						onClick = { onSelected(text) },
					),
			) {
				RadioButton(
					selected = (text == selected),
					onClick = { onSelected(text) },
					modifier = Modifier
						.padding(end = Dp(0F))
						.size(Dp(24f)),
				)
				Text(
					text = text,
					fontSize = 14.sp,
					modifier = Modifier
						.align(Alignment.CenterVertically),
				)
			}
		}
	}
}

@Composable
fun AlignmentOptions(alignmentGroupOptions: List<String>, selected: String, onSelected: (text: String) -> Unit) {
	Row(
		horizontalArrangement = Arrangement.SpaceEvenly,
		modifier = Modifier.padding(vertical = 4.dp),
	) {
		alignmentGroupOptions.forEach { text ->
			Row(
				modifier = Modifier
					.height(intrinsicSize = IntrinsicSize.Max)
					.padding(end = Dp(8.0F))
					.selectable(
						selected = (text == selected),
						onClick = { onSelected(text) },
					),
			) {
				RadioButton(
					selected = (text == selected),
					onClick = { onSelected(text) },
					modifier = Modifier
						.padding(end = Dp(0F))
						.size(Dp(24f)),
				)

				Text(
					text = text,
					fontSize = 14.sp,
					modifier = Modifier
						.align(Alignment.CenterVertically),
				)
			}
		}
	}
}

@Composable
fun OfferOptions(offerGroupOptions: List<String>, selected: String?, onSelected: (text: String) -> Unit) {
	Row(
		horizontalArrangement = Arrangement.SpaceEvenly,
		modifier = Modifier.padding(vertical = 4.dp),
	) {
		offerGroupOptions.forEach { text ->
			Row(
				modifier = Modifier
					.height(intrinsicSize = IntrinsicSize.Max)
					.padding(end = Dp(8.0F))
					.selectable(
						selected = (text == selected),
						onClick = { onSelected(text) },
					),
			) {
				RadioButton(
					selected = (text == selected),
					onClick = { onSelected(text) },
					modifier = Modifier
						.padding(end = Dp(0F))
						.size(Dp(24f)),
				)

				Text(
					text = text,
					fontSize = 14.sp,
					modifier = Modifier
						.align(Alignment.CenterVertically),
				)
			}
		}
	}
}

@Composable
fun AmountField(amount: String, onChange: (text: String) -> Unit) {
	Row(
		modifier = Modifier.padding(vertical = 8.dp),
	) {
		Text(
			text = "Amount",
			fontSize = 14.sp,
			fontWeight = FontWeight.Bold,
			modifier = Modifier
				.width(125.dp)
				.height(intrinsicSize = IntrinsicSize.Max)
				.align(Alignment.CenterVertically),
		)

		StyledTextField(
			value = amount,
			onChange = onChange,
			keyboardType = KeyboardType.Number,
		)
	}
}

@Composable
fun BuyerCountryField(country: String, onChange: (text: String) -> Unit) {
	Row(
		modifier = Modifier.padding(vertical = 9.dp),
	) {
		Text(
			text = "Buyer Country",
			fontSize = 14.sp,
			fontWeight = FontWeight.Bold,
			modifier = Modifier
				.width(125.dp)
				.height(intrinsicSize = IntrinsicSize.Max)
				.align(Alignment.CenterVertically),
		)

		StyledTextField(
			value = country,
			onChange = onChange,
		)
	}
}

@Composable
fun StageTagField(stageTag: String, onChange: (text: String) -> Unit) {
	Row(
		modifier = Modifier.padding(vertical = 8.dp),
	) {
		Text(
			text = "Stage Tag",
			fontSize = 14.sp,
			fontWeight = FontWeight.Bold,
			modifier = Modifier
				.width(125.dp)
				.height(intrinsicSize = IntrinsicSize.Max)
				.align(Alignment.CenterVertically),
		)

		StyledTextField(
			value = stageTag,
			onChange = onChange,
		)
	}
}

@Composable
fun IgnoreCacheSwitch(ignoreCache: Boolean, onChange: (text: Boolean) -> Unit) {
	Row {
		Switch(
			checked = ignoreCache,
			onCheckedChange = onChange,
		)

		Text(
			text = "Ignore Cache",
			fontSize = 14.sp,
			fontWeight = FontWeight.Bold,
			modifier = Modifier
				.align(Alignment.CenterVertically)
				.padding(start = 8.dp),
		)
	}
}

@Composable
fun DevTouchpointSwitch(devTouchpoint: Boolean, onChange: (text: Boolean) -> Unit) {
	Row {
		Switch(
			checked = devTouchpoint,
			onCheckedChange = onChange,
		)

		Text(
			text = "Dev Touchpoint",
			fontSize = 14.sp,
			fontWeight = FontWeight.Bold,
			modifier = Modifier
				.align(Alignment.CenterVertically)
				.padding(start = 8.dp),
		)
	}
}

@Composable
fun FilledButton(onClick: () -> Unit, text: String, buttonEnabled: Boolean) {
	var isButtonClicked by remember { mutableStateOf(false) }
	Button(
		modifier = Modifier.width(intrinsicSize = IntrinsicSize.Min),
		enabled = buttonEnabled,
		onClick = {
			onClick()
			isButtonClicked = !isButtonClicked
		},
	) {
		Text(text)
	}
}

@Composable
fun StyledTextField(
	value: String,
	onChange: (text: String) -> Unit,
	keyboardType: KeyboardType? = KeyboardType.Text,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(36.dp)
			.background(
				Color.LightGray,
				RectangleShape,
			),
	) {
		BasicTextField(
			value = value,
			onValueChange = onChange,
			singleLine = true,
			textStyle = MaterialTheme.typography.bodyMedium,
			keyboardOptions = KeyboardOptions.Default.copy(
				keyboardType = keyboardType ?: KeyboardType.Text,
				imeAction = ImeAction.Done,
			),
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight()
				.padding(start = 4.dp, end = 4.dp, top = 8.dp),
		)
	}
}

@Composable
fun CircularIndicator(progressBar: Boolean) {
	if (!progressBar) return

	CircularProgressIndicator(
		modifier = Modifier
			.width(32.dp),
		color = MaterialTheme.colorScheme.secondary,
		trackColor = MaterialTheme.colorScheme.surfaceVariant,
	)
}
