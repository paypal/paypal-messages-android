package com.paypal.messagesdemo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.paypal.messages.PayPalMessageView
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.PayPalMessageEventsCallbacks
import com.paypal.messages.config.message.PayPalMessageViewStateCallbacks
import com.paypal.messages.config.message.style.PayPalMessageAlign
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import com.paypal.messagesdemo.databinding.ActivityMessageBinding
import com.paypal.messages.config.PayPalEnvironment as Environment

class XmlActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMessageBinding
	private val TAG = "XmlActivity"

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMessageBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val payPalMessage = binding.payPalMessage
		val progressBar = binding.progressBar
		val reloadButton = binding.reloadButton

		// TODO add example of adding MessageView here instead of in XML

		payPalMessage.viewStateCallbacks = PayPalMessageViewStateCallbacks(
			onLoading = {
				Log.d(TAG, "onLoading")
				progressBar.visibility = View.VISIBLE
				reloadButton.isEnabled = false
				Toast.makeText(this, "Loading Content...", Toast.LENGTH_SHORT).show()
			},
			onError = {
				Log.d(TAG, "onError")
				progressBar.visibility = View.INVISIBLE
				runOnUiThread {
					reloadButton.isEnabled = true
					Toast.makeText(this, it.javaClass.toString() + ":" + it.message + ":" + it.paypalDebugId, Toast.LENGTH_LONG).show()
				}
				it.message?.let { it1 -> Log.d("XmlActivity Error", it1) }
				it.paypalDebugId?.let { it1 -> Log.d("XmlActivity Error", it1) }
			},
			onSuccess = {
				Log.d(TAG, "onSuccess")
				progressBar.visibility = View.INVISIBLE
				runOnUiThread {
					reloadButton.isEnabled = true
					Toast.makeText(this, "Success Getting Content", Toast.LENGTH_SHORT).show()
				}
			},
		)
	}

	/**
	 * Prevents unused warnings inside of [PayPalMessageView] and [PayPalMessageConfig]
	 */
	@Suppress("unused")
	fun useUnusedFunctions() {
		binding = ActivityMessageBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val message = binding.payPalMessage
		val config = PayPalMessageConfig()
		config.setGlobalAnalytics("", "")
		message.setConfig(config)

		message.data = PayPalMessageData()
		message.clientId = EnvVars.getClientId(Environment.LIVE)
		message.amount = 1.0
		message.placement = ""
		message.offerType = PayPalMessageOfferType.PAY_LATER_SHORT_TERM
		message.buyerCountry = "country"

		message.eventsCallbacks = PayPalMessageEventsCallbacks()

		message.viewStateCallbacks = PayPalMessageViewStateCallbacks()

		message.color = PayPalMessageColor.BLACK
		message.logoType = PayPalMessageLogoType.PRIMARY
		message.alignment = PayPalMessageAlign.CENTER
	}
}
