package com.vm.coinfold.app.shared.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.accounts_source
import coinfold.shared.generated.resources.accounts_source_custom
import coinfold.shared.generated.resources.source_debt_return
import coinfold.shared.generated.resources.source_gift
import coinfold.shared.generated.resources.source_other
import coinfold.shared.generated.resources.source_salary
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private fun IncomeSource.Preset.label(): StringResource = when (this) {
    IncomeSource.Preset.SALARY -> Res.string.source_salary
    IncomeSource.Preset.DEBT_RETURN -> Res.string.source_debt_return
    IncomeSource.Preset.GIFT -> Res.string.source_gift
    IncomeSource.Preset.OTHER -> Res.string.source_other
}

/** Human readable, localized name of an income source. */
@Composable
fun IncomeSource.displayName(): String = when (this) {
    is IncomeSource.Preset -> stringResource(label())
    is IncomeSource.Custom -> name
}

/**
 * Chips with the preset and previously used custom sources plus a field for a new one.
 * [customText] wins over [selected] while it is not blank.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IncomeSourcePicker(
    selected: IncomeSource,
    customText: String,
    customSources: List<String>,
    onSelected: (IncomeSource) -> Unit,
    onCustomTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(Res.string.accounts_source), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val options = IncomeSource.Preset.entries + customSources.map { IncomeSource.Custom(it) }
            options.forEach { option ->
                FilterChip(
                    selected = customText.isBlank() && selected == option,
                    onClick = { onSelected(option); onCustomTextChange("") },
                    label = { Text(option.displayName()) },
                )
            }
        }
        OutlinedTextField(
            value = customText,
            onValueChange = onCustomTextChange,
            label = { Text(stringResource(Res.string.accounts_source_custom)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** The source the user ended up with: the typed custom name if any, otherwise the selected chip. */
fun resolveSource(selected: IncomeSource, customText: String): IncomeSource =
    customText.trim().takeIf { it.isNotEmpty() }?.let { IncomeSource.Custom(it) } ?: selected
