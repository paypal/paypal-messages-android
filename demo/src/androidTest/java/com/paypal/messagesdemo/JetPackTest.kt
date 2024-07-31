package com.paypal.messagesdemo

import android.view.Gravity
import androidx.core.content.ContextCompat
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.paypal.messages.R
import com.paypal.messages.config.message.style.PayPalMessageColor
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.paypal.messagesdemo.R as Demo

@RunWith(AndroidJUnit4ClassRunner::class)
public class JetPackTest {
	var expectedColor: Int? = null

	@get:Rule
	val activityScenarioRule = ActivityScenarioRule(JetpackActivity::class.java)

	fun submit() {
		onView(withId(Demo.id.submit)).perform(scrollTo())
		onView(withId(Demo.id.submit)).perform(click())
		onView(isRoot()).perform(waitFor(500))
	}

	@Test
	fun testGenericBuyNowPayLaterMessage() {
		// Perform a delay
		onView(isRoot()).perform(waitFor(500))

		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
		checkMessage("%paypal_logo% Buy now, pay later. Learn more")
		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(Gravity.LEFT)))

		// Get the actual color value from the resource ID
		activityScenarioRule.scenario.onActivity { activity ->
			expectedColor = ContextCompat.getColor(activity, PayPalMessageColor.BLACK.colorResId)
		}

		// Use the custom matcher to check the text color of the TextView
		onView(withId(R.id.content))
			.check(matches(ColorMatcher.withTextColor(expectedColor!!)))
	}
}
