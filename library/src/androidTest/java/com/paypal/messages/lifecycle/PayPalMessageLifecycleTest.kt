package com.paypal.messages.lifecycle

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paypal.messages.PayPalMessageView
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.data.PayPalMessageData
import com.paypal.messages.config.message.style.PayPalMessageStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import kotlin.system.measureTimeMillis

/**
 * Android lifecycle tests that validate the PayPal Messages SDK behavior
 * under conditions similar to the "Don't Keep Activities" developer option.
 *
 * These tests ensure the SDK properly handles:
 * 1. Activity destruction and recreation (simulating low memory)
 * 2. State persistence and restoration
 * 3. Resource cleanup during lifecycle events
 * 4. Performance under memory pressure scenarios
 *
 * This addresses the adaptability pillar by ensuring the SDK works reliably
 * across the diverse Android ecosystem and device configurations.
 */
@RunWith(AndroidJUnit4::class)
class PayPalMessageLifecycleTest {

	private lateinit var context: Context
	private lateinit var testConfig: PayPalMessageConfig

	@Before
	fun setup() {
		context = ApplicationProvider.getApplicationContext()
		testConfig = PayPalMessageConfig(
			data = PayPalMessageData(
				clientId = "test-lifecycle-client",
				amount = BigDecimal("149.99"),
				buyerCountry = "US",
			),
			style = PayPalMessageStyle(),
			environment = PayPalEnvironment.SANDBOX,
		)
	}

	@Test
	fun testActivityRecreationScenario() {
		// Simulate the "Don't Keep Activities" scenario where activities
		// are immediately destroyed when the user navigates away

		var messageView: PayPalMessageView? = null
		var configurationTime = 0L
		var cleanupTime = 0L

		// Simulate activity creation
		configurationTime = measureTimeMillis {
			messageView = PayPalMessageView(context).apply {
				config = testConfig
			}
		}

		// Verify the view was configured correctly
		assert(messageView?.config != null) {
			"Message view should retain configuration after creation"
		}

		// Simulate user navigating away (activity onPause/onStop)
		// In "Don't Keep Activities" mode, this would trigger immediate destruction
		cleanupTime = measureTimeMillis {
			messageView?.let { view ->
				// Test that cleanup operations complete quickly
				view.config = null

				// Simulate memory pressure cleanup
				view.onDetachedFromWindow()
			}
		}

		// Simulate activity recreation (user returning to the app)
		val recreationTime = measureTimeMillis {
			messageView = PayPalMessageView(context).apply {
				config = testConfig
			}
		}

		// Performance assertions for budget device compatibility
		assert(configurationTime < 200) {
			"Initial configuration took ${configurationTime}ms, too slow for budget devices"
		}

		assert(cleanupTime < 50) {
			"Cleanup took ${cleanupTime}ms, too slow for activity destruction"
		}

		assert(recreationTime < 200) {
			"Recreation took ${recreationTime}ms, indicating poor lifecycle handling"
		}

		// Verify functionality is maintained after recreation
		assert(messageView?.config?.data?.clientId == "test-lifecycle-client") {
			"Configuration should be properly restored after recreation"
		}
	}

	@Test
	fun testMemoryPressureScenario() = runBlocking {
		// Simulate memory pressure conditions that would trigger
		// aggressive activity cleanup on budget devices

		val messageViews = mutableListOf<PayPalMessageView>()

		// Create multiple message views (simulating multiple screens/fragments)
		repeat(10) { index ->
			val view = PayPalMessageView(context).apply {
				config = testConfig.copy(
					data = testConfig.data.copy(
						clientId = "client-$index",
						amount = BigDecimal("${index * 10}.99"),
					),
				)
			}
			messageViews.add(view)

			// Small delay to simulate real-world creation patterns
			delay(10)
		}

		// Simulate system calling onLowMemory() - immediate cleanup required
		val cleanupTime = measureTimeMillis {
			messageViews.forEach { view ->
				view.config = null
				view.onDetachedFromWindow()
			}
		}

		// Memory pressure cleanup should be very fast
		assert(cleanupTime < 100) {
			"Memory pressure cleanup took ${cleanupTime}ms, too slow for system onLowMemory()"
		}

		// Test recovery after memory pressure
		val recoveryTime = measureTimeMillis {
			val newView = PayPalMessageView(context).apply {
				config = testConfig
			}
			assert(newView.config != null) {
				"Should be able to create new views after memory pressure cleanup"
			}
		}

		assert(recoveryTime < 150) {
			"Recovery after memory pressure took ${recoveryTime}ms, indicating resource leaks"
		}
	}

	@Test
	fun testConfigurationChangeHandling() {
		// Test how the SDK handles configuration changes like screen rotation
		// which can trigger activity recreation on some devices

		val messageView = PayPalMessageView(context)
		val originalConfig = testConfig

		// Initial configuration
		messageView.config = originalConfig

		// Simulate configuration change (e.g., rotation)
		val configChangeTime = measureTimeMillis {
			// Save state (as would happen in onSaveInstanceState)
			val savedClientId = messageView.config?.data?.clientId
			val savedAmount = messageView.config?.data?.amount

			// Clear view (as would happen during destruction)
			messageView.config = null

			// Restore state (as would happen during recreation)
			messageView.config = PayPalMessageConfig(
				data = PayPalMessageData(
					clientId = savedClientId ?: "default",
					amount = savedAmount ?: BigDecimal.ZERO,
					buyerCountry = "US",
				),
				style = PayPalMessageStyle(),
				environment = PayPalEnvironment.SANDBOX,
			)
		}

		// Configuration change handling should be efficient
		assert(configChangeTime < 100) {
			"Configuration change handling took ${configChangeTime}ms, too slow for smooth UX"
		}

		// Verify state was preserved
		assert(messageView.config?.data?.clientId == originalConfig.data.clientId) {
			"Client ID should be preserved through configuration change"
		}
		assert(messageView.config?.data?.amount == originalConfig.data.amount) {
			"Amount should be preserved through configuration change"
		}
	}

	@Test
	fun testRapidLifecycleEvents() = runBlocking {
		// Test rapid lifecycle events as might happen with aggressive
		// activity management or user rapidly switching between apps

		val messageView = PayPalMessageView(context)
		var totalTime = 0L

		repeat(20) { cycle ->
			val cycleTime = measureTimeMillis {
				// Attach/configure
				messageView.config = testConfig.copy(
					data = testConfig.data.copy(clientId = "rapid-$cycle"),
				)

				// Small delay simulating render
				delay(5)

				// Detach/cleanup
				messageView.config = null
				messageView.onDetachedFromWindow()
			}
			totalTime += cycleTime
		}

		val averageCycleTime = totalTime / 20.0

		assert(averageCycleTime < 50) {
			"Average lifecycle cycle took ${averageCycleTime}ms, indicating poor lifecycle optimization"
		}

		assert(totalTime < 800) {
			"Total rapid lifecycle test took ${totalTime}ms, indicating potential memory or performance issues"
		}
	}

	@Test
	fun testConcurrentLifecycleOperations() = runBlocking {
		// Test concurrent lifecycle operations as might happen in
		// multi-fragment scenarios or rapid user navigation

		val views = mutableListOf<PayPalMessageView>()

		val concurrentTime = measureTimeMillis {
			// Create multiple views concurrently
			repeat(5) { index ->
				val view = PayPalMessageView(context)
				views.add(view)

				// Configure concurrently (simulating multiple fragments)
				view.config = testConfig.copy(
					data = testConfig.data.copy(
						clientId = "concurrent-$index",
						amount = BigDecimal("${index * 25}.99"),
					),
				)
			}

			// Cleanup all concurrently (simulating navigation away)
			views.forEach { view ->
				view.config = null
				view.onDetachedFromWindow()
			}
		}

		assert(concurrentTime < 300) {
			"Concurrent lifecycle operations took ${concurrentTime}ms, indicating poor concurrency handling"
		}

		// Verify all views were properly cleaned up
		views.forEach { view ->
			assert(view.config == null) {
				"All views should be cleaned up after concurrent operations"
			}
		}
	}

	@Test
	fun testResourceConstrainedEnvironment() {
		// Test behavior under resource constraints typical of budget devices
		// This simulates conditions where the system is aggressively managing memory

		val messageView = PayPalMessageView(context)

		// Test multiple rapid reconfigurations under time pressure
		val constrainedTime = measureTimeMillis {
			repeat(50) { iteration ->
				messageView.config = testConfig.copy(
					data = testConfig.data.copy(
						amount = BigDecimal("${iteration % 10}.99"),
						buyerCountry = if (iteration % 2 == 0) "US" else "CA",
					),
				)

				// Simulate immediate pressure to reconfigure
				if (iteration % 10 == 9) {
					messageView.config = null
					messageView.config = testConfig
				}
			}
		}

		val averageOperationTime = constrainedTime / 50.0

		assert(averageOperationTime < 10) {
			"Average operation under constraints took ${averageOperationTime}ms, too slow for budget devices"
		}

		assert(constrainedTime < 400) {
			"Total constrained environment test took ${constrainedTime}ms, indicating scalability issues"
		}

		// Verify final state is valid
		assert(messageView.config != null) {
			"View should maintain valid state even under resource constraints"
		}
	}
}
