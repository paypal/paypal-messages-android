package com.paypal.messages.io

/**
 * This interface definition is to provide a callback when the [Action] completes
 */
interface OnActionCompleted {
	/**
	 * Called when a paypal message content fetch action has completed.
	 *
	 * @param result - Result of the fetch content action. See [ActionResponse]
	 * for more about the result object.
	 */
	fun onActionCompleted(result: ApiResult)
}
