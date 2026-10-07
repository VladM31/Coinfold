package com.vm.coinfold.app.shared.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.action_cancel
import coinfold.shared.generated.resources.action_save
import com.vm.coinfold.app.shared.domain.Currency
import com.vm.coinfold.app.shared.domain.ResolvedLanguage
import com.vm.coinfold.app.utils.dateFromPickerMillis
import com.vm.coinfold.app.utils.format
import com.vm.coinfold.app.utils.pickerMillisFor
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

/** Language used for number/date formatting; provided at the app root. */
val LocalAppLanguage = compositionLocalOf { ResolvedLanguage.EN }

/** Colors offered for accounts and categories. */
val ColorPalette: List<Long> = listOf(
    0xFF7C4DFF, 0xFF5E35D6, 0xFFAB47BC, 0xFFE91E63,
    0xFFFF7043, 0xFFFFB300, 0xFF8BC34A, 0xFF26A69A,
    0xFF29B6F6, 0xFF3F51B5, 0xFF8D6E63, 0xFF78909C,
)

@Composable
fun CurrencySelector(
    selected: Currency,
    onSelected: (Currency) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Currency.entries.forEach { currency ->
            FilterChip(
                selected = currency == selected,
                onClick = { onSelected(currency) },
                enabled = enabled,
                label = { Text(currency.code) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPicker(selected: Long?, onSelected: (Long?) -> Unit, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ColorPalette.forEach { value ->
            val isSelected = value == selected
            ColorDot(
                color = Color(value),
                size = 36,
                modifier = Modifier
                    .clip(CircleShape)
                    .then(
                        if (isSelected) {
                            Modifier.border(BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface), CircleShape)
                        } else {
                            Modifier
                        },
                    )
                    .clickable { onSelected(if (isSelected) null else value) },
            )
        }
    }
}

@Composable
fun ColorDot(color: Color, size: Int, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier.size(size.dp).clip(CircleShape).background(color),
    )
}

/** Button showing the date; opens a date picker dialog on click. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(date: LocalDate, onDateChange: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { open = true }, modifier = modifier) { Text(date.format()) }
    if (open) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = pickerMillisFor(date))
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { onDateChange(dateFromPickerMillis(it)) }
                    open = false
                }) { Text(stringResource(Res.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text(stringResource(Res.string.action_cancel)) }
            },
        ) { DatePicker(state = pickerState) }
    }
}
