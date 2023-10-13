package com.paypal.messages.config

import com.paypal.messages.utils.PayPalErrors
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

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
		assertThrows(PayPalErrors.IllegalEnumArg::class.java) { PayPalMessageOfferType(99) }
	}

	@Test
	fun testFromString() {
		assertEquals(PayPalMessageOfferType.PAY_LATER_SHORT_TERM, PayPalMessageOfferType(0))
		assertEquals(PayPalMessageOfferType.PAY_LATER_LONG_TERM, PayPalMessageOfferType(1))
		assertEquals(PayPalMessageOfferType.PAY_LATER_PAY_IN_1, PayPalMessageOfferType(2))
		assertEquals(PayPalMessageOfferType.PAYPAL_CREDIT_NO_INTEREST, PayPalMessageOfferType(3))
	}
}
