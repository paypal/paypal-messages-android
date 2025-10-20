package com.paypal.messages

import android.webkit.WebViewClient
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.modal.ModalCloseButton
import com.paypal.messages.config.modal.ModalConfig
import com.paypal.messages.config.modal.ModalEvents
import com.paypal.messages.utils.PayPalErrors
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PayPalComposableModalTest {

	@Test
	fun testModalConfig() {
		// Test that we can create a ModalConfig properly
		val config = ModalConfig(
			amount = 100.0,
			buyerCountry = "US",
			offer = PayPalMessageOfferType.PAY_LATER_SHORT_TERM,
			modalCloseButton = ModalCloseButton(alternativeText = "Close Modal"),
		)

		// Verify the config properties
		assertEquals(100.0, config.amount)
		assertEquals("US", config.buyerCountry)
		assertEquals(PayPalMessageOfferType.PAY_LATER_SHORT_TERM, config.offer)
		assertEquals("Close Modal", config.modalCloseButton.alternativeText)
	}

	@Test
	fun testModalEvents() {
		// Create flags to track callback invocations
		var applyCalled = false
		var clickCalled = false
		var errorCalled = false
		var loadingCalled = false
		var successCalled = false

		// Create the events object with callbacks
		val events = ModalEvents(
			onApply = { applyCalled = true },
			onClick = { clickCalled = true },
			onError = { errorCalled = true },
			onLoading = { loadingCalled = true },
			onSuccess = { successCalled = true },
		)

		// Call each callback
		events.onApply()
		events.onClick()
		events.onError(PayPalErrors.Base("Test error"))
		events.onLoading()
		events.onSuccess()

		// Verify all callbacks were invoked
		assertTrue(applyCalled)
		assertTrue(clickCalled)
		assertTrue(errorCalled)
		assertTrue(loadingCalled)
		assertTrue(successCalled)
	}

	@Test
	fun testOfferTypeParsing() {
		// Test valid offer type parsing
		val validOfferType = "PAY_LATER_SHORT_TERM"
		val parsedValidType = try {
			PayPalMessageOfferType.valueOf(validOfferType)
		} catch (e: Exception) {
			null
		}
		assertEquals(PayPalMessageOfferType.PAY_LATER_SHORT_TERM, parsedValidType)

		// Test invalid offer type parsing
		val invalidOfferType = "INVALID_OFFER"
		val parsedInvalidType = try {
			PayPalMessageOfferType.valueOf(invalidOfferType)
		} catch (e: Exception) {
			null
		}
		assertNull(parsedInvalidType)
	}

	@Test
	fun testWebViewErrorHandling() {
		// Create a test WebView client
		val testWebViewClient = object : WebViewClient() {
			// Just testing that we can create a WebViewClient subclass
		}

		// Test that the client can be created without errors
		assertNotNull(testWebViewClient)
	}

	@Test
	fun testModalConfigWithNullValues() {
		// Test that we can create a ModalConfig with null values
		val config = ModalConfig(
			amount = null,
			buyerCountry = null,
			offer = null,
			modalCloseButton = ModalCloseButton(),
		)

		// Verify the config properties
		assertNull(config.amount)
		assertNull(config.buyerCountry)
		assertNull(config.offer)
		assertNotNull(config.modalCloseButton)
	}

	@Test
	fun testModalConfigDefaults() {
		// Test that we can create a ModalConfig with default values
		val config = ModalConfig()

		// Verify default values
		assertNull(config.amount)
		assertNull(config.buyerCountry)
		assertNull(config.offer)
		assertEquals(false, config.ignoreCache)
		assertEquals(false, config.devTouchpoint)
		assertNull(config.stageTag)
		assertNotNull(config.modalCloseButton)
	}

	@Test
	fun testModalConfigWithAllParameters() {
		// Test that we can create a ModalConfig with all parameters
		var applyCalled = false
		var clickCalled = false
		var errorCalled = false
		var loadingCalled = false
		var successCalled = false

		val events = ModalEvents(
			onApply = { applyCalled = true },
			onClick = { clickCalled = true },
			onError = { errorCalled = true },
			onLoading = { loadingCalled = true },
			onSuccess = { successCalled = true },
		)

		val config = ModalConfig(
			amount = 250.50,
			buyerCountry = "GB",
			offer = PayPalMessageOfferType.PAY_LATER_LONG_TERM,
			ignoreCache = true,
			devTouchpoint = true,
			stageTag = "test-stage",
			events = events,
			modalCloseButton = ModalCloseButton(alternativeText = "Custom Close"),
		)

		// Verify all properties
		assertEquals(250.50, config.amount)
		assertEquals("GB", config.buyerCountry)
		assertEquals(PayPalMessageOfferType.PAY_LATER_LONG_TERM, config.offer)
		assertEquals(true, config.ignoreCache)
		assertEquals(true, config.devTouchpoint)
		assertEquals("test-stage", config.stageTag)
		assertEquals("Custom Close", config.modalCloseButton.alternativeText)

		// Test events still work
		config.events?.onApply?.invoke()
		assertTrue(applyCalled)
	}

	@Test
	fun testModalCloseButtonDefaults() {
		// Test default ModalCloseButton
		val closeButton = ModalCloseButton()

		// Verify defaults
		assertNull(closeButton.alternativeText)
	}

	@Test
	fun testModalCloseButtonWithText() {
		// Test ModalCloseButton with custom text
		val closeButton = ModalCloseButton(alternativeText = "Dismiss Modal")

		// Verify custom text
		assertEquals("Dismiss Modal", closeButton.alternativeText)
	}

	@Test
	fun testDifferentOfferTypes() {
		// Test all offer types can be set in config
		val offerTypes = listOf(
			PayPalMessageOfferType.PAY_LATER_SHORT_TERM,
			PayPalMessageOfferType.PAY_LATER_LONG_TERM,
			PayPalMessageOfferType.PAY_LATER_PAY_IN_1,
			null,
		)

		offerTypes.forEach { offerType ->
			val config = ModalConfig(
				amount = 100.0,
				offer = offerType,
			)
			assertEquals(offerType, config.offer)
		}
	}

	@Test
	fun testEdgeCaseAmountsInModal() {
		// Test zero amount
		val zeroConfig = ModalConfig(amount = 0.0)
		assertEquals(0.0, zeroConfig.amount)

		// Test very large amount
		val largeConfig = ModalConfig(amount = 1000000.0)
		assertEquals(1000000.0, largeConfig.amount)

		// Test small decimal
		val smallConfig = ModalConfig(amount = 0.01)
		assertEquals(0.01, smallConfig.amount)

		// Test negative amount (should still be accepted, validation is elsewhere)
		val negativeConfig = ModalConfig(amount = -10.0)
		assertEquals(-10.0, negativeConfig.amount)
	}

	@Test
	fun testVariousBuyerCountriesInModal() {
		// Test common country codes
		val countries = listOf("US", "GB", "DE", "FR", "CA", "AU", "JP", "IT", "ES")

		countries.forEach { country ->
			val config = ModalConfig(
				amount = 100.0,
				buyerCountry = country,
			)
			assertEquals(country, config.buyerCountry)
		}
	}

	@Test
	fun testModalEventsIndependence() {
		// Test that each event can be called independently
		var applyCalled = false
		var clickCalled = false
		var errorCalled = false
		var loadingCalled = false
		var successCalled = false

		val events = ModalEvents(
			onApply = { applyCalled = true },
			onClick = { clickCalled = true },
			onError = { errorCalled = true },
			onLoading = { loadingCalled = true },
			onSuccess = { successCalled = true },
		)

		// Call only onApply
		events.onApply()
		assertTrue(applyCalled)
		assertTrue(!clickCalled && !errorCalled && !loadingCalled && !successCalled)

		// Reset and call only onClick
		applyCalled = false
		events.onClick()
		assertTrue(clickCalled)
		assertTrue(!applyCalled && !errorCalled && !loadingCalled && !successCalled)
	}

	@Test
	fun testModalEventsWithNullCallbacks() {
		// Test that ModalEvents can be created with minimal callbacks
		val minimalEvents = ModalEvents()

		// These should be no-ops if not defined
		assertNotNull(minimalEvents)
		// Calling these shouldn't throw exceptions
		minimalEvents.onApply()
		minimalEvents.onClick()
		minimalEvents.onLoading()
		minimalEvents.onSuccess()
	}

	@Test
	fun testErrorWithDifferentErrorTypes() {
		// Test that different error types can be passed to onError
		var capturedError: PayPalErrors.Base? = null

		val events = ModalEvents(
			onError = { error -> capturedError = error },
		)

		// Test with base error
		val baseError = PayPalErrors.Base("Base error")
		events.onError(baseError)
		assertEquals(baseError, capturedError)

		// Test with modal failed to load error
		val modalError = PayPalErrors.ModalFailedToLoad("Modal error", null)
		events.onError(modalError)
		assertEquals(modalError, capturedError)

		// Test with invalid client id error
		val clientIdError = PayPalErrors.InvalidClientId("test-id", null)
		events.onError(clientIdError)
		assertEquals(clientIdError, capturedError)
	}

	@Test
	fun testCacheBehavior() {
		// Test ignoreCache = true
		val noCacheConfig = ModalConfig(
			amount = 100.0,
			ignoreCache = true,
		)
		assertTrue(noCacheConfig.ignoreCache)

		// Test ignoreCache = false (default)
		val withCacheConfig = ModalConfig(
			amount = 100.0,
			ignoreCache = false,
		)
		assertTrue(!withCacheConfig.ignoreCache)
	}

	@Test
	fun testDevTouchpointFlag() {
		// Test devTouchpoint = true
		val devConfig = ModalConfig(
			amount = 100.0,
			devTouchpoint = true,
		)
		assertTrue(devConfig.devTouchpoint)

		// Test devTouchpoint = false (default)
		val prodConfig = ModalConfig(
			amount = 100.0,
			devTouchpoint = false,
		)
		assertTrue(!prodConfig.devTouchpoint)
	}

	@Test
	fun testStageTagValues() {
		// Test with stage tag
		val stagedConfig = ModalConfig(
			amount = 100.0,
			stageTag = "v1.2.3",
		)
		assertEquals("v1.2.3", stagedConfig.stageTag)

		// Test without stage tag
		val unstaged = ModalConfig(
			amount = 100.0,
			stageTag = null,
		)
		assertNull(unstaged.stageTag)

		// Test with empty stage tag
		val emptyStageConfig = ModalConfig(
			amount = 100.0,
			stageTag = "",
		)
		assertEquals("", emptyStageConfig.stageTag)
	}
}
