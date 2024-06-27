package com.paypal.messagesdemo

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.paypal.messages.R
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
public class CheckTest {

	@get:Rule
	val activityScenarioRule = ActivityScenarioRule(XmlActivity::class.java)

	@Test
	fun testTransitionToSecondActivity() {
		// Perform a delay
		onView(isRoot()).perform(waitFor(3000))

		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity

		onView(withId(R.id.content)).check(matches(withText("%paypal_logo% Buy now, pay later. Learn more")))
	}

	// Custom ViewAction to wait
	fun waitFor(millis: Long): ViewAction {
		return object : ViewAction {
			override fun getConstraints(): Matcher<View> {
				return isRoot()
			}

			override fun getDescription(): String {
				return "wait for $millis milliseconds"
			}

			override fun perform(uiController: UiController, view: View) {
				uiController.loopMainThreadForAtLeast(millis)
			}
		}
	}
}
