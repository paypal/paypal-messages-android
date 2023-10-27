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

class JetpackActivity : ComponentActivity() {
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

						Row {
							Text (text = "Offer Type")
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
							modifier = Modifier.fillMaxWidth(),
							factory = { _ ->
								messageView
							}
						)

						IgnoreCacheSwitch()
						DevTouchpointSwitch()

						FilledButtonExample(text = "Reset", onClick = { resetButton(messageView) })
						FilledButtonExample(text = "Submit", onClick = { updateMessageData(messageView) })


					}
				}
			}
		}
	}
}

@Composable
fun PayPalMessagingView(clientId: String) {
	Column(
		modifier = Modifier.padding(16.dp).fillMaxWidth()
	) {
		Box(
			modifier = Modifier.fillMaxWidth()
		) {
			if (clientId.isNotEmpty()) {
				val config = PayPalMessageConfig(
					data = PayPalMessageData(clientID = clientId)
				)

				AndroidView(
					factory = { context ->
						val messageView = PayPalMessageView(context, config = config)
						messageView.viewStateCallbacks = PayPalMessageViewStateCallbacks(
								onLoading = {
									Log.d("TAG", "onLoading")
								},
								onError = {
									Log.d("TAG", "onError")
								},
								onSuccess = {
									Log.d("TAG", "onSuccess")
								}
							)

						messageView

					}
				)
			}
		}
	}
}

@Composable
fun FilledButtonExample(onClick: () -> Unit, text: String) {
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
fun OutlinedButtonExample(onClick: () -> Unit) {
//	var elevation: ButtonElevation = ButtonElevation()
	OutlinedButton(
//		elevation = elevation,
		onClick = { onClick() },
	) {
		Text("Outlined")
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
fun BasicTextFieldDemo() {
	var text by remember { mutableStateOf(TextFieldValue()) }

	BasicTextField(
		value = text,
		onValueChange = {
			text = it
		},
		modifier = Modifier.padding(16.dp)
	)
}

//class ToggleButtonViewModel : ViewModel() {
//	var toggleState by remember { mutableStateOf(false) }
//
//	fun toggle() {
//		toggleState = !toggleState
//	}
//}

//@Composable
//fun ToggleButton(viewModel: ToggleButtonViewModel = viewModel()) {
//	val toggleState = viewModel.toggleState
//
//	Column(
//		modifier = Modifier
//			.fillMaxSize()
//			.background(MaterialTheme.colorScheme.primary)
//			.padding(16.dp),
//		verticalArrangement = Arrangement.Center
//	) {
//		androidx.compose.material3.Switch(
//			checked = toggleState,
//			onCheckedChange = {
//				viewModel.toggle()
//			},
//			colors = androidx.compose.material3.SwitchDefaults.colors(
//				checkedThumbColor = Color.White,
//				checkedTrackColor = Color.Green,
//				uncheckedTrackColor = Color.Gray
//			)
//		)
//	}
//}

fun updateMessageData(messageView: PayPalMessageView) {
	messageView.refresh()
}

fun resetButton(messageView: PayPalMessageView) {
	messageView.amount = null
	messageView.offerType = null
	messageView.alignment = PayPalMessageAlign.LEFT
	messageView.refresh()
}
