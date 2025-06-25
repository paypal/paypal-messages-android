package com.paypal.messages.data

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.paypal.messages.ModalFragment
import com.paypal.messages.analytics.AnalyticsEvent
import com.paypal.messages.config.PayPalEnvironment
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.io.Api
import com.paypal.messages.io.ApiMessageData
import com.paypal.messages.io.ApiResult
import com.paypal.messages.io.OnActionCompleted
import com.paypal.messages.utils.PayPalErrors
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class PayPalMessageDataProviderTest {
	private lateinit var provider: PayPalMessageDataProvider
	private lateinit var mockContext: Context
	private lateinit var mockCallback: PayPalMessageDataCallback
	private lateinit var config: PayPalMessageConfig
	private val instanceId = UUID.randomUUID()
	private val testDispatcher = StandardTestDispatcher()

	@Before
	fun setup() {
		Dispatchers.setMain(testDispatcher)
		provider = PayPalMessageDataProvider()
		mockContext = mockk(relaxed = true)
		mockCallback = mockk(relaxed = true)
		config = PayPalMessageConfig(
			data = PayPalMessageData(
				clientID = "test-client-id",
				environment = PayPalEnvironment.SANDBOX,
			),
		)

		// Mock API static method
		mockkObject(Api)
	}

	@After
	fun tearDown() {
		Dispatchers.resetMain()
		unmockkAll()
	}

	@Test
	fun `fetchMessageData calls onLoading immediately`() {
		// Given
		val actionSlot = slot<OnActionCompleted>()
		every {
			Api.getMessageWithHash(any(), any(), any(), capture(actionSlot))
		} returns Unit

		// When
		provider.fetchMessageData(mockContext, config, instanceId, mockCallback)

		// Then
		verify(exactly = 1) { mockCallback.onLoading() }
	}

	@Test
	fun `fetchMessageData handles success response`() {
		// Given
		val mockResponse = mockk<ApiMessageData.Response>()
		val actionSlot = slot<OnActionCompleted>()

		every {
			Api.getMessageWithHash(any(), any(), any(), capture(actionSlot))
		} answers {
			actionSlot.captured.onActionCompleted(ApiResult.Success(mockResponse))
		}

		// When
		provider.fetchMessageData(mockContext, config, instanceId, mockCallback)

		// Then
		verify(exactly = 1) { mockCallback.onLoading() }
		verify(exactly = 1) { mockCallback.onSuccess(mockResponse, any()) }
		verify(exactly = 0) { mockCallback.onError(any()) }
	}

	@Test
	fun `fetchMessageData handles error response`() {
		// Given
		val error = PayPalErrors.InvalidClientIdException()
		val actionSlot = slot<OnActionCompleted>()

		every {
			Api.getMessageWithHash(any(), any(), any(), capture(actionSlot))
		} answers {
			actionSlot.captured.onActionCompleted(ApiResult.Failure(error))
		}

		// When
		provider.fetchMessageData(mockContext, config, instanceId, mockCallback)

		// Then
		verify(exactly = 1) { mockCallback.onLoading() }
		verify(exactly = 0) { mockCallback.onSuccess(any(), any()) }
		verify(exactly = 1) { mockCallback.onError(error) }
	}

	@Test
	fun `fetchMessageData passes correct parameters to API`() {
		// Given
		val contextSlot = slot<Context>()
		val configSlot = slot<PayPalMessageConfig>()
		val instanceIdSlot = slot<UUID>()

		every {
			Api.getMessageWithHash(
				capture(contextSlot),
				capture(configSlot),
				capture(instanceIdSlot),
				any(),
			)
		} returns Unit

		// When
		provider.fetchMessageData(mockContext, config, instanceId, mockCallback)

		// Then
		assertEquals(mockContext, contextSlot.captured)
		assertEquals(config, configSlot.captured)
		assertEquals(instanceId, instanceIdSlot.captured)
	}

	@Test
	fun `createClickHandler returns a functioning click handler`() {
		// Given
		val mockResponse = mockk<ApiMessageData.Response>(relaxed = true)
		val mockOnClick = mockk<() -> Unit>(relaxed = true)
		val mockOnApply = mockk<() -> Unit>(relaxed = true)
		val mockOnError = mockk<(PayPalErrors.Base) -> Unit>(relaxed = true)
		val logEventSlot = slot<AnalyticsEvent>()
		val logEventCallback = mockk<(AnalyticsEvent) -> Unit> {
			every { this@mockk.invoke(capture(logEventSlot)) } returns Unit
		}

		// Mock the AppCompatActivity cast
		every { mockContext as? AppCompatActivity } returns mockk(relaxed = true)

		// Mock ModalFragment constructor and methods
		mockkConstructor(ModalFragment::class)
		every { anyConstructed<ModalFragment>().init(any()) } returns Unit
		every { anyConstructed<ModalFragment>().show(any(), any()) } returns Unit
		every { anyConstructed<ModalFragment>().expand() } returns Unit

		// When
		val clickHandler = provider.createClickHandler(
			mockContext,
			config,
			instanceId,
			logEventCallback,
		)
		clickHandler.onMessageClick(mockResponse, mockOnClick, mockOnApply, mockOnError)

		// Then
		verify { mockOnClick.invoke() }
		verify { logEventCallback.invoke(any()) }
		verify { anyConstructed<ModalFragment>().show(any(), any()) }

		// Test cleanup
		clickHandler.onCleanup()
		verify { anyConstructed<ModalFragment>().dismiss() }
	}
}
