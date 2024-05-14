package com.paypal.messagesdemo

import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CircularIndicator(progressBar: Boolean) {
	if (!progressBar) return

	CircularProgressIndicator(
		modifier = Modifier
			.width(32.dp),
		color = MaterialTheme.colorScheme.secondary,
		trackColor = MaterialTheme.colorScheme.surfaceVariant,
	)
}
