package com.paypal.messagesdemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun StyledTextField(
	value: String,
	onChange: (text: String) -> Unit,
	keyboardType: KeyboardType? = KeyboardType.Text,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(36.dp)
			.background(
				Color.LightGray,
				RectangleShape,
			),
	) {
		BasicTextField(
			value = value,
			onValueChange = onChange,
			singleLine = true,
			textStyle = MaterialTheme.typography.bodyMedium,
			keyboardOptions = KeyboardOptions.Default.copy(
				keyboardType = keyboardType ?: KeyboardType.Text,
				imeAction = ImeAction.Done,
			),
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight()
				.padding(start = 4.dp, end = 4.dp, top = 8.dp),
		)
	}
}
