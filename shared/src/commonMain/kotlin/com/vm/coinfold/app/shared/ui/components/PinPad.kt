package com.vm.coinfold.app.shared.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A round-key number pad with PIN dots above it. [filled] of [length] dots are filled; [message] (for example
 * "Wrong PIN") is shown in the error color when [isError]. [extraKey] fills the bottom-left cell, where the lock
 * screen puts the biometric button.
 */
@Composable
fun PinPad(
    title: String,
    filled: Int,
    length: Int,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    extraKey: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier.widthIn(max = 320.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            repeat(length) { index ->
                val on = index < filled
                Box(
                    Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isError -> MaterialTheme.colorScheme.error
                                on -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outlineVariant
                            },
                        ),
                )
            }
        }
        // Reserve the line even when empty, so the keys do not jump when a message appears.
        Text(
            text = message.orEmpty().ifEmpty { " " },
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().height(40.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("123", "456", "789").forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    row.forEach { digit -> PinKey(enabled = enabled, onClick = { onDigit(digit) }) { Text(digit.toString(), style = MaterialTheme.typography.headlineSmall) } }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(KEY_SIZE), contentAlignment = Alignment.Center) { extraKey?.invoke() }
                PinKey(enabled = enabled, onClick = { onDigit('0') }) { Text("0", style = MaterialTheme.typography.headlineSmall) }
                PinKey(enabled = true, plain = true, onClick = onBackspace) {
                    Icon(Icons.AutoMirrored.Outlined.Backspace, contentDescription = null)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

private val KEY_SIZE = 72.dp

@Composable
private fun PinKey(enabled: Boolean, onClick: () -> Unit, plain: Boolean = false, content: @Composable () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (plain) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
        border = if (plain) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.size(KEY_SIZE).padding(0.dp),
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}
