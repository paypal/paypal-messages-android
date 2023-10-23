package com.paypal.messages.extensions

import android.content.res.Resources
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class IntTest {
 	@Test
	fun testDpExtension() {
		val basicInteger = 10
		val expectedDp = (basicInteger * Resources.getSystem().displayMetrics.density).roundToInt()
		val actualDp = basicInteger.dp
		assertEquals(expectedDp, actualDp)
	}
}
