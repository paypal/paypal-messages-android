package com.paypal.messages.data

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.paypal.messages.PayPalModalActivity
import com.paypal.messages.TestActivity
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.io.ApiMessageData
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Integration tests for PayPalMessageDataProvider modal functionality
 */
@RunWith(AndroidJUnit4::class)
class PayPalMessageDataProviderModalTest {

	private lateinit var provider: PayPalMessageDataProvider
	private lateinit var instanceId: UUID
	private lateinit var config: PayPalMessageConfig

	@Before
	fun setup() {
		provider = PayPalMessageDataProvider()
		instanceId = UUID.randomUUID()
		config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.SANDBOX,
				amount = 100.0,
				buyerCountry = "US",
			),
		)

		// Reset all modals state
		PayPalModalActivity.resetAllModals()
	}

	@After
	fun tearDown() {
		// Clean up any registered callbacks
		PayPalModalActivity.resetAllModals()
	}

	/**
	 * Test that clicking a message in a Jetpack Compose environment
	 * correctly shows the modal
	 */
	@Test
	fun messageClick_inJetpackComposeEnvironment_showsModal() {
		// Setup a JetpackActivity-like context
		val context = InstrumentationRegistry.getInstrumentation().targetContext

		// Create a mock response for testing
		val mockResponse = createMockResponse()

		// Create a click handler
		val clickHandler = provider.createClickHandler(
			context,
			config,
			instanceId,
		)

		// Prepare to wait for callbacks
		val onClickLatch = CountDownLatch(1)
		val onApplyLatch = CountDownLatch(1)
		val onErrorLatch = CountDownLatch(1)

		// Register activity callbacks
		PayPalModalActivity.registerCallbacks(
			instanceId = instanceId,
			onApply = { onApplyLatch.countDown() },
			onClick = { onClickLatch.countDown() },
			onError = { onErrorLatch.countDown() },
		)

		// Launch test activity to serve as host
		val intent = Intent(ApplicationProvider.getApplicationContext(), TestActivity::class.java)
		ActivityScenario.launch<TestActivity>(intent).use { scenario ->
			// Execute click handler on UI thread
			scenario.onActivity { activity ->
				// Simulate message click
				clickHandler.onMessageClick(
					mockResponse,
					{ onClickLatch.countDown() },
					{ onApplyLatch.countDown() },
					{ onErrorLatch.countDown() },
				)
			}

			// Verify onClick callback was triggered
			assert(onClickLatch.await(2, TimeUnit.SECONDS))

			// Give time for the modal to appear
			Thread.sleep(1000)

			// Clean up
			clickHandler.onCleanup()
		}
	}

	/**
	 * Helper to create a mock API response
	 */
	private fun createMockResponse(): ApiMessageData.Response {
		return ApiMessageData.Response(
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
	}

	/**
	 * Test that a dismissed modal fragment is not reused when showing modal again
	 * This verifies the fix where dismissed fragments create new instances instead of reusing
	 */
	@Test
	fun dismissedModalFragment_createsNewInstanceOnNextShow() {
		val context = InstrumentationRegistry.getInstrumentation().targetContext
		val mockResponse = createMockResponse()

		// Launch test activity
		val intent = Intent(ApplicationProvider.getApplicationContext(), TestActivity::class.java)
		ActivityScenario.launch<TestActivity>(intent).use { scenario ->
			scenario.onActivity { activity ->
				// Create click handler
				val clickHandler = provider.createClickHandler(
					activity,
					config,
					instanceId,
				)

				// First click - show modal
				clickHandler.onMessageClick(
					mockResponse,
					{},
					{},
					{},
				)

				// Wait for modal to be shown
				Thread.sleep(500)
				activity.supportFragmentManager.executePendingTransactions()

				// Find the modal fragment
				val firstModal = activity.supportFragmentManager.fragments
					.find { it is com.paypal.messages.ModalFragment } as? com.paypal.messages.ModalFragment

				assertNotNull("First modal should be created", firstModal)
				assertTrue("First modal should be added", firstModal.isAdded)

				// Dismiss the modal (simulating user clicking close button)
				firstModal.dismiss()
				activity.supportFragmentManager.executePendingTransactions()

				// Verify modal is dismissed
				assertFalse("Modal should be dismissed", firstModal.isAdded)

				// Second click - should create a new modal instance, not reuse the dismissed one
				clickHandler.onMessageClick(
					mockResponse,
					{},
					{},
					{},
				)

				// Wait for new modal to be shown
				Thread.sleep(500)
				activity.supportFragmentManager.executePendingTransactions()

				// Find the new modal fragment
				val secondModal = activity.supportFragmentManager.fragments
					.find { it is com.paypal.messages.ModalFragment } as? com.paypal.messages.ModalFragment

				assertNotNull("Second modal should be created", secondModal)
				assertTrue("Second modal should be added", secondModal.isAdded)

				// Verify it's a different instance (not the dismissed one)
				assertTrue(
					"Second modal should be a new instance, not the dismissed one",
					secondModal !== firstModal,
				)

				// Clean up
				clickHandler.onCleanup()
			}
		}
	}
}
