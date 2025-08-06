package com.paypal.messages.utils

import androidx.fragment.app.DialogFragment

/**
 * Helper class to safely dismiss fragments, preventing IllegalStateExceptions.
 *
 * This class provides static methods to safely dismiss fragments when they might not
 * be attached to a fragment manager, which can happen during configuration changes,
 * activity recreation, or rapid navigation between activities.
 */
internal object FragmentDismissHelper {

	/**
	 * Safely dismisses any DialogFragment, handling the case where it's not attached
	 * to a fragment manager.
	 *
	 * @param fragment The DialogFragment to dismiss
	 * @return true if dismiss was attempted, false if fragment was null or error occurred
	 */
	fun safelyDismiss(fragment: DialogFragment?): Boolean {
		if (fragment == null) return false

		return try {
			// Check if fragment is in a state where it can be dismissed
			if (fragment.isAdded && !fragment.isDetached && fragment.activity != null) {
				// Safe to dismiss normally
				fragment.dismiss()
				true
			} else {
				// Fragment isn't attached properly, log this case
				LogCat.debug("FragmentDismissHelper", "DialogFragment not attached, skipping dismiss")
				false
			}
		} catch (e: Exception) {
			// Catch any exceptions during dismiss to prevent app crashes
			LogCat.error("FragmentDismissHelper", "Error dismissing DialogFragment: ${e.message}")
			false
		}
	}
}
