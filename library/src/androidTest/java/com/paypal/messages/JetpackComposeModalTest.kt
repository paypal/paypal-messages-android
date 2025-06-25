package com.paypal.messages

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.PayPalMessageEventsCallbacks
import com.paypal.messages.data.PayPalMessageDataProvider
import com.paypal.messages.io.ApiMessageData
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.CountDownLatch

/**
 * Tests for modal display in Jetpack Compose environment
 */
@RunWith(AndroidJUnit4::class)
class JetpackComposeModalTest {

	@get:Rule
	val composeRule = createAndroidComposeRule<ComposeTestActivity>()

	private lateinit var instanceId: UUID

	@Before
	fun setup() {
		instanceId = UUID.randomUUID()
		PayPalModalActivity.resetAllModals()
	}

	@After
	fun tearDown() {
		PayPalModalActivity.resetAllModals()
	}

	/**
	 * Test that PayPalModalActivity launches from a Jetpack Compose environment
	 */
	@Test
	fun launchModal_fromJetpackCompose_displaysCorrectly() {
		// Click the button that launches the modal
		composeRule.onNodeWithTag("launchModalButton").performClick()

		// Wait for the modal to appear
		Thread.sleep(1000)

		// Verify the PayPalModalActivity was launched
		// (We can't directly assert UI elements in a different activity)
		val activityMonitor = androidx.test.core.app.ActivityScenario.ActivityMonitor(
			PayPalModalActivity::class.java.name,
			null,
			false,
		)

		val currentActivity = InstrumentationRegistry.getInstrumentation().waitForMonitor(
			activityMonitor,
		)

		// Verify the activity is displayed
		assert(currentActivity is PayPalModalActivity)
	}

	/**
	 * Test that clicking a PayPal message triggers the modal correctly
	 */
	@Test
	fun clickPayPalMessage_showsModal() {
		// Click the test message
		composeRule.onNodeWithTag("paypalMessageTest").performClick()

		// Wait for the modal to appear
		Thread.sleep(1000)

		// Verify the PayPalModalActivity was launched
		val activityMonitor = androidx.test.core.app.ActivityScenario.ActivityMonitor(
			PayPalModalActivity::class.java.name,
			null,
			false,
		)

		val currentActivity = InstrumentationRegistry.getInstrumentation().waitForMonitor(
			activityMonitor,
		)

		// Verify the activity is displayed
		assert(currentActivity is PayPalModalActivity)
	}
}

/**
 * Test activity with Compose content for modal testing
 */
class ComposeTestActivity : androidx.activity.ComponentActivity() {
	override fun onCreate(savedInstanceState: android.os.Bundle?) {
		super.onCreate(savedInstanceState)

		// Initialize once to ensure instance ID is consistent
		val instanceId = UUID.randomUUID()

		// Set up activity content
		setContent {
			TestContent(instanceId)
		}
	}
}

/**
 * Test composable for modal testing
 */
@Composable
fun TestContent(instanceId: UUID) {
	val context = LocalContext.current
	val clickLatch = remember { CountDownLatch(1) }
	val applyLatch = remember { CountDownLatch(1) }

	// Register callbacks for the modal
	PayPalModalActivity.registerCallbacks(
		instanceId = instanceId,
		onApply = { clickLatch.countDown() },
		onClick = { applyLatch.countDown() },
		onError = { /* No-op */ },
	)

	Box(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
		contentAlignment = Alignment.Center,
	) {
		// Direct modal launch button
		Button(
			onClick = {
				val intent = Intent(context, PayPalModalActivity::class.java).apply {
					putExtra("CLIENT_ID", "test-client-id")
					putExtra("AMOUNT", 100.0)
					putExtra("BUYER_COUNTRY", "US")
					putExtra("OFFER_TYPE", PayPalMessageOfferType.PAY_LATER_SHORT_TERM.toString())
					putExtra("INSTANCE_ID", instanceId.toString())
					addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				}
				context.startActivity(intent)
			},
			modifier = Modifier.testTag("launchModalButton"),
		) {
			Text("Launch Modal Directly")
		}

		// Simulated PayPal message that will trigger modal on click
		Button(
			onClick = {
				// Create data provider
				val provider = PayPalMessageDataProvider()

				// Create click handler
				val clickHandler = provider.createClickHandler(
					context,
					PayPalMessageConfig(
						data = PayPalMessageData(
							clientID = "test-client-id",
							amount = 100.0,
							buyerCountry = "US",
						),
						eventsCallbacks = PayPalMessageEventsCallbacks(
							onClick = { clickLatch.countDown() },
							onApply = { applyLatch.countDown() },
						),
					),
					instanceId,
				)

				// Create mock response
				val mockResponse = ApiMessageData.Response(
					meta = ApiMessageData.Metadata(
						offerType = PayPalMessageOfferType.PAY_LATER_SHORT_TERM,
						messageType = "PAYMENT",
						creditProductGroup = com.paypal.messages.config.ProductGroup.PAY_LATER,
						offerCountryCode = "US",
						merchantCountryCode = "US",
						creditProductIdentifiers = listOf("PAY_LATER_SHORT_TERM"),
						modalCloseButton = com.paypal.messages.config.modal.ModalCloseButton(),
						variables = ApiMessageData.Variables(logoPlaceholder = "PP_LOGO"),
						debugId = "test-debug-id",
						fdata = "test-fdata",
						trackingKeys = listOf(),
						originatingInstanceId = instanceId,
					),
					content = ApiMessageData.ContentOptions(
						default = ApiMessageData.ContentDetails(
							main = "Pay in 4 interest-free payments",
							mainAlternative = "Pay in 4 interest-free payments with PayPal",
							disclaimer = "Learn more",
						),
						generic = null,
					),
				)

				// Simulate message click
				clickHandler.onMessageClick(
					mockResponse,
					{ clickLatch.countDown() },
					{ applyLatch.countDown() },
					{ /* No-op */ },
				)
			},
			modifier = Modifier
				.padding(top = 80.dp)
				.testTag("paypalMessageTest"),
		) {
			Text("PayPal Message (Click to show modal)")
		}
	}
}
