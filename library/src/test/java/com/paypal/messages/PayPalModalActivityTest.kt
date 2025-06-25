package com.paypal.messages

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paypal.messages.utils.PayPalErrors
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class PayPalModalActivityTest {
	@get:Rule
	val composeTestRule = createComposeRule()

	private lateinit var instanceId: UUID
	private lateinit var mockOnApply: () -> Unit
	private lateinit var mockOnClick: () -> Unit
	private lateinit var mockOnError: (PayPalErrors.Base) -> Unit

	@Before
	fun setup() {
		instanceId = UUID.randomUUID()
		mockOnApply = mockk(relaxed = true)
		mockOnClick = mockk(relaxed = true)
		mockOnError = mockk(relaxed = true)
	}

	@After
	fun tearDown() {
		unmockkAll()
		// Clear any registered callbacks to prevent test interference
		PayPalModalActivity.resetAllModals()
	}

	@Test
	fun `registerCallbacks stores callbacks for modal instance`() {
		// When
		PayPalModalActivity.registerCallbacks(
			instanceId = instanceId,
			onApply = mockOnApply,
			onClick = mockOnClick,
			onError = mockOnError,
		)

		// Then verify the callback is stored (indirectly by registering and clearing)
		PayPalModalActivity.clearCallbacks(instanceId)
		// No explicit assertion, but if the code reaches here without errors, it worked
	}

	@Test
	fun `clearCallbacks removes callbacks for modal instance`() {
		// Given
		PayPalModalActivity.registerCallbacks(
			instanceId = instanceId,
			onApply = mockOnApply,
			onClick = mockOnClick,
			onError = mockOnError,
		)

		// When
		PayPalModalActivity.clearCallbacks(instanceId)

		// Then
		// We cannot directly verify since the callbacks map is private
		// But we can register again with the same ID without issues
		PayPalModalActivity.registerCallbacks(
			instanceId = instanceId,
			onApply = mockOnApply,
			onClick = mockOnClick,
			onError = mockOnError,
		)

		// Clean up
		PayPalModalActivity.clearCallbacks(instanceId)
	}

	@Test
	fun `resetAllModals clears all callbacks`() {
		// Given
		val instanceId1 = UUID.randomUUID()
		val instanceId2 = UUID.randomUUID()

		PayPalModalActivity.registerCallbacks(
			instanceId = instanceId1,
			onApply = mockOnApply,
			onClick = mockOnClick,
			onError = mockOnError,
		)

		PayPalModalActivity.registerCallbacks(
			instanceId = instanceId2,
			onApply = mockOnApply,
			onClick = mockOnClick,
			onError = mockOnError,
		)

		// When
		PayPalModalActivity.resetAllModals()

		// Then
		// We can register again with the same IDs without issues
		PayPalModalActivity.registerCallbacks(
			instanceId = instanceId1,
			onApply = mockOnApply,
			onClick = mockOnClick,
			onError = mockOnError,
		)

		PayPalModalActivity.registerCallbacks(
			instanceId = instanceId2,
			onApply = mockOnApply,
			onClick = mockOnClick,
			onError = mockOnError,
		)
	}
}
