package com.paypal.messagesdemo.composables

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paypal.messagesdemo.StyledTextField

@Composable
fun InputField(
	text: String,
	value: String,
	onChange: (text: String) -> Unit,
	keyboardType: KeyboardType = KeyboardType.Text,
	padding: Dp = 9.dp,
) {
	Row(
		modifier = Modifier.padding(vertical = padding),
	) {
		Text(
			text = text,
			fontSize = 14.sp,
			fontWeight = FontWeight.Bold,
			modifier = Modifier
				.width(125.dp)
				.height(intrinsicSize = IntrinsicSize.Max)
				.align(Alignment.CenterVertically),
		)

		StyledTextField(
			value = value,
			onChange = onChange,
			keyboardType = keyboardType,
		)
	}
}
