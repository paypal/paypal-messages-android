package com.paypal.messages

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.paypal.messages.utils.PayPalErrors
import io.mockk.spyk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.CountDownLatch

/**
 * UI tests for PayPalModalActivity
 */
@RunWith(AndroidJUnit4::class)
class PayPalModalActivityTest {

	@get:Rule
	val composeTestRule = createAndroidComposeRule<TestComposeActivity>()

	private lateinit var instanceId: UUID

	@Before
	fun setup() {
		instanceId = UUID.randomUUID()
		PayPalModalActivity.resetAllModals()
	}

	/**
	 * Test that modal opens correctly when launched with intent
	 */
	@Test
	fun modalActivity_launches_andDisplaysCorrectly() {
		// Create and launch the PayPalModalActivity with required intent data
		val context = InstrumentationRegistry.getInstrumentation().targetContext
		val intent = Intent(context, PayPalModalActivity::class.java).apply {
			putExtra("CLIENT_ID", "test-client-id")
			putExtra("AMOUNT", 100.0)
			putExtra("BUYER_COUNTRY", "US")
			putExtra("OFFER_TYPE", "PAY_LATER_SHORT_TERM")
			putExtra("INSTANCE_ID", instanceId.toString())
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		}

		// Register callbacks before starting activity
		val onApplyLatch = CountDownLatch(1)
		val onClickLatch = CountDownLatch(1)
		val onErrorLatch = CountDownLatch(1)

		PayPalModalActivity.registerCallbacks(
			instanceId = instanceId,
			onApply = { onApplyLatch.countDown() },
			onClick = { onClickLatch.countDown() },
			onError = { onErrorLatch.countDown() },
		)

		// Launch the activity
		context.startActivity(intent)

		// Wait for the modal to be displayed
		Thread.sleep(1000)

		// Use instrumentation to assert the activity is in foreground
		val currentActivity = InstrumentationRegistry.getInstrumentation().waitForMonitor(
			androidx.test.core.app.ActivityScenario.ActivityMonitor(
				PayPalModalActivity::class.java.name,
				null,
				false,
			),
		)

		// Verify the activity is displayed
		assert(currentActivity is PayPalModalActivity)
	}

	/**
	 * Test that the close button works correctly
	 */
	@Test
	fun modalActivity_closeButton_dismissesModal() {
		// Setup a test compose activity
		composeTestRule.onNodeWithTag("launchModalButton").performClick()

		// Wait for the modal to appear
		composeTestRule.waitForIdle()

		// Find and click the close button
		composeTestRule.onNodeWithContentDescription("Close").assertIsDisplayed()
		composeTestRule.onNodeWithContentDescription("Close").performClick()

		// Verify the modal is dismissed
		composeTestRule.waitForIdle()
		// The activity should be finishing
		assert(composeTestRule.activityRule.scenario.result.resultCode == android.app.Activity.RESULT_CANCELED)
	}

	/**
	 * Test that callbacks are triggered correctly
	 */
	@Test
	fun modalActivity_triggersCallbacks_whenActionsPerformed() {
		// Create spy callbacks
		val onApply = spyk<() -> Unit>({})
		val onClick = spyk<() -> Unit>({})
		val onError = spyk<(PayPalErrors.Base) -> Unit>({})

		// Register callbacks
		PayPalModalActivity.registerCallbacks(
			instanceId = instanceId,
			onApply = onApply,
			onClick = onClick,
			onError = onError,
		)

		// Launch the modal through the test activity
		composeTestRule.onNodeWithTag("launchModalButton").performClick()

		// Wait for the modal to appear
		composeTestRule.waitForIdle()

		// Simulate applying from the modal
		composeTestRule.onNodeWithTag("applyButton").performClick()

		// Verify callback was triggered
		verify(exactly = 1) { onApply.invoke() }
	}
}

/**
 * Helper activity for testing modal in a Compose environment
 */
class TestComposeActivity : androidx.activity.ComponentActivity() {
	override fun onCreate(savedInstanceState: android.os.Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			androidx.compose.material3.Button(
				onClick = {
					// Launch PayPalModalActivity
					val intent = Intent(this, PayPalModalActivity::class.java).apply {
						putExtra("CLIENT_ID", "test-client-id")
						putExtra("AMOUNT", 100.0)
						putExtra("BUYER_COUNTRY", "US")
						putExtra("OFFER_TYPE", "PAY_LATER_SHORT_TERM")
						putExtra("INSTANCE_ID", UUID.randomUUID().toString())
					}
					startActivity(intent)
				},
				modifier = androidx.compose.ui.Modifier.testTag("launchModalButton"),
			) {
				androidx.compose.material3.Text("Launch Modal")
			}
		}
	}
}
