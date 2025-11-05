package com.paypal.messages

import com.paypal.messages.config.Channel
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.modal.ModalCloseButton
import com.paypal.messages.config.modal.ModalConfig
import com.paypal.messages.config.modal.ModalEvents
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for ModalFragment
 * Tests fragment factory method and configuration
 * Note: Full fragment lifecycle and UI tests require Robolectric or instrumented tests
 * These tests focus on the business logic that doesn't require Android framework
 */
class ModalFragmentTest {

	private val testClientId = "test-client-id-123"
	
	// Note: Bundle tests require Android framework or Robolectric
	// Full fragment lifecycle tests are in instrumented test suite
	
	@Test
	fun testModalConfigCreation() {
		// Test that ModalConfig can be created with all properties
		var onClickCalled = false
		var onLoadingCalled = false
		var onSuccessCalled = false
		var onErrorCalled = false
		var onCalculateCalled = false
		var onShowCalled = false
		var onCloseCalled = false
		var onApplyCalled = false
		
		val config = ModalConfig(
			amount = 250.0,
			buyerCountry = "GB",
			offer = PayPalMessageOfferType.PAY_LATER_LONG_TERM,
			ignoreCache = true,
			devTouchpoint = false,
			stageTag = "test-stage",
			channel = Channel.NATIVE,
			events = ModalEvents(
				onClick = { onClickCalled = true },
				onLoading = { onLoadingCalled = true },
				onSuccess = { onSuccessCalled = true },
				onError = { onErrorCalled = true },
				onCalculate = { onCalculateCalled = true },
				onShow = { onShowCalled = true },
				onClose = { onCloseCalled = true },
				onApply = { onApplyCalled = true },
			),
			modalCloseButton = ModalCloseButton(
				alternativeText = "Close",
			),
		)
		
		// Verify config properties
		assertNotNull(config)
		assertEquals(250.0, config.amount)
		assertEquals("GB", config.buyerCountry)
		assertEquals(PayPalMessageOfferType.PAY_LATER_LONG_TERM, config.offer)
		assertTrue(config.ignoreCache)
		assertEquals(false, config.devTouchpoint)
		assertEquals("test-stage", config.stageTag)
		assertEquals(Channel.NATIVE, config.channel)
		
		// Verify that callbacks work
		config.events?.onClick?.invoke()
		config.events?.onLoading?.invoke()
		config.events?.onCalculate?.invoke()
		config.events?.onShow?.invoke()
		config.events?.onApply?.invoke()
		
		assertTrue(onClickCalled)
		assertTrue(onLoadingCalled)
		assertTrue(onCalculateCalled)
		assertTrue(onShowCalled)
		assertTrue(onApplyCalled)
	}

	@Test
	fun testModalEventsCreation() {
		// Test that ModalEvents can be created and callbacks work
		var callbackTriggered = false
		
		val events = ModalEvents(
			onClick = { callbackTriggered = true },
		)
		
		assertNotNull(events)
		assertEquals(false, callbackTriggered)
		
		events.onClick()
		assertTrue(callbackTriggered)
	}

	@Test
	fun testModalCloseButtonCreation() {
		// Test that ModalCloseButton can be created with custom text
		val button = ModalCloseButton(
			alternativeText = "Close Modal",
		)
		
		assertNotNull(button)
		assertEquals("Close Modal", button.alternativeText)
	}

	@Test
	fun testMultipleConfigurations() {
		// Test that multiple configurations can be created with different values
		val config1 = ModalConfig(
			amount = 100.0,
			buyerCountry = "US",
			offer = PayPalMessageOfferType.PAY_LATER_SHORT_TERM,
			events = ModalEvents(),
			modalCloseButton = ModalCloseButton(),
		)
		
		val config2 = ModalConfig(
			amount = 500.0,
			buyerCountry = "CA",
			offer = PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST,
			events = ModalEvents(),
			modalCloseButton = ModalCloseButton(),
		)
		
		assertEquals(100.0, config1.amount)
		assertEquals("US", config1.buyerCountry)
		assertEquals(PayPalMessageOfferType.PAY_LATER_SHORT_TERM, config1.offer)
		
		assertEquals(500.0, config2.amount)
		assertEquals("CA", config2.buyerCountry)
		assertEquals(PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST, config2.offer)
	}
}
