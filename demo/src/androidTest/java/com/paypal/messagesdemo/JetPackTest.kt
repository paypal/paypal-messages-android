package com.paypal.messagesdemo

// @RunWith(AndroidJUnit4ClassRunner::class)
// public class JetPackTest {
// 	var expectedColor: Int? = null
//
// 	@Rule
// 	@JvmField
// 	val activityScenarioRule = ActivityScenarioRule<JetpackActivity>(
// 		Intent(ApplicationProvider.getApplicationContext(), JetpackActivity::class.java).apply {
// 			putExtra("TEST_ENV", "LIVE")
// 		},
// 	)
//
// 	fun submit() {
// 		onView(withId(Demo.id.submit)).perform(scrollTo())
// 		onView(withId(Demo.id.submit)).perform(click())
// 		waitForApp(500)
// 	}
//
// 	@Test
// 	fun testGenericMessage() {
// 		// Perform a delay
// 		waitForApp(1000)
//
// 		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
// 		checkMessage("%paypal_logo% Buy now, pay later. Learn more")
// 		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(Gravity.LEFT)))
//
// 		// Get the actual color value from the resource ID
// 		activityScenarioRule.scenario.onActivity { activity ->
// 			expectedColor = ContextCompat.getColor(activity, PayPalMessageColor.BLACK.colorResId)
// 		}
//
// 		// Use the custom matcher to check the text color of the TextView
// 		onView(withId(R.id.content))
// 			.check(matches(ColorMatcher.withTextColor(expectedColor!!)))
// 	}
//
// 	@Test
// 	fun testGenericMessageAndModal() {
// 		waitForApp(2000)
//
// 		// Check if SecondActivity is displayed by verifying a TextView in SecondActivity
// 		checkMessage("%paypal_logo% Buy now, pay later. Learn more")
// 		clickMessage()
//
// 		onView(withId(R.id.ModalWebView)).check(
// 			matches(ViewMatchers.isDisplayed()),
// 		)
//
// 		modalContent("Pay Later options")
// 		waitForApp(2000)
// 	}
// }
