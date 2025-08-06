package com.paypal.messagesdemo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import com.paypal.messages.PayPalMessageView
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.PayPalMessagePageType
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.PayPalMessageViewStateCallbacks
import com.paypal.messages.config.message.style.PayPalMessageAlignment
import com.paypal.messages.config.message.style.PayPalMessageColor
import com.paypal.messages.config.message.style.PayPalMessageLogoType
import com.paypal.messages.io.Api
import com.paypal.messagesdemo.databinding.FragmentXmlBinding
import com.paypal.messagesdemo.utils.FoolproofToastHelper

class XmlFragment : Fragment() {
	private var _binding: FragmentXmlBinding? = null
	private val binding get() = _binding!!

	private val TAG = "PPM:XmlFragment"
	private var color: PayPalMessageColor = PayPalMessageColor.BLACK
	private var logoType: PayPalMessageLogoType = PayPalMessageLogoType.PRIMARY
	private var textAlignment: PayPalMessageAlignment = PayPalMessageAlignment.LEFT
	private var offerType: PayPalMessageOfferType? = null
	private val environment = PayPalEnvironment.SANDBOX

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = FragmentXmlBinding.inflate(inflater, container, false)
		val root = binding.root

		val messageWrapper = binding.messageWrapper
		val progressBar = binding.progressBar
		val resetButton = binding.reset
		val submitButton = binding.submit
		val payPalMessage = PayPalMessageView(
			context = requireActivity(),
			config = PayPalMessageConfig(
				data = PayPalMessageData(
					clientID = getString(R.string.client_id),
					environment = environment,
					pageType = PayPalMessagePageType.CART,
				),
				viewStateCallbacks = PayPalMessageViewStateCallbacks(
					onLoading = {
						Log.d(TAG, "onLoading")
						requireActivity().runOnUiThread {
							progressBar.visibility = View.VISIBLE
							resetButton.isEnabled = false
							submitButton.isEnabled = false
							FoolproofToastHelper.showToast(requireActivity(), "Loading Content...")
						}
					},
					onError = {
						val error = "${it.javaClass}:\\n  ${it.message}\\n  ${it.debugId}"
						Log.d(TAG, "onError $error")
						requireActivity().runOnUiThread {
							progressBar.visibility = View.INVISIBLE
							resetButton.isEnabled = true
							submitButton.isEnabled = true
							FoolproofToastHelper.showToast(requireActivity(), error, Toast.LENGTH_LONG)
						}
					},
					onSuccess = {
						Log.d(TAG, "onSuccess")
						requireActivity().runOnUiThread {
							progressBar.visibility = View.INVISIBLE
							resetButton.isEnabled = true
							submitButton.isEnabled = true
							FoolproofToastHelper.showToast(requireActivity(), "Success Getting Content")
						}
					},
				),
			),
		)
		payPalMessage.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
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

			payPalMessage.clientID = clientId
			payPalMessage.amount = amount
			payPalMessage.buyerCountry = buyerCountry
			payPalMessage.offerType = offerType
			payPalMessage.environment = environment

			payPalMessage.color = color
			payPalMessage.logoType = logoType
			payPalMessage.textAlignment = textAlignment
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

		return root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
