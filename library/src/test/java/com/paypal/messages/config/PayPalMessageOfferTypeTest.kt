package com.paypal.messages.config

import com.paypal.messages.errors.IllegalEnumArg
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PayPalMessageOfferTypeTest {
	@Test
	fun testPayLaterShortTerm() {
		assertEquals(PayPalMessageOfferType.PAY_LATER_SHORT_TERM.value, 0)
		assertEquals(PayPalMessageOfferType.PAY_LATER_SHORT_TERM.toString(), "PAY_LATER_SHORT_TERM")
	}

	@Test
	fun testPayLaterLongTerm() {
		assertEquals(PayPalMessageOfferType.PAY_LATER_LONG_TERM.value, 1)
		assertEquals(PayPalMessageOfferType.PAY_LATER_LONG_TERM.toString(), "PAY_LATER_LONG_TERM")
	}

	@Test
	fun testPayLaterPayIn1() {
		assertEquals(PayPalMessageOfferType.PAY_LATER_PAY_IN_1.value, 2)
		assertEquals(PayPalMessageOfferType.PAY_LATER_PAY_IN_1.toString(), "PAY_LATER_PAY_IN_1")
	}

	@Test
	fun testPayPalCreditNoInterest() {
		assertEquals(PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST.value, 3)
		assertEquals(PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST.toString(), "PAYPAL_CREDIT_NO_INTEREST")
	}

	@Test
	fun testInvalidIndex() {
		// Assert that an IllegalArgumentException is thrown when an invalid index is provided.
		assertThrows(IllegalEnumArg::class.java) { PayPalMessageOfferType(99) }
	}

	@Test
	fun testFromString() {
		// Assert that the fromString() method correctly returns the corresponding enum value.
		assertEquals(PayPalMessageOfferType.PAY_LATER_SHORT_TERM, PayPalMessageOfferType(0))
		assertEquals(PayPalMessageOfferType.PAY_LATER_LONG_TERM, PayPalMessageOfferType(1))
		assertEquals(PayPalMessageOfferType.PAY_LATER_PAY_IN_1, PayPalMessageOfferType(2))
		assertEquals(PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST, PayPalMessageOfferType(3))
	}
}
