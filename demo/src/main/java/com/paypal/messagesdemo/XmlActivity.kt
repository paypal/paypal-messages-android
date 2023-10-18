package com.paypal.messagesdemo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import com.google.android.material.switchmaterial.SwitchMaterial
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.message.PayPalMessageStyle
import com.paypal.messages.config.message.PayPalMessageViewState
import com.paypal.messages.config.message.style.PayPalMessageAlign
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import com.paypal.messages.io.Api
import com.paypal.messagesdemo.databinding.ActivityMessageBinding

class XmlActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMessageBinding
	private val TAG = "XmlActivity"
	private var logoType: PayPalMessageLogoType = PayPalMessageLogoType.PRIMARY
	private var color: PayPalMessageColor = PayPalMessageColor.BLACK
	private var alignment: PayPalMessageAlign = PayPalMessageAlign.LEFT
	private var offerType: PayPalMessageOfferType? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMessageBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val payPalMessage = binding.payPalMessage
		val progressBar = binding.progressBar

		val editedClientId: EditText? = findViewById<EditText>(R.id.clientId)

		val logoTypeRadioGroup = findViewById<RadioGroup>(R.id.logoTypeRadioGroup)
		logoTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
			logoType = when (checkedId) {
				R.id.stylePrimary -> PayPalMessageLogoType.PRIMARY
				R.id.styleInline -> PayPalMessageLogoType.INLINE
				R.id.styleAlternative -> PayPalMessageLogoType.ALTERNATIVE
				R.id.styleNone -> PayPalMessageLogoType.NONE
				else -> PayPalMessageLogoType.PRIMARY
			}
		}

		val colorRadioGroup = findViewById<RadioGroup>(R.id.colorRadioGroup)
		colorRadioGroup.setOnCheckedChangeListener { _, checkedId ->
			color = when (checkedId) {
				R.id.styleBlack -> PayPalMessageColor.BLACK
				R.id.styleWhite -> PayPalMessageColor.WHITE
				R.id.styleMonochrome -> PayPalMessageColor.MONOCHROME
				R.id.styleGrayscale -> PayPalMessageColor.GRAYSCALE
				else -> PayPalMessageColor.BLACK
			}
		}

		val alignmentRadioGroup = findViewById<RadioGroup>(R.id.alignmentRadioGroup)
		alignmentRadioGroup.setOnCheckedChangeListener { _, checkedId ->
			alignment = when (checkedId) {
				R.id.styleLeft -> PayPalMessageAlign.LEFT
				R.id.styleCenter -> PayPalMessageAlign.CENTER
				R.id.styleRight -> PayPalMessageAlign.RIGHT
				else -> PayPalMessageAlign.LEFT
			}
		}

		val shortTerm = findViewById<ToggleButton>(R.id.shortTerm)
		val longTerm = findViewById<ToggleButton>(R.id.longTerm)
		val payIn1 = findViewById<ToggleButton>(R.id.payIn1)
		val credit = findViewById<ToggleButton>(R.id.credit)

		fun updateOfferUi (offerName: PayPalMessageOfferType?, isChecked: Boolean) {
			shortTerm.isChecked = false
			longTerm.isChecked = false
			payIn1.isChecked = false
			credit.isChecked = false
			offerType = null

			if ( offerName == PayPalMessageOfferType.PAY_LATER_SHORT_TERM && isChecked) {
				shortTerm.isChecked = true
				offerType = PayPalMessageOfferType.PAY_LATER_SHORT_TERM
			} else if ( offerName == PayPalMessageOfferType.PAY_LATER_LONG_TERM && isChecked) {
				longTerm.isChecked = true
				offerType = PayPalMessageOfferType.PAY_LATER_LONG_TERM
			} else if ( offerName == PayPalMessageOfferType.PAY_LATER_PAY_IN_1 && isChecked) {
				payIn1.isChecked = true
				offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1
			} else if ( offerName == PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST && isChecked) {
				credit.isChecked = true
				offerType = PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST
			}

			payPalMessage.setOfferType(offerType = offerType)

		}

		shortTerm.setOnCheckedChangeListener { _, isChecked ->
			updateOfferUi(PayPalMessageOfferType.PAY_LATER_SHORT_TERM, isChecked)
		}
		longTerm.setOnCheckedChangeListener { _, isChecked ->
			updateOfferUi(PayPalMessageOfferType.PAY_LATER_LONG_TERM, isChecked)
		}
		payIn1.setOnCheckedChangeListener { _, isChecked ->
			updateOfferUi(PayPalMessageOfferType.PAY_LATER_PAY_IN_1, isChecked)
		}
		credit.setOnCheckedChangeListener { _, isChecked ->
			updateOfferUi(PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST, isChecked)
		}

		val amount = findViewById<EditText>(R.id.amount)
		val buyerCountry = findViewById<EditText>(R.id.buyerCountry)
		val stageTag = findViewById<EditText>(R.id.stageTag)


		val ignoreCache = findViewById<SwitchMaterial>(R.id.ignoreCache)
		val devTouchpoint = findViewById<SwitchMaterial>(R.id.devTouchpoint)

		// Get the data from the selected options
		fun updateMessageData() {
			Api.devTouchpoint = devTouchpoint.isChecked
			Api.ignoreCache = ignoreCache.isChecked

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

			if ( stageTag?.text.toString().isNotBlank() ) {
				Api.stageTag = stageTag?.text.toString()
			} else {
				Api.stageTag = null
			}

			payPalMessage.setStyle(PayPalMessageStyle(textAlign = alignment, color = color, logoType = logoType))
			payPalMessage.refresh()
		}

		// Restore default options
		val resetButton = findViewById<Button>(R.id.reset)
		resetButton.setOnClickListener {
			// Reset UI
			logoTypeRadioGroup.check(R.id.stylePrimary)
			colorRadioGroup.check(R.id.styleBlack)
			alignmentRadioGroup.check(R.id.styleLeft)
			updateOfferUi(null, false)
			ignoreCache.isChecked = false
			devTouchpoint.isChecked = false
			amount.setText("")
			buyerCountry.setText("")

			updateMessageData()
		}

		// Request message based on options
		val submitButton = findViewById<Button>(R.id.submit)
		submitButton.setOnClickListener {
			updateMessageData()
		}

		// TODO add example of adding MessageView here instead of in XML
		payPalMessage.setViewStates(
			PayPalMessageViewState(
				onLoading = {
					Log.d(TAG, "onLoading")
					progressBar.visibility = View.VISIBLE
					resetButton.isEnabled = false
					submitButton.isEnabled = false
					Toast.makeText(this, "Loading Content...", Toast.LENGTH_SHORT).show()
				},
				onError = {
					Log.d(TAG, "onError")
					progressBar.visibility = View.INVISIBLE
					runOnUiThread {
						resetButton.isEnabled = true
						submitButton.isEnabled = true
						Toast.makeText(this, it.javaClass.toString() + ":" + it.message + ":" + it.paypalDebugId, Toast.LENGTH_LONG).show()
					}
					it.message?.let { it1 -> Log.d("XmlActivity Error", it1) }
					it.paypalDebugId?.let { it1 -> Log.d("XmlActivity Error", it1) }
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
		)
	}

}
