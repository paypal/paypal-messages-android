package com.paypal.messagesdemo.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CircularIndicator(progressBar: Boolean) {
	if (!progressBar) return

	Box(
		modifier = Modifier.padding(vertical = 8.dp),
		contentAlignment = Alignment.Center,
	) {
		CircularProgressIndicator(
			modifier = Modifier.width(32.dp),
			color = MaterialTheme.colorScheme.secondary,
		)
	}
}
