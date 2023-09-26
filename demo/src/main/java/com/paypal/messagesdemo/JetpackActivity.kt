package com.paypal.messagesdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.paypal.messages.Logo
import com.paypal.messages.LogoAsset
import com.paypal.messages.PayPalMessageView
import com.paypal.messages.config.ProductGroup
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import com.paypal.messagesdemo.ui.BasicTheme

class JetpackActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			var color by remember { mutableStateOf(PayPalMessageColor.BLACK) }
			var logoType by remember { mutableStateOf(PayPalMessageLogoType.PRIMARY) }
			var productGroup by remember { mutableStateOf(ProductGroup.PAY_LATER) }

			val asset = Logo(logoType, productGroup).getAsset(color) as LogoAsset.ImageAsset

			BasicTheme {
				val context = LocalContext.current
				// A surface container using the 'background' color from the theme
				Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
					Column {
						Dropdown("Color", color, PayPalMessageColor.values()) { color = it }
						Dropdown("Logo", logoType, PayPalMessageLogoType.values()) { logoType = it }
						Dropdown("Product", productGroup, ProductGroup.values()) { productGroup = it }

						Box(modifier = Modifier.height(50.dp)) {
							Image(painter = painterResource(asset.resId), contentDescription = null)
						}

						// TODO use AndroidView
						// https://developer.android.com/jetpack/compose/migrate/interoperability-apis/views-in-compose
						Box {
							PayPalMessageView(context)
						}
					}
				}
			}
		}
	}
}
