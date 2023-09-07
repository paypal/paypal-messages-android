package com.paypal.messagesdemo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@Composable
fun <T> Dropdown(label: String, value: T, options: Array<T>, onValueChange: (T) -> Unit) {
	var expanded by remember { mutableStateOf(false) }

	Box {
		Button(
			onClick = { expanded = !expanded },
			shape = RectangleShape,
			modifier = Modifier.width(200.dp)
		) {
			Text("$label: $value")
		}
		DropdownMenu(
			expanded = expanded,
			onDismissRequest = { expanded = false },
			modifier = Modifier.width(200.dp)
		) {
			options.forEach { option ->
				DropdownMenuItem(
					text = { Text(option.toString()) },
					onClick = {
						onValueChange(option)
						expanded = false
					}
				)
			}
		}
	}
}
