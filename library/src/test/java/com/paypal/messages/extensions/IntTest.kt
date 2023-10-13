package com.paypal.messages.extensions

import android.content.res.Resources
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.math.roundToInt

// TODO Fix test with mock
class IntTest {
// 	@Test
	fun testDpExtension() {
		val expectedDp = 10
		// Method getSystem in android.content.res.Resources not mocked
		val expectedPixels = (expectedDp * Resources.getSystem().displayMetrics.density).roundToInt()

		val actualDp = expectedDp.dp

		assertEquals(expectedPixels, actualDp)
	}
}
