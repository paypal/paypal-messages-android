package com.paypal.messagesdemo

import android.content.Intent
import android.view.Gravity
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.paypal.messages.R
import com.paypal.messages.config.message.style.PayPalMessageAlignment
import com.paypal.messages.config.message.style.PayPalMessageColor
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.paypal.messagesdemo.R as Demo

@RunWith(AndroidJUnit4ClassRunner::class)
abstract class XmlDemoSetup {

	var expectedColor: Int? = null

	@Rule
	@JvmField
	val activityScenarioRule = ActivityScenarioRule<XmlActivity>(
		Intent(ApplicationProvider.getApplicationContext(), XmlActivity::class.java).apply {
			putExtra("TEST_ENV", "LIVE")
		},
	)
}

@RunWith(AndroidJUnit4ClassRunner::class)
public class XmlDemoGenericTest : XmlDemoSetup() {

	@Test
	fun testGenericMessage() {
		// Perform a delay

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
	}

	@Test
	fun testGenericModalNavigatingTiles() {
		waitForApp(2000)

		clickMessage()

		onView(withId(R.id.ModalWebView)).check(
			matches(ViewMatchers.isDisplayed()),
		)

		closeButtonPresent()
		checkPi4TilePresent()
		checkPayMonthlyTilePresent()
		checkNiTilePresent()

		clickNiTile()
		checkNIContent()
		clickSeeOtherModalOptions()

		clickPi4Tile()
		checkPi4ModalContent()
		clickSeeOtherModalOptions()

		clickPayMonthlyTile()
		checkPayMonthlyContent()
		clickSeeOtherModalOptions()

		closeModal()
	}

	@Test
	fun testGenericModalClose() {
		waitForApp(5000)

		clickMessage()

		onView(withId(R.id.ModalWebView)).check(
			matches(ViewMatchers.isDisplayed()),
		)

		closeModal()
	}

	@Test
	fun testGenericModalCloseAndOpenSameMessage() {
		waitForApp(2000)

		clickMessage()
		modalContent("Pay Later")

		clickPi4Tile()
		checkPi4ModalContent()
		closeModal()
		waitForApp(500)

		clickMessage()
		waitForApp(500)
		checkPi4ModalContent()
	}
}

@RunWith(AndroidJUnit4ClassRunner::class)
public class XmlDemoShortTermTest : XmlDemoSetup() {

	@Test
	fun testShorTermMessage() {
		waitForApp(500)
		clickShortTermOffer()
		submit()
		waitForApp(500)
	}

	@Test
	fun testShortTermNonQualifyingMessage() {
		waitForApp(500)
		clickShortTermOffer()

		typeAmount("15")
		submit()
		waitForApp(500)
		onView(withId(R.id.content)).check(matches(withText(containsString("payments on purchases of "))))
		waitForApp(500)
		clearAmount()
		typeAmount("2000")
		submit()
		onView(withId(R.id.content)).check(matches(withText(containsString("payments on purchases of "))))

		waitForApp(500)
	}

	@Test
	fun testShortTermQualifyingMessage() {
		waitForApp(500)
		clickShortTermOffer()

		typeAmount("1000")
		submit()
		waitForApp(500)
		onView(withId(R.id.content)).check(matches(withText(containsString("250"))))
// 		checkMessage("250")
// 		waitForApp(5000)
	}

	@Test
	fun testShortTermMessageAndModal() {
		waitForApp(500)
		clickShortTermOffer()
		submit()
		waitForApp(500)
		clickMessage()
		checkPi4ModalContent()
		closeModal()
	}

	@Test
	fun testShortTermOpenAndSwitchModal() {
		waitForApp(500)
		clickShortTermOffer()
		submit()
		waitForApp(500)
		clickMessage()
		checkPi4ModalContent()
		clickSeeOtherModalOptions()
		waitForApp(200)
		clickPi4Tile()
		checkPi4ModalContent()
	}

	@Test
	fun testShortTermQualifyingModal() {
		waitForApp(500)
		clickShortTermOffer()
		typeAmount("1000")
		submit()
		waitForApp(500)
		clickMessage()
		checkPi4ModalContent()
	}
}

@RunWith(AndroidJUnit4ClassRunner::class)
public class XmlDemoLongTermTest : XmlDemoSetup() {

	@Test
	fun testLongTermNonQualifyingMessage() {
		waitForApp(500)
		clickLongTermOffer()
		typeAmount("15")
		submit()
		waitForApp(500)
		checkMessage("%paypal_logo% Pay monthly")
		clearAmount()
		typeAmount("20000")
		submit()
		checkMessage("%paypal_logo% Pay monthly")
	}

	@Test
	fun testLongTermQualifyingMessage() {
		waitForApp(500)
		clickLongTermOffer()
		typeAmount("1000")
		submit()
		waitForApp(2000)
		checkMessage("$95")
	}

	@Test
	fun testLongTermNoAmountMessage() {
		waitForApp(500)
		clickLongTermOffer()
		submit()
		waitForApp(500)
		clickMessage()
		checkPayMonthlyContent()
		modalContent("Enter an amount of")
	}

	@Test
	fun testLongTermQualifyingAmountMessage() {
		waitForApp(500)
		clickLongTermOffer()
		typeAmount("1000")
		submit()
		waitForApp(500)
		clickMessage()
		checkPayMonthlyContent()
		modalContent("12 months")
	}

	@Test
	fun testLongTermRangeMessage() {
		waitForApp(500)
		clickLongTermOffer()
		submit()
		waitForApp(500)
		clickMessage()
		checkPayMonthlyContent()

		typeCalculatorAmount("1000")
		waitForApp(5000)
		modalContent("for 12")

		clearCalculatorAmount()
		waitForApp(500)
		typeCalculatorAmount("100")
		waitForApp(5000)
		modalContent("Enter an amount")

		clearCalculatorAmount()
		typeCalculatorAmount("20000")
		waitForApp(5000)
		modalContent("Enter an amount no larger")
		waitForApp(500)
	}

	@Test
	fun testLongTermModalNavigation() {
		waitForApp(500)
		clickLongTermOffer()
		submit()
		waitForApp(500)
		clickMessage()
		checkPayMonthlyContent()

		clickSeeOtherModalOptions()
		clickPayMonthlyTile()
		checkPayMonthlyContent()
	}

	@Test
	fun testLongTermModalDisclosure() {
		waitForApp(500)
		clickLongTermOffer()
		submit()
		waitForApp(2000)
		clickMessage()
		checkPayMonthlyContent()
		waitForApp(2000)

		modalContent("Find more disclosures")

// 		Iframe clicks not working
// 		clickDisclosure()
// 		Espresso.pressBack()
// 		waitForApp(500)
// 		checkPayMonthlyContent()
	}
}

@RunWith(AndroidJUnit4ClassRunner::class)
public class XmlDemoNiTest : XmlDemoSetup() {

	@Test
	fun testNiMessage() {
		waitForApp(500)
		clickNIOffer()
		submit()
		waitForApp(1000)
		checkMessage("No Interest")
	}

	@Test
	fun testNonQualifyingNiMessage() {
		waitForApp(500)
		clickNIOffer()
		typeAmount("15")
		submit()
		waitForApp(1000)
		checkMessage("No Interest")
	}

	@Test
	fun testQualifyingNiMessage() {
		waitForApp(500)
		clickNIOffer()
		typeAmount("100")
		submit()
		waitForApp(1000)
		checkMessage("No Interest")
	}

	@Test
	fun testNiMessageAndModal() {
		waitForApp(500)
		clickNIOffer()
		submit()
		waitForApp(1000)
		checkMessage("%paypal_logo% No Interest")
		clickMessage()
		waitForApp(1000)
		modalContent("Apply now")
	}

	@Test
	fun testNiModalNavigateOtherAndBack() {
		waitForApp(500)
		clickNIOffer()
		submit()
		waitForApp(1000)
		checkMessage("%paypal_logo% No Interest")
		clickMessage()
		waitForApp(1000)
		modalContent("Apply now")
		clickSeeOtherModalOptions()
		clickNiTile()
		modalContent("Apply now")
	}

	@Test
	fun testNiModalApplyNowCloses() {
		waitForApp(500)
		clickNIOffer()
		submit()
		waitForApp(1000)
		checkMessage("%paypal_logo% No Interest")
		clickMessage()
		waitForApp(2000)
		modalContent("Apply now")
// 		Clicking iframe button not working
// 		waitForApp(1000)
// 		clickApplyNow()
// 		Espresso.pressBack()
// 		waitForApp(1000)
// 		modalContent("Apply now")
	}

	@Test
	fun testNiModalTermsCloses() {
		waitForApp(500)
		clickNIOffer()
		submit()
		waitForApp(1000)
		checkMessage("%paypal_logo% No Interest")
		clickMessage()
		waitForApp(1000)
		modalContent("Apply now")
// 		clickApplyNow()
// 		waitForApp(20000)
// 		Espresso.pressBack()
// 		waitForApp(1000)
// 		modalContent("Apply now")
	}
}

@RunWith(AndroidJUnit4ClassRunner::class)
public class XmlDemoStyleOptionsTest : XmlDemoSetup() {

	@Test
	fun testAlignment() {
		waitForApp(500)
		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(Gravity.LEFT)))

		onView(withId(Demo.id.styleCenter)).perform(click())
		submit()
		waitForApp(500)
		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(PayPalMessageAlignment.CENTER.value)))

		onView(withId(Demo.id.styleRight)).perform(click())
		submit()
		waitForApp(500)
		onView(withId(R.id.content)).check(matches(GravityMatcher.withGravity(Gravity.RIGHT)))
	}

	@Test
	fun testMessageColors() {
		waitForApp(500)
		checkMessageColor(activityScenarioRule, PayPalMessageColor.BLACK)

		onView(withId(Demo.id.styleWhite)).perform(click())
		submit()
		waitForApp(500)
		checkMessageColor(activityScenarioRule, PayPalMessageColor.WHITE)

		onView(withId(Demo.id.styleMonochrome)).perform(click())
		submit()
		waitForApp(500)
		checkMessageColor(activityScenarioRule, PayPalMessageColor.MONOCHROME)

		onView(withId(Demo.id.styleGrayscale)).perform(click())
		submit()
		waitForApp(500)
		checkMessageColor(activityScenarioRule, PayPalMessageColor.GRAYSCALE)

		onView(withId(Demo.id.styleBlack)).perform(click())
		submit()
		waitForApp(500)
		checkMessageColor(activityScenarioRule, PayPalMessageColor.BLACK)
	}

	@Test
	fun testLogoAlignment() {
		waitForApp(2000)
		onView(withId(Demo.id.styleInline)).perform(click())
		submit()
		waitForApp(2000)
		checkMessage("Buy now, pay later with %paypal_logo%. Learn more")

		onView(withId(Demo.id.styleAlternative)).perform(click())
		submit()
		waitForApp(2000)
		checkMessage("%paypal_logo% Buy now, pay later. Learn more")

		onView(withId(Demo.id.styleNone)).perform(click())
		submit()
		waitForApp(2000)
		checkMessage("Buy now, pay later with PayPal. Learn more")

		onView(withId(Demo.id.stylePrimary)).perform(click())
		submit()
		waitForApp(2000)
		checkMessage("%paypal_logo% Buy now, pay later. Learn more")
		waitForApp(1000)
	}
}

// TODO - Need to change client id via secrets
// @RunWith(AndroidJUnit4ClassRunner::class)
// public class XmlDemoCrossBorderTest : XmlDemoSetup() {
//
// 	// @Test
// 	// fun testCrossBorder(){
//
// 	// 	}
// }

// @RunWith(AndroidJUnit4ClassRunner::class)
// public class XmlDemoEligibilityTest : XmlDemoSetup() {
//
// 	@Test
// 	fun testStandardConfig() {
// 		waitForApp(500)
//
// 		checkMessage("Buy now")
// 		typeAmount("20")
// 		submit()
// 		waitForApp(1000)
// 		checkMessage("-")
// 		clearAmount()
//
// 		typeAmount("50")
// 		submit()
// 		waitForApp(1000)
// 		checkMessage("12.50")
// 		clearAmount()
//
// 		typeAmount("2000")
// 		submit()
// 		waitForApp(1000)
// 		checkMessage("107.73")
// 		clearAmount()
//
// 		typeAmount("100000")
// 		submit()
// 		waitForApp(1000)
// 		checkMessage("-")
//
// 		clickMessage()
// 		waitForApp(1000)
// 		clickSeeOtherModalOptions()
// 		clickPi4Tile()
// 		waitForApp(500)
// 		clickSeeOtherModalOptions()
// 		clickNiTile()
// 		waitForApp(500)
// 		clickSeeOtherModalOptions()
// 		clickPayMonthlyTile()
// 	}
//
// 	// TODO - Need to change client ids for other tests
// }
