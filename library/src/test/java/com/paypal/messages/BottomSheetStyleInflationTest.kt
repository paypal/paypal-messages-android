package com.paypal.messages

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BottomSheetStyleInflationTest {
	@Test
	fun inflatesBottomSheetDialogWithOurStyle() {
		val context = ApplicationProvider.getApplicationContext<Application>()
		val dialog = BottomSheetDialog(context, R.style.BottomSheetDialog)
		assertNotNull(dialog)
	}
}
