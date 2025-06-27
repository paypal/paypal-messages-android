package com.paypal.messages.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ContextCompatWrapperTest {

	// Mock dependencies
	private lateinit var mockAppCompatActivity: AppCompatActivity
	private lateinit var mockContextWrapper: ContextWrapper
	private lateinit var mockContext: Context

	@BeforeEach
	fun setup() {
		// Initialize mocks
		mockAppCompatActivity = mockk(relaxed = true)
		mockContextWrapper = mockk(relaxed = true)
		mockContext = mockk(relaxed = true)
	}

	@AfterEach
	fun tearDown() {
		unmockkAll()
	}

	@Test
	fun testFindAppCompatActivityWithAppCompatActivity() {
		// Test that when the context is already an AppCompatActivity, it's returned directly

		// Act
		val result = ContextCompatWrapper.findAppCompatActivity(mockAppCompatActivity)

		// Assert
		assertEquals(mockAppCompatActivity, result)
	}

	@Test
	fun testFindAppCompatActivityWithContextWrapper() {
		// Test that when the context is a ContextWrapper with an AppCompatActivity base,
		// the AppCompatActivity is found and returned

		// Arrange
		every { mockContextWrapper.baseContext } returns mockAppCompatActivity

		// Act
		val result = ContextCompatWrapper.findAppCompatActivity(mockContextWrapper)

		// Assert
		assertEquals(mockAppCompatActivity, result)
	}

	@Test
	fun testFindAppCompatActivityWithNestedContextWrapper() {
		// Test that when the context is a nested ContextWrapper with an AppCompatActivity deep inside,
		// the AppCompatActivity is found and returned

		// Arrange - create a chain of context wrappers
		val innerWrapper = mockk<ContextWrapper>(relaxed = true)
		val middleWrapper = mockk<ContextWrapper>(relaxed = true)
		val outerWrapper = mockk<ContextWrapper>(relaxed = true)

		every { innerWrapper.baseContext } returns mockAppCompatActivity
		every { middleWrapper.baseContext } returns innerWrapper
		every { outerWrapper.baseContext } returns middleWrapper

		// Act
		val result = ContextCompatWrapper.findAppCompatActivity(outerWrapper)

		// Assert
		assertEquals(mockAppCompatActivity, result)
	}

	@Test
	fun testFindAppCompatActivityWithRegularContext() {
		// Test that when the context is not an AppCompatActivity or a ContextWrapper with an
		// AppCompatActivity base, null is returned

		// Act
		val result = ContextCompatWrapper.findAppCompatActivity(mockContext)

		// Assert
		assertNull(result)
	}

	@Test
	fun testFindAppCompatActivityWithContextWrapperButNoAppCompatActivity() {
		// Test that when the context is a ContextWrapper but doesn't wrap an AppCompatActivity,
		// null is returned

		// Arrange
		every { mockContextWrapper.baseContext } returns mockContext

		// Act
		val result = ContextCompatWrapper.findAppCompatActivity(mockContextWrapper)

		// Assert
		assertNull(result)
	}

	@Test
	fun testFindAppCompatActivityWithCyclicContextWrapper() {
		// Test that when there's a cyclic reference in the context wrappers (which shouldn't happen
		// but we should handle it gracefully), null is returned to avoid infinite loops

		// Arrange - create a self-referencing context wrapper (pathological case)
		val cyclicWrapper = mockk<ContextWrapper>(relaxed = true)
		every { cyclicWrapper.baseContext } returns cyclicWrapper

		// Act
		val result = ContextCompatWrapper.findAppCompatActivity(cyclicWrapper)

		// Assert - this would loop infinitely if not handled properly, but our implementation
		// should protect against this by checking if the context changes in each iteration
		assertNull(result)
	}
}
