package com.paypal.messagesdemo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.paypal.messages.Logo
import com.paypal.messages.LogoAsset
import com.paypal.messages.PayPalMessageView
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.ProductGroup
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import com.paypal.messagesdemo.ui.BasicTheme

import android.view.ViewGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import com.paypal.messages.config.message.PayPalMessageViewState

class JetpackActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {

			BasicTheme {
				val context = LocalContext.current
				// A surface container using the 'background' color from the theme
				Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
					Column {

						FilledButtonExample(text = "first", onClick = { println("hello") })

						AndroidView(
							factory = { ctx ->
								val messageView = PayPalMessageView(context,
									config = PayPalMessageConfig(
										data = PayPalMessageData(clientId = "B_Aksh3K8UpGwAs_Ngi0ahNHJja2bsxqtuqmhRAm944xzM-p6Qkq9JbqFc2yTz2Fg5WYtcW1QEu_tHDVuI", environment = PayPalEnvironment.SANDBOX)
									)
								)
								messageView
							}
						)
						FilledButtonExample(text = "second", onClick = { println("hello") })

						FilledButtonExample(text = "third", onClick = { println("hello") })

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
				val config = PayPalMessageConfig()
				config.data = PayPalMessageData(clientId = clientId)

				AndroidView(
					factory = { context ->
						val messageView = PayPalMessageView(context, config = config)
						messageView.setViewStates(
							PayPalMessageViewState(
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
	Button(onClick = { onClick() }) {
		Text(text)
	}
}

