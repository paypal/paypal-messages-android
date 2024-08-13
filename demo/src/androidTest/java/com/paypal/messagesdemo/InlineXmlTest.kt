package com.paypal.messagesdemo

import android.view.Gravity
import androidx.core.content.ContextCompat
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.paypal.messages.R
import com.paypal.messages.config.message.style.PayPalMessageColor
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
public class InlineXmlTest {
	var expectedColor: Int? = null

	@get:Rule
	val activityScenarioRule = ActivityScenarioRule(BasicXmlActivity::class.java)

	@Test
	fun testGenericMessage() {
		// Perform a delay
		waitForApp(2000)

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
	fun testGenericModalCloseWithBackButton() {
		waitForApp(500)
		checkMessage("%paypal_logo% Buy now, pay later. Learn more")

		clickMessage()
		waitForApp(2000)
		modalContent("Get more info")

		Espresso.pressBack()
		waitForApp(5000)
		checkMessage("%paypal_logo% Buy now, pay later. Learn more")
		clickMessage()
		waitForApp(5000)
		modalContent("Get more info")
		waitForApp(2000)
	}
}
