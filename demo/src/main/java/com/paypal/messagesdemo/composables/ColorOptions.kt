package com.paypal.messagesdemo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ColorOptions(colorGroupOptions: List<String>, selected: String, onSelected: (text: String) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        colorGroupOptions.forEach { text ->
            Row(
                modifier = Modifier
                    .height(intrinsicSize = IntrinsicSize.Max)
                    .padding(end = Dp(8.0F))
                    .selectable(
                        selected = (text == selected),
                        onClick = { onSelected(text) },
                    ),
            ) {
                RadioButton(
                    selected = (text == selected),
                    onClick = { onSelected(text) },
                    modifier = Modifier
                        .padding(end = Dp(0F))
                        .size(Dp(24f)),
                )
                Text(
                    text = text,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.CenterVertically),
                )
            }
        }
    }
}
