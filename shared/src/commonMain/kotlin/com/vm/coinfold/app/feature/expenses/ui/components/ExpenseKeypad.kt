package com.vm.coinfold.app.feature.expenses.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.utils.OP_DIVIDE
import com.vm.coinfold.app.utils.OP_MINUS
import com.vm.coinfold.app.utils.OP_PLUS
import com.vm.coinfold.app.utils.OP_TIMES

private val KEY_HEIGHT = 56.dp
private val KEY_GAP = 6.dp

/**
 * Calculator keypad of the expense sheet. Five columns: operators, three digit columns and an action
 * column (backspace, date, big confirm button). The bottom-left digit key shows the current currency
 * and cycles UAH, USD, EUR when pressed.
 */
@Composable
fun ExpenseKeypad(
    currency: Currency,
    accent: Color,
    confirmEnabled: Boolean,
    onKey: (Char) -> Unit,
    onBackspace: () -> Unit,
    onCurrencyClick: () -> Unit,
    onDateClick: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(KEY_GAP)) {
        KeyColumn(Modifier.weight(1f)) {
            listOf(OP_DIVIDE, OP_TIMES, OP_MINUS, OP_PLUS).forEach { op ->
                TextKey(op.toString(), emphasized = true) { onKey(op) }
            }
        }
        KeyColumn(Modifier.weight(1f)) {
            TextKey("7") { onKey('7') }
            TextKey("4") { onKey('4') }
            TextKey("1") { onKey('1') }
            TextKey(currency.code, emphasized = true, onClick = onCurrencyClick)
        }
        KeyColumn(Modifier.weight(1f)) {
            TextKey("8") { onKey('8') }
            TextKey("5") { onKey('5') }
            TextKey("2") { onKey('2') }
            TextKey("0") { onKey('0') }
        }
        KeyColumn(Modifier.weight(1f)) {
            TextKey("9") { onKey('9') }
            TextKey("6") { onKey('6') }
            TextKey("3") { onKey('3') }
            TextKey(".") { onKey('.') }
        }
        KeyColumn(Modifier.weight(1f)) {
            IconKey(Icons.AutoMirrored.Outlined.Backspace, onClick = onBackspace)
            IconKey(Icons.Outlined.CalendarMonth, onClick = onDateClick)
            // The confirm key spans the two bottom rows.
            Surface(
                onClick = onConfirm,
                enabled = confirmEnabled,
                shape = RoundedCornerShape(14.dp),
                color = if (confirmEnabled) accent else accent.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth().height(KEY_HEIGHT * 2 + KEY_GAP),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun KeyColumn(modifier: Modifier, content: @Composable () -> Unit) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(KEY_GAP)) { content() }
}

@Composable
private fun TextKey(label: String, emphasized: Boolean = false, onClick: () -> Unit) {
    Key(emphasized, onClick) {
        Text(
            label,
            style = if (label.length > 1) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
        )
    }
}

@Composable
private fun IconKey(icon: ImageVector, onClick: () -> Unit) {
    Key(emphasized = true, onClick = onClick) { Icon(icon, contentDescription = null) }
}

@Composable
private fun Key(emphasized: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (emphasized) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().height(KEY_HEIGHT),
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}
