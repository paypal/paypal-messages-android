package com.paypal.messages

import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.ProductGroup
import com.paypal.messages.config.message.PayPalMessageEventsCallbacks
import com.paypal.messages.config.message.PayPalMessageViewStateCallbacks
import com.paypal.messages.config.modal.ModalCloseButton
import com.paypal.messages.io.ApiMessageData
import com.paypal.messages.io.ApiResult
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import com.paypal.messages.config.message.PayPalMessageConfig as MessageConfig
import com.paypal.messages.config.message.PayPalMessageData as MessageData

@RunWith(AndroidJUnit4::class)
class PayPalMessageViewTest {
// 	@get:Rule
// 	val activityTestRule = ActivityTestRule(TestActivity::class.java)
//
// 	@get:Rule
// 	val activityScenarioRule = ActivityScenarioRule(TestActivity::class.java)

	private val defaultMain = "Test main"
	private val defaultDisclaimer = "Test disclaimer"
	private val response = ApiMessageData.Response(
		meta = ApiMessageData.Metadata(
			creditProductGroup = ProductGroup.PAYPAL_CREDIT,
			offerCountryCode = "",
			offerType = PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			messageType = "",
			modalCloseButton = ModalCloseButton(
				0,
				0,
				0,
				0,
				"",
				"",
			),
			variables = ApiMessageData.Variables(logoPlaceholder = ""),
			merchantCountryCode = "",
			creditProductIdentifiers = List(0) { "" },
			debugId = "",
			fdata = "",
			trackingKeys = List(0) { "" },
			originatingInstanceId = UUID.randomUUID(),
		),
		content = ApiMessageData.ContentOptions(
			default = ApiMessageData.ContentDetails(main = defaultMain, disclaimer = defaultDisclaimer),
			generic = ApiMessageData.ContentDetails(main = "", disclaimer = ""),
		),
	)

	@Test
	fun testUpdateMessageContent() {
		val context = InstrumentationRegistry.getInstrumentation().targetContext
		val payPalMessageView = PayPalMessageView(
			context = context,
			config = MessageConfig(MessageData(clientID = "test_client_id")),
		)

		payPalMessageView.onActionCompleted(ApiResult.Success(response))

		val messageTextView = payPalMessageView.findViewById<TextView>(R.id.content)
		assertTrue(messageTextView.text.toString().contains(defaultMain))
		assertTrue(messageTextView.text.toString().contains(defaultDisclaimer))
	}

	@Test
	fun testMultipleMessagesCopyConfig() {
		val context = InstrumentationRegistry.getInstrumentation().targetContext
		val config = MessageConfig(MessageData(clientID = "test_client_id", amount = 100.00))
		val payPalMessageView = PayPalMessageView(
			context = context,
			config = config,
		)

		payPalMessageView.onActionCompleted(ApiResult.Success(response))

		val emptyFunction = fun () {}

		config.data.amount = 200.00
		config.viewStateCallbacks = PayPalMessageViewStateCallbacks(onLoading = emptyFunction)
		config.eventsCallbacks = PayPalMessageEventsCallbacks(onClick = emptyFunction)

		assertTrue(payPalMessageView.getConfig().data.amount!!.equals(100.00))
		assertFalse(payPalMessageView.getConfig().viewStateCallbacks?.onLoading == emptyFunction)
		assertFalse(payPalMessageView.getConfig().eventsCallbacks?.onClick == emptyFunction)
	}
}
