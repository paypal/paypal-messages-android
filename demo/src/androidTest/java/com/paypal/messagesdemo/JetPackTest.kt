package com.paypal.messagesdemo

import android.content.Intent
import android.view.Gravity
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
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

	@Rule
	@JvmField
	val activityScenarioRule = ActivityScenarioRule<JetpackActivity>(
		Intent(ApplicationProvider.getApplicationContext(), JetpackActivity::class.java).apply {
			putExtra("TEST_ENV", "LIVE")
		},
	)

	fun submit() {
		onView(withId(Demo.id.submit)).perform(scrollTo())
		onView(withId(Demo.id.submit)).perform(click())
		waitForApp(500)
	}

	@Test
	fun testGenericMessage() {
		// Perform a delay
		waitForApp(1000)

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

	@Test
	fun testGenericMessageAndModal() {
		waitForApp(2000)

		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
		checkMessage("%paypal_logo% Buy now, pay later. Learn more")
		clickMessage()

		onView(withId(R.id.ModalWebView)).check(
			matches(ViewMatchers.isDisplayed()),
		)

		modalContent("Pay Later options")
		waitForApp(2000)
	}
}
