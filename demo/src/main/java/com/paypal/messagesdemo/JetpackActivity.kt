package com.paypal.messagesdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.paypal.messages.PayPalMessageView
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import com.paypal.messagesdemo.ui.BasicTheme
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.message.*
import com.paypal.messages.config.message.style.PayPalMessageAlign
import com.paypal.messages.io.Api
import androidx.compose.ui.unit.dp

fun toSentenceCase(input: String): String {
	// Convert the string to lowercase and split it into words
	val words = input.toLowerCase().split(" ")

	// Capitalize the first letter of each word and join them back into a string
	val sentenceCase = words.joinToString(" ") { it.capitalize() }

	return sentenceCase
}

class JetpackActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			BasicTheme {
				val context = LocalContext.current

				// Client ID

				// Style Color
				var backgroundColor by remember { mutableStateOf(Color.White) }
				val colorGroupOptions = listOf(
					toSentenceCase(PayPalMessageColor.BLACK.name),
					toSentenceCase(PayPalMessageColor.WHITE.name),
					toSentenceCase(PayPalMessageColor.MONOCHROME.name),
					toSentenceCase(PayPalMessageColor.GRAYSCALE.name)
				)
				var messageColor by remember { mutableStateOf(colorGroupOptions[0])}

				// Style Logo
				val logoGroupOptions = listOf(
					toSentenceCase(PayPalMessageLogoType.PRIMARY.name),
					toSentenceCase(PayPalMessageLogoType.INLINE.name),
					toSentenceCase(PayPalMessageLogoType.ALTERNATIVE.name),
					toSentenceCase(PayPalMessageLogoType.NONE.name)
				)
				var messageLogo by remember { mutableStateOf(logoGroupOptions[0])}

				// Style Aligntment
				val alignmentGroupOptions = listOf(
					toSentenceCase(PayPalMessageAlign.LEFT.name),
					toSentenceCase(PayPalMessageAlign.CENTER.name),
					toSentenceCase(PayPalMessageAlign.RIGHT.name)
				)
				var messageAlignment by remember { mutableStateOf(alignmentGroupOptions[0])}

				// Offer Type

				var amount by remember { mutableStateOf("") }
				var buyerCountry by remember { mutableStateOf("") }
				var stageTag by remember { mutableStateOf("") }
				// ignore cache
				// dev touchpoint

				val messageView = PayPalMessageView(context,
					config = PayPalMessageConfig(
						data = PayPalMessageData(clientID = "B_Aksh3K8UpGwAs_Ngi0ahNHJja2bsxqtuqmhRAm944xzM-p6Qkq9JbqFc2yTz2Fg5WYtcW1QEu_tHDVuI", environment = PayPalEnvironment.SANDBOX),
						viewStateCallbacks = PayPalMessageViewStateCallbacks(
							onLoading = {
//								progressBar.visibility = View.VISIBLE
//								resetButton.isEnabled = false
//								submitButton.isEnabled = false
								Toast.makeText(this, "Loading Content...", Toast.LENGTH_SHORT).show()
							},
							onError = {
//								Log.d(TAG, "onError")
//								progressBar.visibility = View.INVISIBLE
								runOnUiThread {
//									resetButton.isEnabled = true
//									submitButton.isEnabled = true
									Toast.makeText(this, it.javaClass.toString() + ":" + it.message, Toast.LENGTH_LONG).show()
								}
//								it.message?.let { it1 -> Log.d("XmlActivity Error", it1) }
//								it.paypalDebugId?.let { it1 -> Log.d("XmlActivity Error", it1) }
							},
							onSuccess = {
//								Log.d(TAG, "onSuccess")
//								progressBar.visibility = View.INVISIBLE
								runOnUiThread {
//									resetButton.isEnabled = true
//									submitButton.isEnabled = true
									Toast.makeText(this, "Success Getting Content", Toast.LENGTH_SHORT).show()
								}
							},
						)
					)
				)

				fun updateMessageData() {

					if ( PayPalMessageColor.valueOf(messageColor.uppercase()) === PayPalMessageColor.WHITE ) {
						backgroundColor = Color.Black
					} else {
						backgroundColor = Color.White
					}

					messageView.color = PayPalMessageColor.valueOf(messageColor.uppercase())
					messageView.logoType = PayPalMessageLogoType.valueOf(messageLogo.uppercase())
					messageView.alignment = PayPalMessageAlign.valueOf((messageAlignment.uppercase()))
					// offer type

					messageView.amount = amount.toDouble() ?: null
					messageView.buyerCountry = buyerCountry
					Api.stageTag = stageTag

					// ignore cache

					// dev touchpoint

					messageView.refresh()
				}

				fun resetButton() {
					messageColor = colorGroupOptions[0]
					messageLogo = logoGroupOptions[0]
					messageAlignment = alignmentGroupOptions[0]
					// offer type

					// amount
					amount = ""

					// country

					// stage tag

					// ignore cache

					// dev touchpoint

					updateMessageData()
				}

				// A surface container using the 'background' color from the theme
				Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
					Column (
						modifier = Modifier.verticalScroll(state = rememberScrollState())
					){

						Row {
							Text (text = "Message Configuration")
						}

						// Client ID
						// TODO Change back to Row, Make Input look better
						Column (
							modifier = Modifier.height(intrinsicSize = IntrinsicSize.Max)
						) {
							Text( text = "Client Id", fontSize = TextUnit(value = 32.0F, type = TextUnitType(type = 1)))
							var text by remember { mutableStateOf(TextFieldValue( messageView.clientId)) }

//							BasicTextField(
//								value = text,
//								onValueChange = {
//									text = it
//								},
//								textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
//								modifier = Modifier
//									.fillMaxWidth() // To make the TextField occupy the full width
//									.padding(4.dp) // Customize the padding here
////									.background(Color(239f, 239f, 239f))
//							)
							TextField(
								value = text,
								onValueChange = {
									text = it
									messageView.clientId = it.text.toString()
								},
								placeholder = { Text(text = "Client Id") },
								modifier = Modifier
									.height(Dp(value = 64F))
									.padding(all = 1.dp)
							)

						}

						Text("Style Options")

						LogoOptions(
							logoGroupOptions = logoGroupOptions,
							selected = messageLogo,
							onSelected = { text: String ->
								messageLogo = text.toString()
							}
						)

						ColorOptions(
							colorGroupOptions = colorGroupOptions,
							selected = messageColor,
							onSelected = { text: String ->
								messageColor = text.toString()
							}
						)

						AlignmentOptions(
							alignmentGroupOptions = alignmentGroupOptions,
							selected = messageAlignment,
							onSelected = { text: String ->
								messageAlignment = text.toString()
							}
						)

						Text ("Offer Type")

						OfferButtons(messageView)

						AmountField(
							amount = amount,
							onChange = { amount = it }
						)

						BuyerCountryField(
							country = buyerCountry,
							onChange = { buyerCountry = it }
						)

						StageTagField(stageTag, onChange = { stageTag = it })

						AndroidView(
							modifier = Modifier
								.background(color = backgroundColor)
								.fillMaxWidth(),
							factory = { _ ->
								messageView
							}
						)

						IgnoreCacheSwitch()
						DevTouchpointSwitch()

						FilledButton(text = "Reset", onClick = { resetButton() })
						FilledButton(text = "Submit", onClick = { updateMessageData() })

					}
				}
			}
		}
	}
}

@Composable
fun LogoOptions( logoGroupOptions: List<String>, selected: String, onSelected: (text: String) -> Unit ) {
	val radioGroupOptions = logoGroupOptions

	Row (
		horizontalArrangement = Arrangement.SpaceEvenly
	) {
		radioGroupOptions.forEach { text ->

			Row(
				modifier = Modifier
					.height(intrinsicSize = IntrinsicSize.Max)
					.padding(end = Dp(8.0F))
					.selectable(
						selected = (text == selected),
						onClick = { onSelected(text) }
					),
			) {
				RadioButton (
					selected = (text == selected),
					onClick = { onSelected(text) },
					modifier = Modifier
						.padding(end = Dp(0F))
//						.background(color = Color.Black)
						.size(Dp(24f))
				)
				Text(
					text = text,
					fontSize = 16.sp,
//					color = Color.White,
					modifier = Modifier
//						.background(color = Color.Black)
						.align(Alignment.CenterVertically),

				)
			}


		}
	}
}

@Composable
fun ColorOptions(colorGroupOptions: List<String>, selected: String, onSelected: (text: String) -> Unit) {

	Row (
		horizontalArrangement = Arrangement.SpaceEvenly
	) {
		colorGroupOptions.forEach { text ->

			Row(
				modifier = Modifier
					.height(intrinsicSize = IntrinsicSize.Max)
					.padding(end = Dp(8.0F))
					.selectable(
						selected = (text == selected),
						onClick = { onSelected(text) }
					),
			) {
				RadioButton (
					selected = (text == selected),
					onClick = { onSelected(text) },
					modifier = Modifier
						.padding(end = Dp(0F))
						.size(Dp(24f))
				)
				Text(
					text = text,
					fontSize = 16.sp,
					modifier = Modifier
						.align(Alignment.CenterVertically)
				)
			}

		}
	}
}

@Composable
fun AlignmentOptions(alignmentGroupOptions: List<String>, selected: String, onSelected: (text: String) -> Unit) {

	Row (
		horizontalArrangement = Arrangement.SpaceEvenly
	) {
		alignmentGroupOptions.forEach { text ->

			Row(
				modifier = Modifier
					.height(intrinsicSize = IntrinsicSize.Max)
					.padding(end = Dp(8.0F))
					.selectable(
						selected = (text == selected),
						onClick = { onSelected(text) }
					),
			) {
				RadioButton (
					selected = (text == selected),
					onClick = { onSelected(text) },
					modifier = Modifier
						.padding(end = Dp(0F))
//						.background(color = Color.Black)
						.size(Dp(24f))
				)
				Text(
					text = text,
					fontSize = 16.sp,
//					color = Color.White,
					modifier = Modifier
//						.background(color = Color.Black)
						.align(Alignment.CenterVertically),

					)
			}

		}
	}
}

@Composable
fun AmountField(amount: String, onChange: (text: String) -> Unit){

	Row (
		modifier = Modifier.height(intrinsicSize = IntrinsicSize.Max)
	) {
		Text(text = "Amount", fontSize = TextUnit(value = 32.0F, type = TextUnitType(type = 1)))

		TextField(
			value = amount,
			onValueChange = onChange,
			placeholder = { Text(text = "0.00") },
			keyboardOptions = KeyboardOptions.Default.copy(
				keyboardType = KeyboardType.Number,
				imeAction = ImeAction.Done
			),
			modifier = Modifier
				.height(Dp(value = 64F))
				.padding(all = 1.dp)
		)

	}
}

@Composable
fun BuyerCountryField(country: String, onChange: (text: String) -> Unit){
	Row (
		modifier = Modifier.height(intrinsicSize = IntrinsicSize.Max)
	) {
		Text(text = "Buyer Country", fontSize = TextUnit(value = 32.0F, type = TextUnitType(type = 1)))

		TextField(
			value = country,
			onValueChange = onChange,
			placeholder = { Text(text = "US") },
			modifier = Modifier
				.height(Dp(value = 64F))
				.padding(all = 1.dp)
		)

	}
}

@Composable
fun StageTagField( stageTag: String, onChange: (text: String) -> Unit){
	Row (
		modifier = Modifier.height(intrinsicSize = IntrinsicSize.Max)
	) {
		Text(text = "Stage Tag", fontSize = TextUnit(value = 32.0F, type = TextUnitType(type = 1)))

		TextField(
			value = stageTag,
			onValueChange = onChange,
			modifier = Modifier
				.height(Dp(value = 64F))
				.padding(all = 1.dp)
		)

	}
}

@Composable
fun IgnoreCacheSwitch() {
	var checked by remember { mutableStateOf(false) }

	Switch(
		checked = checked,
		onCheckedChange = {
			checked = it
			Api.ignoreCache = it
		}
	)

	Text (text = "Ignore Cache")
}

@Composable
fun DevTouchpointSwitch() {
	var checked by remember { mutableStateOf(false) }

	Switch(
		checked = checked,
		onCheckedChange = {
			checked = it
			Api.devTouchpoint = it
		}
	)

	Text (text = "Dev Touchpoint")
}

@Composable
fun OfferButtons(messageView: PayPalMessageView) {
	var button1Enabled by remember { mutableStateOf(false) }
	var button2Enabled by remember { mutableStateOf(false) }
	var button3Enabled by remember { mutableStateOf(false) }
	var button4Enabled by remember { mutableStateOf(false) }

	Row() {
		ToggleButton(
			toggledState = button1Enabled,
			text = "Short Term",
			onClick = {
				messageView.offerType = PayPalMessageOfferType.PAY_LATER_SHORT_TERM
				button1Enabled = !button1Enabled
				button2Enabled = false
				button3Enabled = false
				button4Enabled = false
			}
		)
		ToggleButton(
			toggledState = button2Enabled,
			text = "Long Term",

			onClick = {
				messageView.offerType = PayPalMessageOfferType.PAY_LATER_LONG_TERM
				button1Enabled = false
				button2Enabled = !button2Enabled
				button3Enabled = false
				button4Enabled = false
			}
		)
		ToggleButton(
			toggledState = button3Enabled,
			text = "Pay in 1",
			onClick = {
				messageView.offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1
				button1Enabled = false
				button2Enabled = false
				button3Enabled = !button3Enabled
				button4Enabled = false
			}
		)
		ToggleButton(
			toggledState = button4Enabled,
			text = "Credit",
			onClick = {
				messageView.offerType = PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST
				button1Enabled = false
				button2Enabled = false
				button3Enabled = false
				button4Enabled = !button4Enabled
			}
		)
	}
}

@Composable
fun ToggleButton( toggledState: Boolean, text: String, onClick: () -> Unit) {

	Button(
//			shape = RoundedCornerShape(8.dp),
		colors = when(toggledState) {
			true -> ButtonDefaults.outlinedButtonColors()
			else -> ButtonDefaults.elevatedButtonColors()
		},
		elevation = ButtonDefaults.elevatedButtonElevation(),
		contentPadding = PaddingValues(start = 6.dp, top = 0.dp, end = 12.dp, bottom = 0.dp),
		modifier = Modifier
			.height(IntrinsicSize.Max),
//				.border(
//					width = 2.dp, // Border width
//					color = Color.LightGray, // Border color
////					shape = MaterialTheme.shapes.small
//				),
		onClick = onClick
	) {
		Text(text)
	}
}

@Composable
fun FilledButton(onClick: () -> Unit, text: String) {
	var isButtonClicked by remember { mutableStateOf(false) }
	Button(
		onClick = {
			onClick()
			isButtonClicked = !isButtonClicked
		},
	) {
		Text(text)
	}
}
