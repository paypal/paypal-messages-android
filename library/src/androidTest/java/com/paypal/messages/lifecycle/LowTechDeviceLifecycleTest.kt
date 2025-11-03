package com.paypal.messages.lifecycle

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paypal.messages.ModalFragment
import com.paypal.messages.PayPalMessageView
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.PayPalMessageOfferType
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.PayPalMessageStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

/**
 * Comprehensive lifecycle tests for low-tech devices and "Don't Keep Activities" scenarios.
 *
 * This test suite specifically addresses:
 * 1. WebView state handling during activity recreation
 * 2. Network request behavior during activity destruction
 * 3. Memory leak detection in rapid lifecycle scenarios
 * 4. State persistence across configuration changes
 * 5. Performance on resource-constrained devices
 *
 * These tests ensure the SDK works reliably on budget Android devices globally,
 * supporting the affordability and adaptability pillars.
 */
@RunWith(AndroidJUnit4::class)
class LowTechDeviceLifecycleTest {

	private lateinit var context: Context
	private lateinit var testConfig: PayPalMessageConfig

	@Before
	fun setup() {
		context = ApplicationProvider.getApplicationContext()
		testConfig = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "low-tech-test-client",
			).apply {
				amount = 99.99
				buyerCountry = "US"
				environment = PayPalEnvironment.SANDBOX
			},
			style = PayPalMessageStyle(),
		)
	}

	/**
	 * Test WebView state handling during rapid activity recreation.
	 * Simulates "Don't Keep Activities" with modal WebView open.
	 */
	@Test
	fun testWebViewStateHandlingDuringRecreation() {
		var scenario: FragmentScenario<ModalFragment>? = null
		var fragmentRecreationTime = 0L

		try {
			// Create modal fragment with WebView
			val fragmentArgs = Bundle().apply {
				putString("client_id", "webview-test-client")
			}

			// Launch fragment (this would load a WebView in real scenario)
			val launchTime = measureTimeMillis {
				scenario = launchFragmentInContainer(
					fragmentArgs = fragmentArgs,
					themeResId = androidx.appcompat.R.style.Theme_AppCompat,
				)
			}

			// Simulate user backgrounding app (fragment moves to STARTED)
			scenario?.moveToState(Lifecycle.State.STARTED)

			// Simulate "Don't Keep Activities" destroying the fragment
			scenario?.moveToState(Lifecycle.State.CREATED)

			// Recreate the fragment (as would happen when user returns)
			fragmentRecreationTime = measureTimeMillis {
				scenario?.recreate()
			}

			// Verify fragment was recreated successfully
			scenario?.onFragment { fragment ->
				assertNotNull("Fragment should exist after recreation", fragment)
				assertNotNull("Fragment arguments should be preserved", fragment.arguments)
				assertEquals(
					"webview-test-client",
					fragment.arguments?.getString("client_id"),
				)
			}

			// WebView recreation should be reasonably fast even on budget devices
			assert(fragmentRecreationTime < 1000) {
				"Fragment with WebView recreation took ${fragmentRecreationTime}ms, too slow for budget devices"
			}

			assert(launchTime < 500) {
				"Initial fragment launch took ${launchTime}ms, too slow for low-tech devices"
			}
		} finally {
			scenario?.close()
		}
	}

	/**
	 * Test multiple rapid WebView lifecycle events.
	 * Simulates user rapidly switching between apps with "Don't Keep Activities" enabled.
	 */
	@Test
	fun testRapidWebViewLifecycleEvents() = runBlocking {
		val scenarios = mutableListOf<FragmentScenario<ModalFragment>>()
		val cycleTimes = mutableListOf<Long>()

		try {
			// Simulate 5 rapid open/close cycles
			repeat(5) { cycle ->
				val cycleTime = measureTimeMillis {
					// Create modal fragment
					val scenario = launchFragmentInContainer<ModalFragment>(
						fragmentArgs = Bundle().apply {
							putString("client_id", "rapid-webview-$cycle")
						},
						themeResId = androidx.appcompat.R.style.Theme_AppCompat,
					)

					// Move through lifecycle states rapidly
					scenario.moveToState(Lifecycle.State.STARTED)
					scenario.moveToState(Lifecycle.State.RESUMED)

					// Brief delay simulating user interaction
					delay(50)

					// Background and destroy (simulating "Don't Keep Activities")
					scenario.moveToState(Lifecycle.State.STARTED)
					scenario.moveToState(Lifecycle.State.CREATED)

					scenarios.add(scenario)
				}
				cycleTimes.add(cycleTime)
			}

			val averageCycleTime = cycleTimes.average()
			val maxCycleTime = cycleTimes.maxOrNull() ?: 0L

			println("WebView lifecycle cycle times: $cycleTimes")
			println("Average cycle time: ${averageCycleTime}ms")
			println("Max cycle time: ${maxCycleTime}ms")

			// Rapid WebView lifecycle events should not cause performance degradation
			assert(averageCycleTime < 1000) {
				"Average WebView lifecycle cycle took ${averageCycleTime}ms, too slow for low-tech devices"
			}

			assert(maxCycleTime < 1500) {
				"Maximum WebView lifecycle cycle took ${maxCycleTime}ms, indicating memory pressure"
			}

			// Check for performance degradation over cycles
			val firstHalfAverage = cycleTimes.take(cycleTimes.size / 2).average()
			val secondHalfAverage = cycleTimes.takeLast(cycleTimes.size / 2).average()
			val degradationRatio = secondHalfAverage / firstHalfAverage

			assert(degradationRatio < 1.8) {
				"WebView performance degraded by ${(degradationRatio - 1) * 100}% over cycles, indicating leaks"
			}
		} finally {
			// Clean up all scenarios
			scenarios.forEach { it.close() }
		}
	}

	/**
	 * Test network request handling during activity destruction.
	 * Ensures in-flight requests don't cause crashes or leaks.
	 */
	@Test
	fun testNetworkRequestDuringActivityDestruction() = runBlocking {
		val messageViews = mutableListOf<PayPalMessageView>()

		// Create multiple message views that would trigger network requests
		repeat(3) { index ->
			val view = PayPalMessageView(context)
			view.setConfig(
				testConfig.clone().apply {
					data.clientID = "network-test-$index"
					data.amount = (index * 100).toDouble() + 0.99
				},
			)
			messageViews.add(view)

			// Small delay to allow potential network request initiation
			delay(100)
		}

		// Immediately destroy all views (simulating "Don't Keep Activities")
		val destructionTime = measureTimeMillis {
			messageViews.forEach { view ->
				// Setting empty config simulates cleanup
				view.setConfig(PayPalMessageConfig(PayPalMessageData(clientID = "")))
			}
		}

		println("Network cleanup during destruction took: ${destructionTime}ms")

		// Destruction should complete quickly despite potential in-flight requests
		assert(destructionTime < 200) {
			"Destruction with potential network requests took ${destructionTime}ms, too slow"
		}

		// Allow time for any background operations to complete
		delay(500)

		// Create new view to ensure system is still functional
		val recoveryView = PayPalMessageView(context)
		recoveryView.setConfig(testConfig)

		assertNotNull("Should be able to create new views after network cleanup", recoveryView.getConfig())
	}

	/**
	 * Test that rapid network request cycles don't cause memory leaks.
	 */
	@Test
	fun testNetworkRequestMemoryLeakPrevention() = runBlocking {
		val requestCycles = 10
		val cycleTimes = mutableListOf<Long>()

		repeat(requestCycles) { cycle ->
			val cycleTime = measureTimeMillis {
				// Create view that would initiate network request
				val view = PayPalMessageView(context)
				view.setConfig(
					testConfig.clone().apply {
						data.clientID = "memory-leak-test-$cycle"
						data.amount = (cycle * 25).toDouble() + 0.99
					},
				)

				// Brief delay for potential request initiation
				delay(50)

				// Immediate cleanup (simulating rapid activity destruction)
				view.setConfig(PayPalMessageConfig(PayPalMessageData(clientID = "")))
			}
			cycleTimes.add(cycleTime)
		}

		val averageCycleTime = cycleTimes.average()
		val firstThirdAverage = cycleTimes.take(requestCycles / 3).average()
		val lastThirdAverage = cycleTimes.takeLast(requestCycles / 3).average()
		val degradationRatio = lastThirdAverage / firstThirdAverage

		println("Network request cycle times: $cycleTimes")
		println("First third average: ${firstThirdAverage}ms")
		println("Last third average: ${lastThirdAverage}ms")
		println("Degradation ratio: $degradationRatio")

		// Performance should remain consistent (no significant degradation)
		assert(degradationRatio < 1.5) {
			"Network request performance degraded by ${(degradationRatio - 1) * 100}%, indicating memory leaks"
		}

		assert(averageCycleTime < 200) {
			"Average network request cycle took ${averageCycleTime}ms, too slow for budget devices"
		}
	}

	/**
	 * Test state persistence across configuration changes with complex data.
	 */
	@Test
	fun testCompleteStatePersistence() {
		// Test that all configuration aspects are preserved during recreation
		val complexConfig = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "state-test-client",
				merchantID = "merchant-123",
				partnerAttributionID = "partner-456",
			).apply {
				amount = 549.99
				buyerCountry = "CA"
				offerType = PayPalMessageOfferType.PAY_LATER_LONG_TERM
				environment = PayPalEnvironment.SANDBOX
			},
			style = PayPalMessageStyle(),
		)

		val originalView = PayPalMessageView(context)
		originalView.setConfig(complexConfig)

		// Save state (as would happen in onSaveInstanceState)
		val savedConfig = originalView.getConfig()

		// Simulate destruction by clearing view reference
		// (in real scenario, view would be destroyed)

		// Recreate view with saved state
		val recreatedView = PayPalMessageView(context)
		val restorationTime = measureTimeMillis {
			recreatedView.setConfig(savedConfig)
		}

		// Verify all state was preserved
		val restoredConfig = recreatedView.getConfig()
		assertEquals("Client ID not preserved", "state-test-client", restoredConfig.data.clientID)
		assertEquals("Merchant ID not preserved", "merchant-123", restoredConfig.data.merchantID)
		assertEquals("Partner ID not preserved", "partner-456", restoredConfig.data.partnerAttributionID)
		assertEquals("Amount not preserved", 549.99, restoredConfig.data.amount ?: 0.0, 0.01)
		assertEquals("Country not preserved", "CA", restoredConfig.data.buyerCountry)
		assertEquals("Offer type not preserved", PayPalMessageOfferType.PAY_LATER_LONG_TERM, restoredConfig.data.offerType)

		// Restoration should be fast
		assert(restorationTime < 100) {
			"Complex state restoration took ${restorationTime}ms, too slow for smooth UX"
		}
	}

	/**
	 * Test extreme memory pressure scenario with multiple components.
	 */
	@Test
	fun testExtremeMemoryPressureWithMultipleComponents() = runBlocking {
		val messageViews = mutableListOf<PayPalMessageView>()
		val fragmentScenarios = mutableListOf<FragmentScenario<ModalFragment>>()

		try {
			// Create multiple message views and modal fragments (simulating complex app)
			val creationTime = measureTimeMillis {
				// Create 5 message views
				repeat(5) { index ->
					val view = PayPalMessageView(context)
					view.setConfig(
						testConfig.clone().apply {
							data.clientID = "pressure-view-$index"
							data.amount = (index * 50).toDouble() + 0.99
						},
					)
					messageViews.add(view)
				}

				// Create 3 modal fragments
				repeat(3) { index ->
					val scenario = launchFragmentInContainer<ModalFragment>(
						fragmentArgs = Bundle().apply {
							putString("client_id", "pressure-modal-$index")
						},
						themeResId = androidx.appcompat.R.style.Theme_AppCompat,
					)
					fragmentScenarios.add(scenario)
				}
			}

			println("Multiple component creation took: ${creationTime}ms")

			// Simulate system onLowMemory() - aggressive cleanup required
			val cleanupTime = measureTimeMillis {
				// Cleanup message views
				messageViews.forEach { view ->
					view.setConfig(PayPalMessageConfig(PayPalMessageData(clientID = "")))
				}

				// Destroy fragments
				fragmentScenarios.forEach { scenario ->
					scenario.moveToState(Lifecycle.State.DESTROYED)
				}
			}

			println("Low memory cleanup took: ${cleanupTime}ms")

			// Cleanup should be very fast even with multiple components
			assert(cleanupTime < 500) {
				"Low memory cleanup took ${cleanupTime}ms, too slow for system onLowMemory()"
			}

			assert(creationTime < 2000) {
				"Multiple component creation took ${creationTime}ms, too slow for budget devices"
			}

			// Test recovery after memory pressure
			delay(100)
			val recoveryView = PayPalMessageView(context)
			recoveryView.setConfig(testConfig)
			assertNotNull("Should recover after memory pressure", recoveryView.getConfig())
		} finally {
			fragmentScenarios.forEach { it.close() }
		}
	}

	/**
	 * Test performance degradation detection over extended lifecycle cycles.
	 */
	@Test
	fun testLongTermPerformanceDegradation() = runBlocking {
		val longTermCycles = 25
		val cycleTimes = mutableListOf<Long>()
		val messageView = PayPalMessageView(context)

		repeat(longTermCycles) { cycle ->
			val cycleTime = measureTimeMillis {
				// Configure
				messageView.setConfig(
					testConfig.clone().apply {
						data.clientID = "long-term-$cycle"
						data.amount = (cycle * 10).toDouble() + 0.99
					},
				)

				// Simulate brief usage
				delay(20)

				// Cleanup (simulating activity destruction)
				messageView.setConfig(PayPalMessageConfig(PayPalMessageData(clientID = "")))
			}
			cycleTimes.add(cycleTime)
		}

		// Analyze performance over time
		val segments = 5
		val segmentSize = longTermCycles / segments
		val segmentAverages = (0 until segments).map { segment ->
			val start = segment * segmentSize
			val end = start + segmentSize
			cycleTimes.subList(start, end).average()
		}

		println("Performance over $longTermCycles cycles ($segments segments):")
		segmentAverages.forEachIndexed { index, average ->
			println("  Segment $index: ${average}ms")
		}

		// Check for linear degradation
		val firstSegmentAverage = segmentAverages.first()
		val lastSegmentAverage = segmentAverages.last()
		val totalDegradationRatio = lastSegmentAverage / firstSegmentAverage

		assert(totalDegradationRatio < 1.8) {
			"Performance degraded by ${(totalDegradationRatio - 1) * 100}% over $longTermCycles cycles"
		}

		// Overall performance should remain acceptable
		val overallAverage = cycleTimes.average()
		assert(overallAverage < 150) {
			"Overall average cycle time ${overallAverage}ms too slow for long-term usage"
		}
	}
}
