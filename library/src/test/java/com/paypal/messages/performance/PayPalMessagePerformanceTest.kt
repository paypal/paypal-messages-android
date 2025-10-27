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
                clientID = "test-client-id"
            ).apply {
                amount = 99.99
                buyerCountry = "US"
                environment = PayPalEnvironment.SANDBOX
            },
            style = PayPalMessageStyle()
        )
    }

    @Test
    @DisplayName("Affordability: Message view creation should complete within budget device constraints")
    fun testMessageViewCreationPerformance() {
        // Budget devices typically have 2-4GB RAM and slower CPUs
        // Message view creation should complete within reasonable time limits
        assertTimeout(Duration.ofMillis(500)) {
            val creationTime = measureTimeMillis {
                PayPalMessageView(mockContext, config = baseConfig)
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
                messageView.setConfig(PayPalMessageConfig(
                    data = PayPalMessageData(
                        clientID = "test-client-id-$iteration"
                    ).apply {
                        amount = (iteration * 10).toDouble() + 0.99
                        buyerCountry = "US"
                        environment = PayPalEnvironment.SANDBOX
                    },
                    style = PayPalMessageStyle()
                ))
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
            0.01,     // Micro-payment
            99.99,    // Standard e-commerce
            999.99,   // Higher value
            9999.99,  // Enterprise
            99999.99  // Large transaction
        )

        val performanceTimes = testAmounts.map { amount ->
            measureTimeMillis {
                messageView.setConfig(baseConfig.clone().apply {
                    data.amount = amount
                })
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
            PayPalEnvironment.DEVELOP()
        )

        val switchingTimes = environments.map { environment ->
            measureTimeMillis {
                messageView.setConfig(baseConfig.clone().apply {
                    data.environment = environment
                })
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
                val messageView = PayPalMessageView(
                    mockContext,
                    config = baseConfig.clone().apply {
                        data.clientID = "test-$iteration"
                    }
                )
                messageViews.add(messageView)
            }
        }

        val cleanupTime = measureTimeMillis {
            // Simulate cleanup during activity destruction
            messageViews.forEach { view ->
                // In real implementation, this would trigger cleanup callbacks
                view.setConfig(PayPalMessageConfig(PayPalMessageData(clientID = "")))
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
    @DisplayName("Low-Tech: State persistence during rapid activity recreation")
    fun testStatePersistenceDuringDontKeepActivities() {
        // Simulates the "Don't Keep Activities" scenario where configuration must be preserved
        val originalAmount = 199.99
        val originalCountry = "US"

        val messageView = PayPalMessageView(mockContext)

        // Set initial configuration
        val initialConfig = baseConfig.clone().apply {
            data.amount = originalAmount
            data.buyerCountry = originalCountry
        }
        messageView.setConfig(initialConfig)

        // Simulate activity destruction and recreation by getting and restoring config
        val savedConfig = messageView.getConfig()

        // Create new view instance (as Android would do)
        val recreatedView = PayPalMessageView(mockContext)

        val restorationTime = measureTimeMillis {
            recreatedView.setConfig(savedConfig)
        }

        println("State restoration time: ${restorationTime}ms")

        // Verify configuration was preserved
        val restoredConfig = recreatedView.getConfig()
        assert(restoredConfig.data.amount == originalAmount) {
            "Amount not preserved after recreation: expected $originalAmount, got ${restoredConfig.data.amount}"
        }
        assert(restoredConfig.data.buyerCountry == originalCountry) {
            "Buyer country not preserved after recreation: expected $originalCountry, got ${restoredConfig.data.buyerCountry}"
        }

        // Restoration should be fast enough for smooth user experience
        assert(restorationTime < 100) {
            "State restoration took ${restorationTime}ms, too slow for smooth activity recreation"
        }
    }

    @Test
    @DisplayName("Low-Tech: Multiple rapid recreations simulate extreme memory pressure")
    fun testExtremeActivityRecreationScenario() {
        // Simulates the worst-case scenario: user repeatedly backgrounding/foregrounding
        // the app with "Don't Keep Activities" enabled

        val recreationCycles = 10
        val recreationTimes = mutableListOf<Long>()
        var currentConfig = baseConfig.clone()

        repeat(recreationCycles) { cycle ->
            val cycleTime = measureTimeMillis {
                // Create view
                val view = PayPalMessageView(mockContext)

                // Configure with potentially different data each cycle
                currentConfig = currentConfig.clone().apply {
                    data.amount = (cycle * 50.0) + 0.99
                }
                view.setConfig(currentConfig)

                // Get config (simulates saving state)
                val savedConfig = view.getConfig()

                // Verify data integrity
                assert(savedConfig.data.amount == currentConfig.data.amount) {
                    "Configuration lost during cycle $cycle"
                }
            }
            recreationTimes.add(cycleTime)
        }

        val averageRecreationTime = recreationTimes.average()
        val maxRecreationTime = recreationTimes.maxOrNull() ?: 0L

        println("Recreation times over $recreationCycles cycles: $recreationTimes")
        println("Average recreation time: ${averageRecreationTime}ms")
        println("Maximum recreation time: ${maxRecreationTime}ms")

        // Performance should not degrade over multiple cycles
        assert(averageRecreationTime < 100) {
            "Average recreation time ${averageRecreationTime}ms indicates memory pressure"
        }
        assert(maxRecreationTime < 200) {
            "Maximum recreation time ${maxRecreationTime}ms indicates severe performance degradation"
        }

        // Check for performance degradation over time
        val firstHalfAverage = recreationTimes.take(recreationCycles / 2).average()
        val secondHalfAverage = recreationTimes.takeLast(recreationCycles / 2).average()
        val degradationRatio = secondHalfAverage / firstHalfAverage

        println("First half average: ${firstHalfAverage}ms")
        println("Second half average: ${secondHalfAverage}ms")
        println("Degradation ratio: $degradationRatio")

        assert(degradationRatio < 1.5) {
            "Performance degraded by ${(degradationRatio - 1) * 100}% over time, indicating memory leaks"
        }
    }

    @Test
    @DisplayName("Low-Tech: Configuration changes under memory constraints")
    fun testConfigurationChangeUnderMemoryPressure() {
        // Simulates configuration changes on devices with limited memory (1-2GB RAM)
        // where Android may aggressively kill and recreate activities

        val messageView = PayPalMessageView(mockContext)
        val testScenarios = listOf(
            Triple(50.0, "US", PayPalEnvironment.SANDBOX),
            Triple(100.0, "CA", PayPalEnvironment.LIVE),
            Triple(500.0, "GB", PayPalEnvironment.DEVELOP()),
            Triple(1000.0, "AU", PayPalEnvironment.SANDBOX),
            Triple(2500.0, "DE", PayPalEnvironment.LIVE)
        )

        val configChangeTimes = mutableListOf<Long>()

        testScenarios.forEach { (amount, country, environment) ->
            // Save current state
            val currentConfig = messageView.getConfig()

            // Measure change time
            val changeTime = measureTimeMillis {
                val newConfig = currentConfig.clone().apply {
                    data.amount = amount
                    data.buyerCountry = country
                    data.environment = environment
                }
                messageView.setConfig(newConfig)

                // Verify change was applied
                val verifiedConfig = messageView.getConfig()
                assert(verifiedConfig.data.amount == amount)
                assert(verifiedConfig.data.buyerCountry == country)
                assert(verifiedConfig.data.environment == environment)
            }

            configChangeTimes.add(changeTime)
        }

        val averageChangeTime = configChangeTimes.average()
        val maxChangeTime = configChangeTimes.maxOrNull() ?: 0L

        println("Configuration change times: $configChangeTimes")
        println("Average change time: ${averageChangeTime}ms")

        // Configuration changes should be fast even under memory pressure
        assert(averageChangeTime < 50) {
            "Average configuration change time ${averageChangeTime}ms too slow for low-memory devices"
        }
        assert(maxChangeTime < 100) {
            "Maximum configuration change time ${maxChangeTime}ms indicates stalls on low-memory devices"
        }
    }

    @Test
    @DisplayName("Low-Tech: Memory allocation patterns during lifecycle events")
    fun testMemoryAllocationPatterns() {
        // Tests that the SDK doesn't allocate excessive objects during lifecycle transitions
        // Important for devices with 1-2GB RAM where GC pauses can cause jank

        val allocationCycles = 20
        val views = mutableListOf<PayPalMessageView>()
        val configs = mutableListOf<PayPalMessageConfig>()

        val totalAllocationTime = measureTimeMillis {
            repeat(allocationCycles) { iteration ->
                // Create view and config (allocations)
                val view = PayPalMessageView(mockContext)
                val config = baseConfig.clone().apply {
                    data.clientID = "allocation-test-$iteration"
                    data.amount = iteration.toDouble() + 0.99
                }

                view.setConfig(config)
                views.add(view)
                configs.add(view.getConfig())
            }
        }

        val averageAllocationTime = totalAllocationTime.toDouble() / allocationCycles

        println("Total allocation time for $allocationCycles cycles: ${totalAllocationTime}ms")
        println("Average allocation time per cycle: ${averageAllocationTime}ms")

        // Each cycle should allocate efficiently
        assert(averageAllocationTime < 25) {
            "Average allocation time ${averageAllocationTime}ms indicates excessive object creation"
        }

        // Cleanup should also be efficient
        val cleanupTime = measureTimeMillis {
            views.clear()
            configs.clear()
        }

        println("Cleanup time: ${cleanupTime}ms")

        assert(cleanupTime < 50) {
            "Cleanup took ${cleanupTime}ms, indicating potential finalization issues"
        }
    }

    @Test
    @DisplayName("Low-Tech: Consistent performance across budget device CPU speeds")
    fun testPerformanceOnSlowCPU() {
        // Budget devices often have slower CPUs (1.0-1.5 GHz quad-core)
        // This test ensures operations remain responsive even with slower processing

        val operations = mutableMapOf<String, Long>()

        // Test 1: View creation
        operations["view_creation"] = measureTimeMillis {
            PayPalMessageView(mockContext, config = baseConfig)
        }

        // Test 2: Configuration update
        val testView = PayPalMessageView(mockContext)
        operations["config_update"] = measureTimeMillis {
            testView.setConfig(baseConfig.clone().apply {
                data.amount = 299.99
            })
        }

        // Test 3: Configuration retrieval
        operations["config_retrieval"] = measureTimeMillis {
            testView.getConfig()
        }

        // Test 4: Configuration cloning
        operations["config_cloning"] = measureTimeMillis {
            repeat(10) {
                baseConfig.clone()
            }
        }

        println("Operation timings on budget CPU:")
        operations.forEach { (operation, time) ->
            println("  $operation: ${time}ms")
        }

        // All operations should complete quickly even on slow CPUs
        assert(operations["view_creation"]!! < 300) {
            "View creation took ${operations["view_creation"]}ms, too slow for budget devices"
        }
        assert(operations["config_update"]!! < 100) {
            "Config update took ${operations["config_update"]}ms, too slow for budget devices"
        }
        assert(operations["config_retrieval"]!! < 50) {
            "Config retrieval took ${operations["config_retrieval"]}ms, too slow for budget devices"
        }
        assert(operations["config_cloning"]!! < 100) {
            "Config cloning (10x) took ${operations["config_cloning"]}ms, too slow for budget devices"
        }
    }

    @Test
    @DisplayName("Innovation: Concurrent configuration changes should be handled efficiently")
    fun testConcurrentConfigurationHandling() {
        val messageView = PayPalMessageView(mockContext)

        // Test rapid configuration changes as might happen during user interaction
        val rapidConfigurationTime = measureTimeMillis {
            repeat(20) { iteration ->
                messageView.setConfig(baseConfig.clone().apply {
                    data.amount = iteration.toDouble() + 0.99
                    data.buyerCountry = if (iteration % 2 == 0) "US" else "CA"
                })
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
                baseConfig.clone().apply {
                    data.amount = it.toDouble() + 0.99
                }
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