package com.paypal.messages

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.modal.ModalCloseButton
import com.paypal.messages.config.modal.ModalConfig
import com.paypal.messages.config.modal.ModalEvents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests to verify that ModalFragment properly handles Android lifecycle events,
 * including the "Don't Keep Activities" developer option scenario where fragments
 * are destroyed and recreated when the app is backgrounded.
 *
 * This test addresses the crash reported in:
 * androidx.fragment.app.Fragment$InstantiationException: Unable to instantiate fragment
 * com.paypal.messages.ModalFragment: could not find Fragment constructor
 */
@RunWith(AndroidJUnit4::class)
class ModalFragmentLifecycleTest {

	@Test
	fun testFragmentRecreationWithArguments() {
		// Create fragment with proper arguments using factory method
		val fragment = ModalFragment.newInstance("test-client-id")

		// Verify arguments were set
		assertNotNull("Fragment arguments should not be null", fragment.arguments)
		assertEquals(
			"test-client-id",
			fragment.arguments?.getString("client_id"),
		)
	}

	@Test
	fun testFragmentSurvivesConfigurationChange() {
		// Launch fragment in a container
		val scenario: FragmentScenario<ModalFragment> = launchFragmentInContainer(
			fragmentArgs = Bundle().apply {
				putString("client_id", "test-lifecycle-client")
			},
		)

		// Simulate configuration change (like screen rotation or backgrounding)
		scenario.recreate()

		// Verify fragment was recreated successfully
		scenario.onFragment { fragment ->
			assertNotNull("Fragment should exist after recreation", fragment)
			// The fragment should have been recreated using the no-arg constructor
			// and arguments should have been restored from the Bundle
		}
	}

	@Test
	fun testFragmentInitializationWithConfig() {
		// Create fragment using factory method
		val fragment = ModalFragment.newInstance("test-config-client")

		// Initialize with modal configuration
		val modalConfig = ModalConfig(
			amount = 299.99,
			buyerCountry = "US",
			offer = PayPalMessageOfferType.PAY_LATER_LONG_TERM,
			ignoreCache = false,
			devTouchpoint = false,
			stageTag = null,
			events = ModalEvents(
				onClick = {},
				onLoading = {},
				onSuccess = {},
				onError = {},
				onCalculate = {},
				onShow = {},
				onClose = {},
				onApply = {},
			),
			modalCloseButton = ModalCloseButton(
				width = 24,
				height = 24,
				availableWidth = 24,
				availableHeight = 24,
				color = "#000000",
				colorType = "dark",
				alternativeText = "Close",
			),
		)

		fragment.init(modalConfig)

		// Verify configuration was applied
		assertEquals(299.99, fragment.amount ?: 0.0, 0.01)
		assertEquals("US", fragment.buyerCountry)
		assertEquals(PayPalMessageOfferType.PAY_LATER_LONG_TERM, fragment.offerType)
	}

	@Test
	fun testFragmentDoesNotCrashWhenRecreatedBySystem() {
		// This test simulates the Android system recreating the fragment
		// after process death (like with "Don't Keep Activities" option)

		val scenario: FragmentScenario<ModalFragment> = launchFragmentInContainer(
			fragmentArgs = Bundle().apply {
				putString("client_id", "process-death-test-client")
			},
		)

		// Move through lifecycle states
		scenario.moveToState(androidx.lifecycle.Lifecycle.State.STARTED)
		scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)

		// Simulate backgrounding (what happens when user presses home)
		scenario.moveToState(androidx.lifecycle.Lifecycle.State.STARTED)
		scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)

		// Simulate recreation (what happens when user returns to app)
		scenario.recreate()

		// Verify fragment survived the recreation
		scenario.onFragment { fragment ->
			assertNotNull("Fragment should be recreated successfully", fragment)
		}
	}
}
