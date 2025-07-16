package com.paypal.messages.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity

/**
 * Utility class for finding AppCompatActivity instance from a Context.
 */
object ContextCompatWrapper {
	/**
	 * Attempts to find an AppCompatActivity from a given Context.
	 * This is useful when we need an AppCompatActivity reference but only have a Context.
	 *
	 * @param context The context to search from
	 * @return An AppCompatActivity if found, null otherwise
	 */
	fun findAppCompatActivity(context: Context): AppCompatActivity? {
		var currentContext = context
		var previousContext: Context? = null
		
		// Loop until we find AppCompatActivity or reach end of chain
		// Also protect against cyclic references by checking if context changes
		while (currentContext !is AppCompatActivity && currentContext is ContextWrapper &&
			currentContext != previousContext
		) {
			previousContext = currentContext
			currentContext = currentContext.baseContext
		}
		
		return if (currentContext is AppCompatActivity) {
			currentContext
		} else {
			null
		}
	}
}
