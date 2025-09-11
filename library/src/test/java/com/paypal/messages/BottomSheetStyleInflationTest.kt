package com.paypal.messages

import android.app.Application
import android.view.ContextThemeWrapper
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
		// BottomSheetDialog with a MaterialComponents overlay requires a base MaterialComponents theme.
		val themedContext = ContextThemeWrapper(
			context,
			com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar,
		)
		val dialog = BottomSheetDialog(themedContext, R.style.BottomSheetDialog)
		assertNotNull(dialog)
	}
}
