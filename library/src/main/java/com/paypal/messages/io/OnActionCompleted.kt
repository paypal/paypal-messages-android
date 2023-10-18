package com.paypal.messages.io

import com.paypal.messages.utils.IgnoreGeneratedTestReport

/**
 * This interface definition is to provide a callback when [Api.getMessageWithHash] completes
 */
@IgnoreGeneratedTestReport
interface OnActionCompleted {
	/**
	 * Called when a paypal message content fetch action has completed.
	 *
	 * @param result - Result of the fetch content action. See [ApiMessageData.Response]
	 * for more about the result object.
	 */
	fun onActionCompleted(result: ApiResult)
}
