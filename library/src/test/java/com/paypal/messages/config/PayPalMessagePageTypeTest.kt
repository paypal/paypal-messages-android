package com.paypal.messages.config

import com.paypal.messages.utils.PayPalErrors
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PayPalMessagePageTypeTest {
	@Test
	fun testCart() {
		assertEquals(PayPalMessagePageType.CART.value, 0)
		assertEquals(PayPalMessagePageType.CART.toString(), "CART")
	}

	@Test
	fun testCheckout() {
		assertEquals(PayPalMessagePageType.CHECKOUT.value, 1)
		assertEquals(PayPalMessagePageType.CHECKOUT.toString(), "CHECKOUT")
	}

	@Test
	fun testHome() {
		assertEquals(PayPalMessagePageType.HOME.value, 2)
		assertEquals(PayPalMessagePageType.HOME.toString(), "HOME")
	}

	@Test
	fun testMini_cart() {
		assertEquals(PayPalMessagePageType.MINI_CART.value, 3)
		assertEquals(PayPalMessagePageType.MINI_CART.toString(), "MINI_CART")
	}

	@Test
	fun testProduct_details() {
		assertEquals(PayPalMessagePageType.PRODUCT_DETAILS.value, 4)
		assertEquals(PayPalMessagePageType.PRODUCT_DETAILS.toString(), "PRODUCT_DETAILS")
	}

	@Test
	fun testProduct_listing() {
		assertEquals(PayPalMessagePageType.PRODUCT_LISTING.value, 5)
		assertEquals(PayPalMessagePageType.PRODUCT_LISTING.toString(), "PRODUCT_LISTING")
	}

	@Test
	fun testSearch_results() {
		assertEquals(PayPalMessagePageType.SEARCH_RESULTS.value, 6)
		assertEquals(PayPalMessagePageType.SEARCH_RESULTS.toString(), "SEARCH_RESULTS")
	}

	@Test
	fun testInvalidIndex() {
		assertThrows(PayPalErrors.IllegalEnumArg::class.java) { PayPalMessagePageType(99) }
	}

	@Test
	fun testFromString() {
		assertEquals(PayPalMessagePageType.CART, PayPalMessagePageType(0))
		assertEquals(PayPalMessagePageType.CHECKOUT, PayPalMessagePageType(1))
		assertEquals(PayPalMessagePageType.HOME, PayPalMessagePageType(2))
		assertEquals(PayPalMessagePageType.MINI_CART, PayPalMessagePageType(3))
		assertEquals(PayPalMessagePageType.PRODUCT_DETAILS, PayPalMessagePageType(4))
		assertEquals(PayPalMessagePageType.PRODUCT_LISTING, PayPalMessagePageType(5))
		assertEquals(PayPalMessagePageType.SEARCH_RESULTS, PayPalMessagePageType(6))
	}
}
