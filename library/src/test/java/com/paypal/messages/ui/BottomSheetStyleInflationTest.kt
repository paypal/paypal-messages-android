package com.paypal.messages.ui

import android.app.Application
import android.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.paypal.messages.R
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BottomSheetStyleInflationTest {

	@Test
	fun bottomSheetDialog_inflates_withMaterialThemeAndCustomOverlay() {
		val app = ApplicationProvider.getApplicationContext<Application>()
		val themed = ContextThemeWrapper(app, R.style.Theme_PayPalModal)

		val dialog = BottomSheetDialog(themed)
		dialog.setContentView(android.R.layout.simple_list_item_1)

		// Verify dialog and its behavior exist; this implicitly exercises
		// style/attr resolution (e.g., shapeAppearanceOverlay, bottomSheetModal)
		assertNotNull(dialog)
		val window = dialog.window
		assertNotNull("Dialog window should be created", window)
	}
}
