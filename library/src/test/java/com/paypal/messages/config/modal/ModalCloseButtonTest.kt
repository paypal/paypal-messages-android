package com.paypal.messages.config.modal

import com.google.gson.Gson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ModalCloseButtonTest {
	private val modalCloseButton = ModalCloseButton(
		width = 100,
		height = 100,
		availableWidth = 200,
		availableHeight = 200,
		color = "#FFFFFF",
		colorType = "solid",
		alternativeText = "test_alternative_text",
	)

	@Test
	fun testConstructor() {
		assertEquals(modalCloseButton.width, 100)
		assertEquals(modalCloseButton.height, 100)
		assertEquals(modalCloseButton.availableWidth, 200)
		assertEquals(modalCloseButton.availableHeight, 200)
		assertEquals(modalCloseButton.color, "#FFFFFF")
		assertEquals(modalCloseButton.colorType, "solid")
		assertEquals(modalCloseButton.alternativeText, "test_alternative_text")
	}

	@Test
	fun testSerialization() {
		val gson = Gson()
		val json = gson.toJson(modalCloseButton)

		assertEquals(
			json,
			@Suppress("ktlint:standard:max-line-length")
			"""{"width":100,"height":100,"available_width":200,"available_height":200,"color":"#FFFFFF","color_type":"solid","alternative_text":"test_alternative_text"}""",
		)
	}
}
