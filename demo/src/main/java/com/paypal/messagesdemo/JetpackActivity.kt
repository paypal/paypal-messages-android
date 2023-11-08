package com.paypal.messagesdemo

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import com.paypal.messages.Logo
import com.paypal.messages.LogoAsset
import com.paypal.messages.PayPalMessageView
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.ProductGroup
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import com.paypal.messagesdemo.ui.BasicTheme

import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.*
import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.ButtonElevation
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.message.*
import com.paypal.messages.config.message.style.PayPalMessageAlign
import com.paypal.messages.io.Api
import kotlinx.coroutines.Delay
import java.util.logging.Handler
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


class JetpackActivity : ComponentActivity() {
	private var backgroundColor: Color = Color.White

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			BasicTheme {
				val context = LocalContext.current

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

						LogoOptions(messageView)
						ColorOptions(messageView)
						AlignmentOptions(messageView)

						Column {
							Text (text = "Offer Type")
							OfferButtons(messageView)
						}

						// Amount
						Row (
							modifier = Modifier.height(intrinsicSize = IntrinsicSize.Max)
						) {
							Text(text = "Amount", fontSize = TextUnit(value = 32.0F, type = TextUnitType(type = 1)))
							var text by remember { mutableStateOf(TextFieldValue("")) }

							TextField(
								value = text,
								onValueChange = {
									text = it
									messageView.amount = it.text.toDouble()
								},
								placeholder = { Text(text = "0.00") },
								modifier = Modifier
									.height(Dp(value = 64F))
									.padding(all = 1.dp)
							)

						}

						// Buyer Country
						Row (
							modifier = Modifier.height(intrinsicSize = IntrinsicSize.Max)
						) {
							Text(text = "Buyer Country", fontSize = TextUnit(value = 32.0F, type = TextUnitType(type = 1)))
							var text by remember { mutableStateOf(TextFieldValue("")) }

							TextField(
								value = text,
								onValueChange = {
									text = it
									messageView.buyerCountry = it.text.toString()
								},
								placeholder = { Text(text = "US") },
								modifier = Modifier
									.height(Dp(value = 64F))
									.padding(all = 1.dp)
							)

						}

						// Stage Tag
						Row (
							modifier = Modifier.height(intrinsicSize = IntrinsicSize.Max)
						) {
							Text(text = "Stage Tag", fontSize = TextUnit(value = 32.0F, type = TextUnitType(type = 1)))
							var text by remember { mutableStateOf(TextFieldValue("")) }

							TextField(
								value = text,
								onValueChange = {
									text = it
									Api.stageTag = it.text.toString()
								},
								modifier = Modifier
									.height(Dp(value = 64F))
									.padding(all = 1.dp)
							)

						}

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

						FilledButton(text = "Reset", onClick = { resetButton(messageView) })
						FilledButton(text = "Submit", onClick = { updateMessageData(messageView) })

					}
				}
			}
		}
	}

	fun updateMessageData(messageView: PayPalMessageView) {
		if ( messageView.color === PayPalMessageColor.WHITE ) {
			backgroundColor = Color.Black
		}

		messageView.refresh()
	}

	fun resetButton(messageView: PayPalMessageView) {
		messageView.amount = null
		messageView.offerType = null
		messageView.alignment = PayPalMessageAlign.LEFT
		messageView.color = PayPalMessageColor.BLACK
		messageView.refresh()
		backgroundColor = Color.White
	}
}

@Composable
fun FilledButton(onClick: () -> Unit, text: String) {
	var isButtonClicked by remember { mutableStateOf(false) }
	Button(
//		elevation = Dp(1F),
		onClick = {
			onClick()
			isButtonClicked = !isButtonClicked
		},
//		modifier = Modifier
//			.graphicsLayer(
//				scaleX = if (isButtonClicked) 1.2f else 1f,
//				scaleY = if (isButtonClicked) 1.2f else 1f
//			)
	) {
		Text(text)
	}
}

@Composable
fun LogoOptions(messageView: PayPalMessageView) {
	var selected by remember { mutableStateOf("Primary") }
	val radioGroupOptions = listOf("Primary", "Inline", "Alternative", "None")
	val onSelectedChange = { text: String ->
		selected = text
		messageView.logoType = when ( text.toString() ) {
			"Primary" -> PayPalMessageLogoType.PRIMARY
			"Inline" -> PayPalMessageLogoType.INLINE
			"Alternative" -> PayPalMessageLogoType.ALTERNATIVE
			"None" -> PayPalMessageLogoType.NONE
			else -> PayPalMessageLogoType.PRIMARY
		}
	}

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
						onClick = { onSelectedChange(text) }
					),
			) {
				RadioButton (
					selected = (text == selected),
					onClick = { onSelectedChange(text) },
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
fun ColorOptions(messageView: PayPalMessageView) {
	var selected by remember { mutableStateOf("Black") }

	val radioGroupOptions = listOf("Black", "White", "Monochorme", "Grayscale")
	val onSelectedChange = { text: String ->
		selected = text
		messageView.color = when ( text.toString() ) {
			"Black" -> PayPalMessageColor.BLACK
			"White" -> PayPalMessageColor.WHITE
			"Monochorme" -> PayPalMessageColor.MONOCHROME
			"Grayscale" -> PayPalMessageColor.GRAYSCALE
			else -> PayPalMessageColor.BLACK
		}
	}

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
						onClick = { onSelectedChange(text) }
					),
			) {
				RadioButton (
					selected = (text == selected),
					onClick = { onSelectedChange(text) },
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
fun AlignmentOptions(messageView: PayPalMessageView) {
	var selected by remember { mutableStateOf("Left") }

	val radioGroupOptions = listOf("Left", "Center", "Right")
	val onSelectedChange = { text: String ->
		selected = text

		println("alignment options")
		println(text)

		messageView.alignment = when ( text.toString() ) {
			"left" -> PayPalMessageAlign.LEFT
			"Center" -> PayPalMessageAlign.CENTER
			"Right" -> PayPalMessageAlign.RIGHT
			else -> PayPalMessageAlign.LEFT
		}
	}

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
						onClick = { onSelectedChange(text) }
					),
			) {
				RadioButton (
					selected = (text == selected),
					onClick = { onSelectedChange(text) },
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
