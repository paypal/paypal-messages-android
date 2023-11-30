package com.paypal.messagesdemo

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.message.*
import com.paypal.messages.config.message.style.PayPalMessageAlign
import com.paypal.messages.io.Api
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight

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
				var clientId: String by remember { mutableStateOf("B_Aksh3K8UpGwAs_Ngi0ahNHJja2bsxqtuqmhRAm944xzM-p6Qkq9JbqFc2yTz2Fg5WYtcW1QEu_tHDVuI") }

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
				var offerButton1 by remember { mutableStateOf(false) }
				var offerButton2 by remember { mutableStateOf(false) }
				var offerButton3 by remember { mutableStateOf(false) }
				var offerButton4 by remember { mutableStateOf(false) }
				var offerType: PayPalMessageOfferType? by remember { mutableStateOf(null) }

				var amount: String by remember { mutableStateOf("") }
				var buyerCountry: String  by remember { mutableStateOf("") }
				var stageTag: String by remember { mutableStateOf("") }
				var ignoreCache: Boolean by remember { mutableStateOf(false) }
				var devTouchpoint: Boolean by remember { mutableStateOf(false) }

				var buttonEnabled: Boolean by remember { mutableStateOf(true) }

				val messageView = PayPalMessageView(context,
					config = PayPalMessageConfig(
						data = PayPalMessageData(clientID = clientId, environment = PayPalEnvironment.SANDBOX),
						viewStateCallbacks = PayPalMessageViewStateCallbacks(
							onLoading = {
//								progressBar.visibility = View.VISIBLE
								buttonEnabled = false
								Toast.makeText(this, "Loading Content...", Toast.LENGTH_SHORT).show()
							},
							onError = {
								Log.d(TAG, "onError")
//								progressBar.visibility = View.INVISIBLE
								runOnUiThread {
									buttonEnabled = true
									Toast.makeText(this, it.javaClass.toString() + ":" + it.message, Toast.LENGTH_LONG).show()
								}
							},
							onSuccess = {
								Log.d(TAG, "onSuccess")
//								progressBar.visibility = View.INVISIBLE
								runOnUiThread {
									buttonEnabled = true
									Toast.makeText(this, "Success Getting Content", Toast.LENGTH_SHORT).show()
								}
							},
						)
					)
				)

				fun updateMessageData() {

					messageView.clientId = clientId

					if ( PayPalMessageColor.valueOf(messageColor.uppercase()) === PayPalMessageColor.WHITE ) {
						backgroundColor = Color.Black
					} else {
						backgroundColor = Color.White
					}

					messageView.color = PayPalMessageColor.valueOf(messageColor.uppercase())
					messageView.logoType = PayPalMessageLogoType.valueOf(messageLogo.uppercase())
					messageView.alignment = PayPalMessageAlign.valueOf((messageAlignment.uppercase()))
					messageView.offerType = offerType

					messageView.amount = amount.let{
						if (it.isBlank()){
							null
						} else {
							it.toDouble()

						}
					}
					messageView.buyerCountry = buyerCountry.let{
						if(it.isBlank()){
							null
						}else {
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

					offerButton1 = false
					offerButton2 = false
					offerButton3 = false
					offerButton4 = false
					offerType = null

					amount = ""
					buyerCountry = ""
					stageTag = ""
					ignoreCache = false
					devTouchpoint = false

					updateMessageData()
				}

				// A surface container using the 'background' color from the theme
				Surface(modifier = Modifier.fillMaxSize().padding(start = 12.dp, end = 12.dp), color = MaterialTheme.colorScheme.background) {
					Column (
						modifier = Modifier.verticalScroll(state = rememberScrollState())
					){

						Text(text = "Message Configuration", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))

						ClientIdField(
							text = "Client ID",
							clientId = clientId,
							onChange = {
								clientId = it
							}
						)

						Text(text = "Style Options", fontSize = 14.sp, fontWeight = FontWeight.Bold,
							modifier = Modifier
								.width(125.dp)
								.height(intrinsicSize = IntrinsicSize.Max)
						)

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

						OfferButtons(
							offerButton1 = offerButton1,
							offer1 = "Short Term",
							offerButton1Click = {
								offerType = PayPalMessageOfferType.PAY_LATER_SHORT_TERM
								offerButton1 = !offerButton1
								offerButton2 = false
								offerButton3 = false
								offerButton4 = false
							},
							offerButton2 = offerButton2,
							offer2 = "Long Term",
							offerButton2Click = {
								offerType = PayPalMessageOfferType.PAY_LATER_LONG_TERM
								offerButton1 = false
								offerButton2 = !offerButton2
								offerButton3 = false
								offerButton4 = false
							},
							offerButton3 = offerButton3,
							offer3 = "Pay in 1",
							offerButton3Click = {
								offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1
								offerButton1 = false
								offerButton2 = false
								offerButton3 = !offerButton3
								offerButton4 = false
							},
							offerButton4 = offerButton4,
							offer4 = "Credit",
							offerButton4Click = {
								offerType = PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST
								offerButton1 = false
								offerButton2 = false
								offerButton3 = false
								offerButton4 = !offerButton4
							}
						)

						AmountField(
							amount = amount,
							onChange = { amount = it }
						)

						BuyerCountryField(
							country = buyerCountry,
							onChange = { buyerCountry = it }
						)

						StageTagField(stageTag, onChange = { stageTag = it })

						Row (
							horizontalArrangement = Arrangement.SpaceBetween,
							modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
						){
							IgnoreCacheSwitch(ignoreCache = ignoreCache, onChange = { ignoreCache = it })
							DevTouchpointSwitch(devTouchpoint = devTouchpoint, onChange = { devTouchpoint = it })
						}

						AndroidView(
							modifier = Modifier
								.padding(top = 16.dp, bottom = 32.dp, start = 8.dp, end = 8.dp)
								.background(color = backgroundColor)
								.fillMaxWidth(),
							factory = { _ ->
								messageView
							}
						)

						Row (
							horizontalArrangement = Arrangement.SpaceBetween,
							modifier = Modifier.fillMaxWidth()
						){
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
	Row (
		modifier = Modifier.padding(vertical = 16.dp)
	) {
		Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Bold,
			modifier = Modifier
				.width(125.dp)
				.height(intrinsicSize = IntrinsicSize.Max)
				.align(Alignment.CenterVertically)
		)

		StyledTextField(
			value = clientId,
			onChange = onChange,
			placeholder = text
		)

	}
}

@Composable
fun LogoOptions( logoGroupOptions: List<String>, selected: String, onSelected: (text: String) -> Unit ) {
	val radioGroupOptions = logoGroupOptions

	Row (
		horizontalArrangement = Arrangement.SpaceEvenly,
		modifier = Modifier.padding(top = 8.dp)
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
fun ColorOptions(colorGroupOptions: List<String>, selected: String, onSelected: (text: String) -> Unit) {

	Row (
		horizontalArrangement = Arrangement.SpaceEvenly,
		modifier = Modifier.padding(vertical = 4.dp)
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
					fontSize = 14.sp,
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
		horizontalArrangement = Arrangement.SpaceEvenly,
		modifier = Modifier.padding(vertical = 4.dp)
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
fun OfferButtons(
	offerButton1: Boolean,
	offer1: String,
	offerButton1Click: () -> Unit,
	offerButton2: Boolean,
	offer2: String,
	offerButton2Click: () -> Unit,
	offerButton3: Boolean,
	offer3: String,
	offerButton3Click: () -> Unit,
	offerButton4: Boolean,
	offer4: String,
	offerButton4Click: () -> Unit,
) {

	Column( modifier = Modifier.padding(top = 16.dp, bottom = 8.dp) ) {
		Text(text = "Offer Type", fontSize = 14.sp, fontWeight = FontWeight.Bold,
			modifier = Modifier
				.width(125.dp)
				.height(intrinsicSize = IntrinsicSize.Max)
		)

		Row( modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween ) {

			ToggleButton(
				toggledState = offerButton1,
				text = offer1,
				onClick = offerButton1Click
			)
			ToggleButton(
				toggledState = offerButton2,
				text = offer2,
				onClick = offerButton2Click
			)
			ToggleButton(
				toggledState = offerButton3,
				text = offer3,
				onClick = offerButton3Click
			)
			ToggleButton(
				toggledState = offerButton4,
				text = offer4,
				onClick = offerButton4Click
			)
		}
	}
}

@Composable
fun AmountField(amount: String, onChange: (text: String) -> Unit){

	Row(
			modifier = Modifier.padding(vertical = 8.dp)
	){

		Text(text = "Amount", fontSize = 14.sp, fontWeight = FontWeight.Bold,
			modifier = Modifier
				.width(125.dp)
				.height(intrinsicSize = IntrinsicSize.Max)
				.align(Alignment.CenterVertically)
		)

		StyledTextField(
			value = amount,
			onChange = onChange,
			placeholder = "US",
			keyboardType = KeyboardType.Number
		)

	}

}

@Composable
fun BuyerCountryField(country: String, onChange: (text: String) -> Unit){
	Row (
		modifier = Modifier.padding(vertical = 9.dp)
	) {
		Text(text = "Buyer Country", fontSize = 14.sp, fontWeight = FontWeight.Bold,
			modifier = Modifier
				.width(125.dp)
				.height(intrinsicSize = IntrinsicSize.Max)
				.align(Alignment.CenterVertically)
		)

		StyledTextField(
			value = country,
			onChange = onChange,
			placeholder = "US"
		)

	}
}

@Composable
fun StageTagField( stageTag: String, onChange: (text: String) -> Unit){
	Row (
		modifier = Modifier.padding(vertical = 8.dp)
	) {
		Text(text = "Stage Tag", fontSize = 14.sp, fontWeight = FontWeight.Bold,
			modifier = Modifier
				.width(125.dp)
				.height(intrinsicSize = IntrinsicSize.Max)
				.align(Alignment.CenterVertically)
		)

		StyledTextField(
			value = stageTag,
			onChange = onChange,
			placeholder = "US"
		)

	}
}

@Composable
fun IgnoreCacheSwitch(ignoreCache: Boolean, onChange: (text: Boolean) -> Unit) {
	Row {
		Switch(
			checked = ignoreCache,
			onCheckedChange = onChange
		)

		Text (text = "Ignore Cache", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically).padding(start = 8.dp))
	}
}

@Composable
fun DevTouchpointSwitch(devTouchpoint: Boolean, onChange: (text: Boolean) -> Unit) {

	Row{
		Switch(
			checked = devTouchpoint,
			onCheckedChange = onChange
		)

		Text (text = "Dev Touchpoint", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically).padding(start = 8.dp))
	}

}

@Composable
fun ToggleButton( toggledState: Boolean, text: String, onClick: () -> Unit) {

	Button(
		colors = when(toggledState) {
			true -> ButtonDefaults.outlinedButtonColors()
			else -> ButtonDefaults.elevatedButtonColors()
		},
		elevation = ButtonDefaults.elevatedButtonElevation(),
		shape = RectangleShape,
		contentPadding = PaddingValues(start = 6.dp, top = 0.dp, end = 12.dp, bottom = 0.dp),
		modifier = Modifier
			.width(96.dp)
			.height(IntrinsicSize.Max),
		onClick = onClick
	) {
		Text(text)
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
fun StyledTextField(value: String, onChange: (text: String) -> Unit, placeholder: String, keyboardType: KeyboardType? = KeyboardType.Text){
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(36.dp)
			.background(
				Color.LightGray,
				RectangleShape
			)
	){
		BasicTextField(
			value = value,
			onValueChange = onChange,
			singleLine = true,
			textStyle =  MaterialTheme.typography.bodyMedium,
			keyboardOptions = KeyboardOptions.Default.copy(
				keyboardType = keyboardType ?: KeyboardType.Text,
				imeAction = ImeAction.Done
			),
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight()
				.padding(start = 4.dp, end = 4.dp, top = 8.dp)
		)
	}
}
