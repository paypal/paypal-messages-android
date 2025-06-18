package com.paypal.messagesdemo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import com.paypal.messages.PayPalMessageView
import com.paypal.messages.PayPalMessagesModalFragment
import com.paypal.messages.config.Channel
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.PayPalMessagePageType
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.PayPalMessageViewStateCallbacks
import com.paypal.messages.config.message.style.PayPalMessageAlignment
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import com.paypal.messages.config.modal.PayPalMessagesModalConfig
import com.paypal.messages.io.Api
import com.paypal.messagesdemo.databinding.ActivityMessageBinding

class XmlActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMessageBinding
	private val TAG = "PPM:XmlActivity"
	private var color: PayPalMessageColor = PayPalMessageColor.BLACK
	private var logoType: PayPalMessageLogoType = PayPalMessageLogoType.PRIMARY
	private var textAlignment: PayPalMessageAlignment = PayPalMessageAlignment.LEFT
	private var offerType: PayPalMessageOfferType? = null
	private val environment = PayPalEnvironment.SANDBOX

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMessageBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val messagesModalText = binding.messagesModalText
		val modalConfig = PayPalMessagesModalConfig(
			clientID = getString(R.string.client_id),
			environment = environment,
		)
		val modal = PayPalMessagesModalFragment(context = this, config = modalConfig)
		messagesModalText.setOnClickListener {
			modal.show()
		}

// 		// XML can also be used
// 		val xmlModal = binding.xmlModal
// 		messagesModalText.setOnClickListener {
// 			xmlModal.show()
// 		}

		val messageWrapper = binding.messageWrapper
		val progressBar = binding.progressBar
		val resetButton = binding.reset
		val submitButton = binding.submit
		val payPalMessage = PayPalMessageView(
			context = this,
			config = PayPalMessageConfig(
				data = PayPalMessageData(
					clientID = getString(R.string.client_id),
					environment = environment,
					pageType = PayPalMessagePageType.CART,
				),
				viewStateCallbacks = PayPalMessageViewStateCallbacks(
					onLoading = {
						Log.d(TAG, "onLoading")
						progressBar.visibility = View.VISIBLE
						resetButton.isEnabled = false
						submitButton.isEnabled = false
						Toast.makeText(this, "Loading Content...", Toast.LENGTH_SHORT).show()
					},
					onError = {
						val error = "${it.javaClass}:\n  ${it.message}\n  ${it.debugId}"
						Log.d(TAG, "onError $error")
						progressBar.visibility = View.INVISIBLE
						runOnUiThread {
							resetButton.isEnabled = true
							submitButton.isEnabled = true
							Toast.makeText(this, error, Toast.LENGTH_LONG).show()
						}
					},
					onSuccess = {
						Log.d(TAG, "onSuccess")
						progressBar.visibility = View.INVISIBLE
						runOnUiThread {
							resetButton.isEnabled = true
							submitButton.isEnabled = true
							Toast.makeText(this, "Success Getting Content", Toast.LENGTH_SHORT).show()
						}
					},
				),
			),
		)
		payPalMessage.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
		messageWrapper.addView(payPalMessage)

		val clientIdEdit: EditText = binding.clientId

		val logoTypeRadioGroup = binding.logoTypeRadioGroup
		logoTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
			logoType = when (checkedId) {
				R.id.stylePrimary -> PayPalMessageLogoType.PRIMARY
				R.id.styleInline -> PayPalMessageLogoType.INLINE
				R.id.styleAlternative -> PayPalMessageLogoType.ALTERNATIVE
				R.id.styleNone -> PayPalMessageLogoType.NONE
				else -> PayPalMessageLogoType.PRIMARY
			}
		}

		val colorRadioGroup = binding.colorRadioGroup
		colorRadioGroup.setOnCheckedChangeListener { _, checkedId ->
			color = when (checkedId) {
				R.id.styleBlack -> PayPalMessageColor.BLACK
				R.id.styleWhite -> PayPalMessageColor.WHITE
				R.id.styleMonochrome -> PayPalMessageColor.MONOCHROME
				R.id.styleGrayscale -> PayPalMessageColor.GRAYSCALE
				else -> PayPalMessageColor.BLACK
			}
		}

		val alignmentRadioGroup = binding.alignmentRadioGroup
		alignmentRadioGroup.setOnCheckedChangeListener { _, checkedId ->
			textAlignment = when (checkedId) {
				R.id.styleLeft -> PayPalMessageAlignment.LEFT
				R.id.styleCenter -> PayPalMessageAlignment.CENTER
				R.id.styleRight -> PayPalMessageAlignment.RIGHT
				else -> PayPalMessageAlignment.LEFT
			}
		}

		val clearOfferTypeButton = binding.clearOfferTypeButton
		val offerTypeRadioGroup = binding.offerTypeRadioGroup
		clearOfferTypeButton.setOnClickListener {
			offerTypeRadioGroup.clearCheck()
			offerType = null
		}
		offerTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
			offerType = when (checkedId) {
				R.id.offerShortTerm -> PayPalMessageOfferType.PAY_LATER_SHORT_TERM
				R.id.offerLongTerm -> PayPalMessageOfferType.PAY_LATER_LONG_TERM
				R.id.offerPayIn1 -> PayPalMessageOfferType.PAY_LATER_PAY_IN_1
				R.id.offerCredit -> PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST
				else -> null
			}
		}

		val amountEdit = binding.amount
		val buyerCountryEdit = binding.buyerCountry
		val stageTagEdit = binding.stageTag
		val ignoreCache = binding.ignoreCache
		val devTouchpoint = binding.devTouchpoint

		// Get the data from the selected options
		fun updateMessageData() {
			Api.devTouchpoint = devTouchpoint.isChecked
			Api.ignoreCache = ignoreCache.isChecked
			Api.stageTag = stageTagEdit.text.toString().ifBlank { null }

			val clientId = clientIdEdit.text.toString().ifBlank { "" }

			val amountString = amountEdit.text.toString()
			val amount = if (amountString.isNotBlank()) amountString.toDouble() else null

			val buyerCountry = buyerCountryEdit.text.toString().ifBlank { "" }

			val backgroundColor = if (color === PayPalMessageColor.WHITE) Color.Black else Color.White
			payPalMessage.setBackgroundColor(backgroundColor.hashCode())

			// TODO: verify/fix offer type not working as expected
			payPalMessage.clientID = clientId
			payPalMessage.amount = amount
			payPalMessage.buyerCountry = buyerCountry
			payPalMessage.offerType = offerType
			payPalMessage.environment = environment

			payPalMessage.color = color
			payPalMessage.logoType = logoType
			payPalMessage.textAlignment = textAlignment

			modal.clientID = clientId
			modal.amount = amount
			modal.buyerCountry = buyerCountry
			modal.offerType = offerType
			modal.environment = environment
		}

		// Restore default options and reset UI
		resetButton.setOnClickListener {
			logoTypeRadioGroup.check(R.id.stylePrimary)
			colorRadioGroup.check(R.id.styleBlack)
			alignmentRadioGroup.check(R.id.styleLeft)
			offerType = null
			offerTypeRadioGroup.clearCheck()
			ignoreCache.isChecked = false
			devTouchpoint.isChecked = false
			amountEdit.setText("")
			buyerCountryEdit.setText("")

			updateMessageData()
		}

		// Request message based on options
		submitButton.setOnClickListener { updateMessageData() }
	}

	@Override
	private fun activityPaused() {
		val modalConfig = PayPalMessagesModalConfig(clientID = "someClientID")
		val modal = PayPalMessagesModalFragment(context = this, config = modalConfig)
		modal.hide()
	}

	/**
	 * Prevents unused warnings inside of PayPalMessageView and PayPalMessageConfig
	 */
	@Suppress("unused")
	fun useUnusedFunctions() {
		binding = ActivityMessageBinding.inflate(layoutInflater)
		setContentView(binding.root)

		PayPalMessageConfig.setGlobalAnalytics("", "")
		val config = PayPalMessageConfig(data = PayPalMessageData(clientID = "someClientID"))
		val message = PayPalMessageView(context = this, config = config)
		message.getConfig()
		message.setConfig(config)
		message.clientID = ""
		message.merchantID = ""
		message.partnerAttributionID = ""
		message.pageType = PayPalMessagePageType.CART
		message.channel = Channel.UPSTREAM.toString()
		message.onClick = {}
		message.onApply = {}
		message.onLoading = {}
		message.onSuccess = {}
		message.onError = {}

		val modalConfig = PayPalMessagesModalConfig(clientID = "someClientID")
		val modal = PayPalMessagesModalFragment(context = this, config = modalConfig)
		modal.getConfig()
		modal.setConfig(modalConfig)
		modal.environment = PayPalEnvironment.SANDBOX
		modal.clientID = ""
		modal.merchantID = ""
		modal.partnerAttributionID = ""
		modal.channel = Channel.UPSTREAM.toString()
		modal.pageType = PayPalMessagePageType.CART
		modal.onClick = {}
		modal.onApply = {}
		modal.onLoading = {}
		modal.onSuccess = {}
		modal.onError = {}
		modal.onCalculate = {}
		modal.onShow = {}
		modal.onClose = {}

		val textView = TextView(this)
		textView.setOnClickListener {
			modal.show()
		}
	}
}
