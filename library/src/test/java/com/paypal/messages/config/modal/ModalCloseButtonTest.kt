package com.paypal.messages.config.modal

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class ModalCloseButtonTest {
	@Test
	fun testConstructor() {
		val modalCloseButton = ModalCloseButton(
			width = 100,
			height = 100,
			availableWidth = 200,
			availableHeight = 200,
			color = "#FFFFFF",
			colorType = "solid",
		)

		val gson = Gson()
		val json = gson.toJson(modalCloseButton)

		assertEquals(modalCloseButton.width, 100)
		assertEquals(modalCloseButton.height, 100)
		assertEquals(modalCloseButton.availableWidth, 200)
		assertEquals(modalCloseButton.availableHeight, 200)
		assertEquals(modalCloseButton.color, "#FFFFFF")
		assertEquals(modalCloseButton.colorType, "solid")
		assertEquals(json, "{\"width\":100,\"height\":100,\"available_width\":200,\"available_height\":200,\"color\":\"#FFFFFF\",\"color_type\":\"solid\"}")
	}
}
