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
import com.paypal.messages.config.message.style.PayPalMessageAlign
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
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

		val editedClientId: EditText? = findViewById<EditText>(R.id.clientId)

		val logoTypeRadioGroup = findViewById<RadioGroup>(R.id.logoTypeRadioGroup)
		logoTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
			when (checkedId) {
				R.id.stylePrimary -> {
					logoType = PayPalMessageLogoType.PRIMARY
				}
				R.id.styleInline -> {
					logoType = PayPalMessageLogoType.INLINE
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

		val amount = findViewById<EditText>(R.id.amount)
		val buyerCountry = findViewById<EditText>(R.id.buyercountry)

		val ignoreCache = findViewById<Switch>(R.id.ignoreCache)
		val devTouchpoint = findViewById<Switch>(R.id.devTouchpoint)

		// Get the data from the selected options
		fun updateMessageData() {
			Api.devTouchpoint = devTouchpoint.isChecked.toString()
			Api.ignoreCache = ignoreCache.isChecked.toString()

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
		}

		// Restore default options
		val resetButton = findViewById<Button>(R.id.reset)
		resetButton.setOnClickListener {
			// Reset UI
			logoTypeRadioGroup.check(R.id.stylePrimary)
			colorRadioGroup.check(R.id.styleBlack)
			alignmentRadioGroup.check(R.id.styleLeft)
			offerTypeRadioGroup.check(R.id.offerTypeShortTerm)
			ignoreCache.isChecked = false
			devTouchpoint.isChecked = false
			amount.setText("")
			buyerCountry.setText("")

			updateMessageData()
			payPalMessage.setConfig(
				PayPalMessageConfig(
					PayPalMessageData(offerType = offerType),
					PayPalMessageStyle(textAlign = alignment, color = color, logoType = logoType)
				)
			)
			payPalMessage.refresh()
		}

		// Request message based on options
		val submitButton = findViewById<Button>(R.id.submit)
		submitButton.setOnClickListener {
			updateMessageData()
			payPalMessage.setConfig(
				PayPalMessageConfig(
					PayPalMessageData(offerType = offerType),
					PayPalMessageStyle(textAlign = alignment, color = color, logoType = logoType)
				)
			)
			payPalMessage.refresh()
		}

		// TODO add example of adding MessageView here instead of in XML
		payPalMessage.setViewStates(
			PayPalMessageViewState(
				onLoading = {
					Log.d(TAG, "onLoading")
					progressBar.visibility = View.VISIBLE
					resetButton.isEnabled = false
					submitButton.isEnabled = false
					Toast.makeText(this, "Loading Content...",Toast.LENGTH_SHORT).show()
				},
				onError = {
					Log.d(TAG, "onError")
					progressBar.visibility = View.INVISIBLE
					runOnUiThread {
						resetButton.isEnabled = true
						submitButton.isEnabled = true
						Toast.makeText(this, it.javaClass.toString() + ":" + it.message + ":" + it.paypalDebugId,Toast.LENGTH_LONG).show()
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
				}
			)
		)
	}

}
