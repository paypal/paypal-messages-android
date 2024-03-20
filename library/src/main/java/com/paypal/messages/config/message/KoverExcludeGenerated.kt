package com.paypal.messages.config.message

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
