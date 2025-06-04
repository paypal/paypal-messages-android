package com.paypal.messages.compose

import com.paypal.messages.io.ApiMessageData
import com.paypal.messages.utils.PayPalErrors

/**
 * Represents the state of a PayPal Message in Jetpack Compose.
 *
 * This sealed class provides type-safe state management for PayPal Messages:
 * - [Loading]: Initial state when fetching data
 * - [Success]: Data was successfully fetched
 * - [Error]: An error occurred during data fetch
 *
 * Example usage:
 * ```
 * when (state) {
 *     PayPalMessageState.Loading -> ShowLoadingIndicator()
 *     is PayPalMessageState.Success -> ShowMessage(state.response)
 *     is PayPalMessageState.Error -> ShowError(state.error)
 * }
 * ```
 */
sealed class PayPalMessageState {
	object Loading : PayPalMessageState()
	data class Success(
		val response: ApiMessageData.Response,
		val requestDuration: Int,
	) : PayPalMessageState()
	data class Error(val error: PayPalErrors.Base) : PayPalMessageState()
}
