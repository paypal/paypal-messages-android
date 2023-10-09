package com.paypal.messages.utils

/**
 *  Filters out anything that shouldn't be included in a jacoco coverage report.
 *  Examples to exclude are things like View logic or Android dependent code
 *  that cannot be mocked and unit tested.
 *  See: https://github.com/jacoco/jacoco/pull/822/files
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
	AnnotationTarget.FUNCTION,
	AnnotationTarget.CLASS,
	AnnotationTarget.FIELD,
	AnnotationTarget.FILE,
)
annotation class IgnoreGeneratedTestReport
