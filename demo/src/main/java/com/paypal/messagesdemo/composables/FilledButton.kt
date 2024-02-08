package com.paypal.messagesdemo

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun FilledButton(onClick: () -> Unit, text: String, buttonEnabled: Boolean) {
	var isButtonClicked by remember { mutableStateOf(false) }
	Button(
		modifier = Modifier.width(intrinsicSize = IntrinsicSize.Min),
		enabled = buttonEnabled,
		onClick = {
			onClick()
			isButtonClicked = !isButtonClicked
		},
	) {
		Text(text)
	}
}
