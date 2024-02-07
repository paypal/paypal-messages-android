package com.paypal.messagesdemo

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IgnoreCacheSwitch(ignoreCache: Boolean, onChange: (text: Boolean) -> Unit) {
    Row {
        Switch(
            checked = ignoreCache,
            onCheckedChange = onChange,
        )

        Text(
            text = "Ignore Cache",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 8.dp),
        )
    }
}
