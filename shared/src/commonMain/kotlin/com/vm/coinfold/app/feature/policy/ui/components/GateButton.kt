package com.vm.coinfold.app.feature.policy.ui.components

import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Tall, rounded button of the start-up screens: filled for the main action, outlined for the rest. */
@Composable
fun GateButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false) {
    val buttonModifier = modifier.widthIn(min = 240.dp).heightIn(min = 54.dp)
    val shape = MaterialTheme.shapes.large
    val label: @Composable () -> Unit = {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
    if (primary) {
        Button(onClick = onClick, modifier = buttonModifier, shape = shape) { label() }
    } else {
        OutlinedButton(onClick = onClick, modifier = buttonModifier, shape = shape) { label() }
    }
}
