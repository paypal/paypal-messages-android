package com.paypal.messagesdemo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import com.paypal.messages.PayPalMessageView
import com.paypal.messages.config.PayPalEnvironment as Environment
import com.paypal.messages.config.message.style.PayPalMessageAlign
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.PayPalMessageEvents
import com.paypal.messages.config.message.PayPalMessageStyle
import com.paypal.messages.config.message.PayPalMessageViewState
import com.paypal.messages.io.Api
import com.paypal.messagesdemo.databinding.ActivityMessageBinding

class XmlActivity: AppCompatActivity() {
	private lateinit var binding: ActivityMessageBinding
	private val TAG = "XmlActivity"
	private var logoType: PayPalMessageLogoType = PayPalMessageLogoType.PRIMARY
	private var color: PayPalMessageColor = PayPalMessageColor.BLACK
	private var alignment: PayPalMessageAlign = PayPalMessageAlign.LEFT
	private var offerType: PayPalMessageOfferType = PayPalMessageOfferType.PAY_LATER_SHORT_TERM

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMessageBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val payPalMessage = binding.payPalMessage
		val progressBar = binding.progressBar

		val logoTypeRadioGroup = findViewById<RadioGroup>(R.id.logoTypeRadioGroup)
		logoTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
			when (checkedId) {
				R.id.styleInline -> {
					logoType = PayPalMessageLogoType.INLINE
				}
				R.id.stylePrimary -> {
					logoType = PayPalMessageLogoType.PRIMARY
				}
				R.id.styleAlternative -> {
					logoType = PayPalMessageLogoType.ALTERNATIVE
				}
				R.id.styleNone -> {
					logoType = PayPalMessageLogoType.NONE
				}
			}
		}

		val colorRadioGroup = findViewById<RadioGroup>(R.id.colorRadioGroup)
		colorRadioGroup.setOnCheckedChangeListener { _, checkedId ->
			when (checkedId) {
				R.id.styleBlack -> {
					color = PayPalMessageColor.BLACK
				}
				R.id.styleWhite -> {
					color = PayPalMessageColor.WHITE
				}
				R.id.styleMonochrome -> {
					color = PayPalMessageColor.MONOCHROME
				}
				R.id.styleGrayscale -> {
					color = PayPalMessageColor.GRAYSCALE
				}
			}
		}

		val alignmentRadioGroup = findViewById<RadioGroup>(R.id.alignmentRadioGroup)
		alignmentRadioGroup.setOnCheckedChangeListener { _, checkedId ->
			when (checkedId) {
				R.id.styleLeft -> {
					alignment = PayPalMessageAlign.LEFT
				}
				R.id.styleCenter -> {
					alignment = PayPalMessageAlign.CENTER
				}
				R.id.styleRight -> {
					alignment = PayPalMessageAlign.RIGHT
				}
			}
		}

		val offerTypeRadioGroup = findViewById<RadioGroup>(R.id.offerTypeRadioGroup)
		offerTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
			when (checkedId) {
				R.id.offerTypeShortTerm -> {
					offerType = PayPalMessageOfferType.PAY_LATER_SHORT_TERM
				}
				R.id.offerTypeLongTerm -> {
					offerType = PayPalMessageOfferType.PAY_LATER_LONG_TERM
				}
				R.id.offerTypePayIn1 -> {
					offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1
				}
				R.id.offerTypeCredit -> {
					offerType = PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST
				}
			}
		}

		val reloadButton = findViewById<Button>(R.id.reset)
		reloadButton.setOnClickListener {
			val editedClientId: EditText? = findViewById<EditText>(R.id.clientId)
			val amount: EditText? = findViewById<EditText>(R.id.amount)
			val buyerCountry: EditText? = findViewById<EditText>(R.id.buyercountry)

			val ignoreCache = findViewById<Switch>(R.id.ignoreCache)
			val devTouchpoint = findViewById<Switch>(R.id.devTouchpoint)
			Api.devTouchpoint = devTouchpoint.isEnabled.toString()
			Api.ignoreCache = ignoreCache.isEnabled.toString()

			if ( editedClientId?.text.toString().isNotBlank() ) {
				payPalMessage.setClientId(editedClientId?.text.toString())
			} else {
				payPalMessage.setClientId("")
			}

			if ( amount?.text.toString().isNotBlank() ) {
				payPalMessage.setAmount(amount?.text.toString().toDouble())
			} else {
				payPalMessage.setAmount(null)
			}

			if ( buyerCountry?.text.toString().isNotBlank() ) {
				payPalMessage.setBuyerCountry(buyerCountry?.text.toString())
			} else {
				payPalMessage.setBuyerCountry("US")
			}

			if ( color === PayPalMessageColor.WHITE ) {
				payPalMessage.setBackgroundColor(Color.Black.hashCode())
			} else {
				payPalMessage.setBackgroundColor(Color.White.hashCode())
			}

			// This can be better optimized since setOfferType and setStyle calls updateMessageContent()
			payPalMessage.setOfferType(offerType = offerType)
			payPalMessage.setStyle(PayPalMessageStyle(textAlign = alignment, color = color, logoType = logoType))

		}

		// TODO add example of adding MessageView here instead of in XML
		payPalMessage.setViewStates(
			PayPalMessageViewState(
				onLoading = {
					Log.d(TAG, "onLoading")
					progressBar.visibility = View.VISIBLE
					reloadButton.isEnabled = false
					Toast.makeText(this, "Loading Content...",Toast.LENGTH_SHORT).show()
				},
				onError = {
					Log.d(TAG, "onError")
					progressBar.visibility = View.INVISIBLE
					runOnUiThread {
						reloadButton.isEnabled = true
						Toast.makeText(this, it.javaClass.toString() + ":" + it.message + ":" + it.paypalDebugId,Toast.LENGTH_LONG).show()
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
				}
			)
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

		message.setData(PayPalMessageData())
		message.setClientId(EnvVars.getClientId(Environment.LIVE))
		message.setAmount(1.0)
		message.setPlacement("placement")
		message.setOfferType(PayPalMessageOfferType.PAY_LATER_SHORT_TERM)
		message.setBuyerCountry("country")

		message.setActionEventCallbacks(PayPalMessageEvents())

		message.setViewStateCallbacks(PayPalMessageViewState())

		message.setStyle(PayPalMessageStyle())
		message.setColor(PayPalMessageColor.BLACK)
		message.setLogoType(PayPalMessageLogoType.PRIMARY)
		message.setTextAlignment(PayPalMessageAlign.CENTER)
	}
}
