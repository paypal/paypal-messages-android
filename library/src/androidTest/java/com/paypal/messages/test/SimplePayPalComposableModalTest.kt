package com.paypal.messages.test

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paypal.messages.PayPalComposableModal
import com.paypal.messages.config.modal.ModalCloseButton
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Simple UI test for the PayPalComposableModal composable
 */
@RunWith(AndroidJUnit4::class)
class SimplePayPalComposableModalTest {

	@get:Rule
	val composeRule = createComposeRule()

	/**
	 * Test that the composable displays correctly
	 */
	@Test
	fun customModalContent_displays() {
		// Set up the composable
		composeRule.setContent {
			PayPalComposableModal(
				clientId = "test-client-id",
				amount = 100.0,
				buyerCountry = "US",
				offerType = "PAY_LATER_SHORT_TERM",
				modalCloseButtonType = ModalCloseButton(),
				onDismiss = {},
				onApply = {},
				onError = {},
			)
		}

		// Verify components exist
		composeRule.onNodeWithTag("closeButton").assertExists()
	}
}
