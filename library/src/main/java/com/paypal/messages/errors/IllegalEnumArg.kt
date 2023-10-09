package com.paypal.messages.errors

class IllegalEnumArg(enumName: String, enumValues: Int) :
	BaseException(
		"PayPalMessage Attempted to create a $enumName with an invalid index. " +
			"Please use an index that is between 0 and ${enumValues - 1} and try again.",
		null,
	)
