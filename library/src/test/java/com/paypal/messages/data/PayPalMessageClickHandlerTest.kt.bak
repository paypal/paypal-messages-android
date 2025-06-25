package com.paypal.messages.data

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.paypal.messages.ModalFragment
import com.paypal.messages.config.message.PayPalMessageConfig
import com.paypal.messages.config.message.PayPalMessageData
import com.paypal.messages.io.ApiMessageData
import com.paypal.messages.utils.PayPalErrors
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.util.UUID

class PayPalMessageClickHandlerTest {
	private lateinit var provider: PayPalMessageDataProvider
	private lateinit var mockAppCompatContext: AppCompatActivity
	private lateinit var mockComposeContext: ComponentActivity
	private lateinit var mockResponse: ApiMessageData.Response
	private lateinit var config: PayPalMessageConfig
	private val instanceId = UUID.randomUUID()

	@Before
	fun setUp() {
		provider = PayPalMessageDataProvider()

		// Setup mock contexts
		mockAppCompatContext = mockk(relaxed = true)
		mockComposeContext = mockk(relaxed = true)

		// Setup fragment manager mock for AppCompatActivity
		val mockFragmentManager = mockk<FragmentManager>(relaxed = true)
		every { mockAppCompatContext.supportFragmentManager } returns mockFragmentManager

		// Setup config
		config = PayPalMessageConfig(
			data = PayPalMessageData(clientID = "test-client-id"),
		)

		// Setup response
		val mockMeta = mockk<ApiMessageData.Meta>(relaxed = true)
		mockResponse = mockk(relaxed = true)
		every { mockResponse.meta } returns mockMeta
	}

	@Test
	fun `createClickHandler returns valid handler`() {
		// When
		val clickHandler = provider.createClickHandler(
			mockAppCompatContext,
			config,
			instanceId,
		)

		// Then
		assertNotNull(clickHandler)
	}

	@Test
	fun `click handler shows modal for AppCompatActivity`() {
		// Given
		mockkConstructor(ModalFragment::class)
		val clickHandler = provider.createClickHandler(
			mockAppCompatContext,
			config,
			instanceId,
		)
		val onClickMock = mockk<() -> Unit>(relaxed = true)
		val onApplyMock = mockk<() -> Unit>(relaxed = true)
		val onErrorMock = mockk<(PayPalErrors.Base) -> Unit>(relaxed = true)

		// When
		clickHandler.onMessageClick(
			mockResponse,
			onClickMock,
			onApplyMock,
			onErrorMock,
		)

		// Then
		verify { onClickMock.invoke() }
		verify {
			mockAppCompatContext.supportFragmentManager.let { any<ModalFragment>().show(it, any()) }
		}
	}

	@Test
	fun `click handler starts activity for ComponentActivity`() {
		// Given
		val intentSlot = slot<Intent>()
		every { mockComposeContext.startActivity(capture(intentSlot)) } returns Unit

		val clickHandler = provider.createClickHandler(
			mockComposeContext,
			config,
			instanceId,
		)
		val onClickMock = mockk<() -> Unit>(relaxed = true)
		val onApplyMock = mockk<() -> Unit>(relaxed = true)
		val onErrorMock = mockk<(PayPalErrors.Base) -> Unit>(relaxed = true)

		// When
		clickHandler.onMessageClick(
			mockResponse,
			onClickMock,
			onApplyMock,
			onErrorMock,
		)

		// Then
		verify { onClickMock.invoke() }
		verify { mockComposeContext.startActivity(any()) }
		assert(intentSlot.captured.component?.className?.contains("PayPalModalActivity") ?: false) {
			"Intent should start PayPalModalActivity but was ${intentSlot.captured.component}"
		}
	}

	@Test
	fun `click handler invokes error for unsupported context`() {
		// Given
		val mockContext = mockk<Context>(relaxed = true)
		val clickHandler = provider.createClickHandler(
			mockContext,
			config,
			instanceId,
		)
		val onClickMock = mockk<() -> Unit>(relaxed = true)
		val onApplyMock = mockk<() -> Unit>(relaxed = true)
		val onErrorMock = mockk<(PayPalErrors.Base) -> Unit>(relaxed = true)

		// When
		clickHandler.onMessageClick(
			mockResponse,
			onClickMock,
			onApplyMock,
			onErrorMock,
		)

		// Then
		verify { onClickMock.invoke() }
		verify { onErrorMock.invoke(any<PayPalErrors.UnsupportedContextException>()) }
	}

	@Test
	fun `cleanup dismisses modal`() {
		// Given
		mockkConstructor(ModalFragment::class)
		val clickHandler = provider.createClickHandler(
			mockAppCompatContext,
			config,
			instanceId,
		)

		// When
		clickHandler.onMessageClick(
			mockResponse,
			{},
			{},
			{},
		)
		clickHandler.onCleanup()

		// Then
		verify { any<ModalFragment>().dismiss() }
	}

	@Test
	fun `click handler handles ContextWrapper containing AppCompatActivity`() {
		// Given
		mockkConstructor(ModalFragment::class)
		val mockAppCompatContextWrapper = mockk<ContextWrapper>(relaxed = true)
		every { mockAppCompatContextWrapper.baseContext } returns mockAppCompatContext

		val clickHandler = provider.createClickHandler(
			mockAppCompatContextWrapper,
			config,
			instanceId,
		)
		val onClickMock = mockk<() -> Unit>(relaxed = true)
		val onApplyMock = mockk<() -> Unit>(relaxed = true)
		val onErrorMock = mockk<(PayPalErrors.Base) -> Unit>(relaxed = true)

		// When
		clickHandler.onMessageClick(
			mockResponse,
			onClickMock,
			onApplyMock,
			onErrorMock,
		)

		// Then
		verify { onClickMock.invoke() }
		verify { mockAppCompatContext.supportFragmentManager }
		verify(exactly = 0) { onErrorMock.invoke(any()) }
	}
}
