package com.paypal.messages.utils

@Retention(AnnotationRetention.RUNTIME)
@Target(
	AnnotationTarget.CLASS,
	AnnotationTarget.CONSTRUCTOR,
	AnnotationTarget.FIELD,
	AnnotationTarget.FUNCTION,
	AnnotationTarget.LOCAL_VARIABLE,
	AnnotationTarget.PROPERTY,
	AnnotationTarget.TYPE,
	AnnotationTarget.TYPE_PARAMETER,
	AnnotationTarget.VALUE_PARAMETER,
)
annotation class KoverExcludeGenerated
