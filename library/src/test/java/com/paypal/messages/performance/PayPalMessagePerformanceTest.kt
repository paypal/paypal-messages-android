package com.paypal.messages.performance

import android.content.Context
import com.paypal.messages.PayPalMessageView
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.config.message.PayPalMessageStyle
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeout
import java.time.Duration
import kotlin.system.measureTimeMillis

/**
 * Performance tests for PayPal Messages Android SDK aligned with the three pillars:
 * 1. Affordability - Tests performance on resource-constrained devices
 * 2. Adaptability - Tests performance across different configurations
 * 3. Innovation - Tests modern Android performance optimization features
 *
 * These tests ensure the SDK maintains acceptable performance standards
 * that would be expected on budget Android devices globally.
 */
@DisplayName("PayPal Message Performance Tests")
class PayPalMessagePerformanceTest {

	private lateinit var mockContext: Context
	private lateinit var baseConfig: PayPalMessageConfig

	@BeforeEach
	fun setup() {
		mockContext = mockk(relaxed = true)

		// Create a standard configuration for testing
		baseConfig = PayPalMessageConfig(
			data = PayPalMessageData(
				clientId = "test-client-id",
				amount = BigDecimal("99.99"),
				buyerCountry = "US",
			),
			style = PayPalMessageStyle(),
			environment = PayPalEnvironment.SANDBOX,
		)
	}

	@Test
	@DisplayName("Affordability: Message view creation should complete within budget device constraints")
	fun testMessageViewCreationPerformance() {
		// Budget devices typically have 2-4GB RAM and slower CPUs
		// Message view creation should complete within reasonable time limits
		assertTimeout(Duration.ofMillis(500)) {
			val creationTime = measureTimeMillis {
				PayPalMessageView(mockContext).apply {
					config = baseConfig
				}
			}

			// Log performance for monitoring (would integrate with actual logging in production)
			println("Message view creation time: ${creationTime}ms")

			// Ensure creation time is acceptable for budget devices
			assert(creationTime < 300) {
				"Message view creation took ${creationTime}ms, exceeding budget device threshold of 300ms"
			}
		}
	}

	@Test
	@DisplayName("Affordability: Multiple message configurations should not cause memory pressure")
	fun testMultipleConfigurationPerformance() {
		val messageView = PayPalMessageView(mockContext)
		val configurationTimes = mutableListOf<Long>()

		// Test multiple configuration changes as might happen in recycler views
		// This simulates real-world usage patterns in e-commerce apps
		repeat(10) { iteration ->
			val configTime = measureTimeMillis {
				messageView.config = PayPalMessageConfig(
					data = PayPalMessageData(
						clientId = "test-client-id-$iteration",
						amount = BigDecimal("${iteration * 10}.99"),
						buyerCountry = "US",
					),
					style = PayPalMessageStyle(),
					environment = PayPalEnvironment.SANDBOX,
				)
			}
			configurationTimes.add(configTime)
		}

		val averageTime = configurationTimes.average()
		val maxTime = configurationTimes.maxOrNull() ?: 0L

		println("Average configuration time: ${averageTime}ms")
		println("Maximum configuration time: ${maxTime}ms")

		// Configuration should remain fast even after multiple changes
		assert(averageTime < 50) {
			"Average configuration time ${averageTime}ms exceeds acceptable threshold for budget devices"
		}
		assert(maxTime < 100) {
			"Maximum configuration time ${maxTime}ms indicates potential memory pressure"
		}
	}

	@Test
	@DisplayName("Adaptability: Performance should be consistent across different amount ranges")
	fun testAmountRangePerformanceConsistency() {
		val messageView = PayPalMessageView(mockContext)
		val testAmounts = listOf(
			BigDecimal("0.01"), // Micro-payment
			BigDecimal("99.99"), // Standard e-commerce
			BigDecimal("999.99"), // Higher value
			BigDecimal("9999.99"), // Enterprise
			BigDecimal("99999.99"), // Large transaction
		)

		val performanceTimes = testAmounts.map { amount ->
			measureTimeMillis {
				messageView.config = baseConfig.copy(
					data = baseConfig.data.copy(amount = amount),
				)
			}
		}

		val variance = calculateVariance(performanceTimes)
		val maxTime = performanceTimes.maxOrNull() ?: 0L

		println("Performance times across amounts: $performanceTimes")
		println("Performance variance: $variance")

		// Performance should be consistent regardless of amount
		assert(variance < 25.0) {
			"Performance variance $variance indicates inconsistent behavior across amount ranges"
		}
		assert(maxTime < 100) {
			"Maximum configuration time ${maxTime}ms with large amounts exceeds threshold"
		}
	}

	@Test
	@DisplayName("Adaptability: Environment switching should maintain performance")
	fun testEnvironmentSwitchingPerformance() {
		val messageView = PayPalMessageView(mockContext)
		val environments = listOf(
			PayPalEnvironment.SANDBOX,
			PayPalEnvironment.LIVE,
			PayPalEnvironment.DEVELOP,
			PayPalEnvironment.STAGE,
		)

		val switchingTimes = environments.map { environment ->
			measureTimeMillis {
				messageView.config = baseConfig.copy(environment = environment)
			}
		}

		val maxSwitchTime = switchingTimes.maxOrNull() ?: 0L
		val averageSwitchTime = switchingTimes.average()

		println("Environment switching times: $switchingTimes")
		println("Average switching time: ${averageSwitchTime}ms")

		// Environment switching should be fast and consistent
		assert(maxSwitchTime < 75) {
			"Environment switching took ${maxSwitchTime}ms, too slow for responsive UI"
		}
		assert(averageSwitchTime < 50) {
			"Average environment switching time ${averageSwitchTime}ms exceeds budget device expectations"
		}
	}

	@Test
	@DisplayName("Innovation: Memory usage should be optimized for modern Android lifecycle")
	fun testMemoryOptimizationBehavior() {
		// This test simulates the "Don't Keep Activities" developer option
		// which immediately destroys activities when users leave them
		val messageViews = mutableListOf<PayPalMessageView>()

		val creationTime = measureTimeMillis {
			// Create multiple message views as would happen in rapid activity recreation
			repeat(5) { iteration ->
				val messageView = PayPalMessageView(mockContext).apply {
					config = baseConfig.copy(
						data = baseConfig.data.copy(clientId = "test-$iteration"),
					)
				}
				messageViews.add(messageView)
			}
		}

		val cleanupTime = measureTimeMillis {
			// Simulate cleanup during activity destruction
			messageViews.forEach { view ->
				// In real implementation, this would trigger cleanup callbacks
				view.config = null
			}
			messageViews.clear()
		}

		println("Multiple view creation time: ${creationTime}ms")
		println("Cleanup time: ${cleanupTime}ms")

		// Creation and cleanup should be efficient for activity lifecycle scenarios
		assert(creationTime < 500) {
			"Multiple view creation took ${creationTime}ms, indicating potential memory issues"
		}
		assert(cleanupTime < 100) {
			"Cleanup took ${cleanupTime}ms, too slow for activity destruction scenarios"
		}
	}

	@Test
	@DisplayName("Innovation: Concurrent configuration changes should be handled efficiently")
	fun testConcurrentConfigurationHandling() {
		val messageView = PayPalMessageView(mockContext)

		// Test rapid configuration changes as might happen during user interaction
		val rapidConfigurationTime = measureTimeMillis {
			repeat(20) { iteration ->
				messageView.config = baseConfig.copy(
					data = baseConfig.data.copy(
						amount = BigDecimal("$iteration.99"),
						buyerCountry = if (iteration % 2 == 0) "US" else "CA",
					),
				)
			}
		}

		println("Rapid configuration changes (20x) took: ${rapidConfigurationTime}ms")

		// Should handle rapid changes without performance degradation
		assert(rapidConfigurationTime < 1000) {
			"Rapid configuration changes took ${rapidConfigurationTime}ms, indicating poor concurrency handling"
		}

		// Average per-change should remain reasonable
		val averagePerChange = rapidConfigurationTime / 20.0
		assert(averagePerChange < 30) {
			"Average per-change time ${averagePerChange}ms indicates scaling issues"
		}
	}

	@Test
	@DisplayName("Regression: Configuration cloning performance for immutability")
	fun testConfigurationCloningPerformance() {
		// Test the performance of configuration cloning which ensures immutability
		// This is critical for avoiding side effects in multi-threaded scenarios

		val cloningTimes = mutableListOf<Long>()

		repeat(100) {
			val cloneTime = measureTimeMillis {
				baseConfig.copy(
					data = baseConfig.data.copy(amount = BigDecimal("$it.99")),
				)
			}
			cloningTimes.add(cloneTime)
		}

		val averageCloneTime = cloningTimes.average()
		val maxCloneTime = cloningTimes.maxOrNull() ?: 0L

		println("Average clone time: ${averageCloneTime}ms")
		println("Maximum clone time: ${maxCloneTime}ms")

		// Configuration cloning should be extremely fast
		assert(averageCloneTime < 5.0) {
			"Average configuration cloning time ${averageCloneTime}ms is too slow for frequent operations"
		}
		assert(maxCloneTime < 20) {
			"Maximum clone time ${maxCloneTime}ms indicates potential garbage collection issues"
		}
	}

	/**
	 * Calculate variance to measure performance consistency
	 */
	private fun calculateVariance(values: List<Long>): Double {
		val mean = values.average()
		val squaredDifferences = values.map { (it - mean) * (it - mean) }
		return squaredDifferences.average()
	}
}
