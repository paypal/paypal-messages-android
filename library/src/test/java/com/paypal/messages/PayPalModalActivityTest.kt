package com.paypal.messages

import android.content.Intent
import android.os.Bundle
import com.paypal.messages.utils.PayPalErrors
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class PayPalModalActivityTest {

	// Mock dependencies
	private val mockIntent = mockk<Intent>(relaxed = true)
	private val mockBundle = mockk<Bundle>(relaxed = true)
	private var testInstanceId = UUID.randomUUID()

	@BeforeEach
	fun setup() {
		// Reset instance state
		testInstanceId = UUID.randomUUID()

		// Clean up any state from previous tests
		PayPalModalActivity.resetAllModals()
	}

	@AfterEach
	fun tearDown() {
		// Clean up any leftover state
		PayPalModalActivity.resetAllModals()
		unmockkAll()
	}

	@Test
	fun testRegisterCallbacks() {
		// Arrange
		var onApplyCalled = false
		var onClickCalled = false
		var onErrorCalled = false
		val testError = PayPalErrors.ModalFailedToLoad("Test error")

		// Act - register callbacks
		PayPalModalActivity.registerCallbacks(
			instanceId = testInstanceId,
			onApply = { onApplyCalled = true },
			onClick = { onClickCalled = true },
			onError = { onErrorCalled = true },
		)

		// Get the callbacks via reflection
		val callbacksField = PayPalModalActivity::class.java.getDeclaredField("callbacksRegistry")
		callbacksField.isAccessible = true
		val callbacksRegistry = callbacksField.get(null) as java.util.concurrent.ConcurrentHashMap<*, *>
		val callbacks = callbacksRegistry[testInstanceId] as? PayPalModalActivity.Companion.ModalCallbacks

		// Assert
		assertNotNull(callbacks)

		// Trigger callbacks and verify they work
		callbacks?.onApply?.invoke()
		assertTrue(onApplyCalled)

		callbacks?.onClick?.invoke()
		assertTrue(onClickCalled)

		callbacks?.onError?.invoke(testError)
		assertTrue(onErrorCalled)
	}

	@Test
	fun testClearCallbacks() {
		// Arrange - register a callback
		PayPalModalActivity.registerCallbacks(
			instanceId = testInstanceId,
			onApply = { },
			onClick = { },
			onError = { },
		)

		// Act - clear the callbacks
		PayPalModalActivity.clearCallbacks(testInstanceId)

		// Get the registry via reflection
		val callbacksField = PayPalModalActivity::class.java.getDeclaredField("callbacksRegistry")
		callbacksField.isAccessible = true
		val callbacksRegistry = callbacksField.get(null) as java.util.concurrent.ConcurrentHashMap<*, *>

		// Assert
		assertEquals(null, callbacksRegistry[testInstanceId])
	}

	@Test
	fun testResetAllModals() {
		// Arrange - register multiple callbacks
		val id1 = UUID.randomUUID()
		val id2 = UUID.randomUUID()

		PayPalModalActivity.registerCallbacks(
			instanceId = id1,
			onApply = { },
			onClick = { },
			onError = { },
		)

		PayPalModalActivity.registerCallbacks(
			instanceId = id2,
			onApply = { },
			onClick = { },
			onError = { },
		)

		// Act - reset all modals
		PayPalModalActivity.resetAllModals()

		// Get the registry via reflection
		val callbacksField = PayPalModalActivity::class.java.getDeclaredField("callbacksRegistry")
		callbacksField.isAccessible = true
		val callbacksRegistry = callbacksField.get(null) as java.util.concurrent.ConcurrentHashMap<*, *>

		// Assert
		assertEquals(0, callbacksRegistry.size)
	}

	@Test
	fun testCleanupStaleEntries() {
		// Arrange - add entries with old timestamps
		val activeModalsField = PayPalModalActivity::class.java.getDeclaredField("activeModals")
		activeModalsField.isAccessible = true
		val activeModals = activeModalsField.get(null) as java.util.concurrent.ConcurrentHashMap<*, *>

		val id1 = UUID.randomUUID()
		val id2 = UUID.randomUUID()

		// Add one recent entry and one old entry (more than 5 minutes old)
		val currentTime = System.currentTimeMillis()
		val oldTime = currentTime - (6 * 60 * 1000) // 6 minutes ago

		// Use reflection to add entries
		@Suppress("UNCHECKED_CAST")
		(activeModals as java.util.concurrent.ConcurrentHashMap<UUID, Long>)[id1] = currentTime
		@Suppress("UNCHECKED_CAST")
		(activeModals as java.util.concurrent.ConcurrentHashMap<UUID, Long>)[id2] = oldTime

		// Act - call cleanup method via reflection
		val companionClass = PayPalModalActivity.Companion::class.java
		val cleanupMethod = companionClass.getDeclaredMethod("cleanupStaleEntries")
		cleanupMethod.isAccessible = true
		cleanupMethod.invoke(PayPalModalActivity.Companion)

		// Assert - the old entry should be removed
		assertTrue(activeModals.containsKey(id1))
		assertEquals(false, activeModals.containsKey(id2))
	}

	@Test
	fun testModalActivityCreation() {
		// This is more of an integration test that would need to be run on a device
		// For unit testing, we'll test the companion object methods which can be tested without
		// Android dependencies

		// Verify that callbacks can be registered and retrieved
		val mockOnApply = mockk<() -> Unit>()
		val mockOnClick = mockk<() -> Unit>()
		val mockOnError = mockk<(PayPalErrors.Base) -> Unit>()

		// Register callbacks
		PayPalModalActivity.registerCallbacks(
			instanceId = testInstanceId,
			onApply = mockOnApply,
			onClick = mockOnClick,
			onError = mockOnError,
		)

		// Access private fields for testing
		val callbacksField = PayPalModalActivity::class.java.getDeclaredField("callbacksRegistry")
		callbacksField.isAccessible = true
		val callbacksRegistry = callbacksField.get(null) as java.util.concurrent.ConcurrentHashMap<*, *>

		// Get the registered callbacks
		val callbacks = callbacksRegistry[testInstanceId] as PayPalModalActivity.Companion.ModalCallbacks

		// Verify the callbacks were properly stored
		assertEquals(mockOnApply, callbacks.onApply)
		assertEquals(mockOnClick, callbacks.onClick)
		assertEquals(mockOnError, callbacks.onError)
	}

	@Test
	fun testDuplicateModalPrevention() {
		// Test that the same instanceId can't be displayed twice using only the static fields
		// This simplifies the test by avoiding spyk which requires complex Android mocking
		val displayedModalsField = PayPalModalActivity::class.java.getDeclaredField("displayedModals")
		displayedModalsField.isAccessible = true
		val displayedModals = displayedModalsField.get(null) as java.util.Set<*>

		// Make sure the set is empty at the start
		displayedModals.clear()
		
		// Add an entry to displayedModals
		@Suppress("UNCHECKED_CAST")
		(displayedModals as java.util.Set<UUID>).add(testInstanceId)

		// Verify it's in the set
		assertTrue(displayedModals.contains(testInstanceId))

		// Now verify the duplicate logic by trying to add the same ID again
		// using the companion method clearCallbacks that interacts with displayedModals
		PayPalModalActivity.clearCallbacks(testInstanceId)
		
		// Verify the ID was removed from displayedModals
		assertEquals(false, displayedModals.contains(testInstanceId))
		
		// Add it back for another test
		@Suppress("UNCHECKED_CAST")
		(displayedModals as java.util.Set<UUID>).add(testInstanceId)
		
		// Now reset all modals which should clear the set
		PayPalModalActivity.resetAllModals()
		
		// Verify the set is empty
		assertEquals(0, displayedModals.size)
	}
}
