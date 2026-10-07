package com.vm.coinfold.app.feature.expenses.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.action_save
import coinfold.shared.generated.resources.expense_account
import coinfold.shared.generated.resources.expense_no_accounts
import coinfold.shared.generated.resources.expense_title
import coinfold.shared.generated.resources.field_amount
import coinfold.shared.generated.resources.field_currency
import coinfold.shared.generated.resources.field_note
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.ui.components.CategoryBadge
import com.vm.coinfold.app.shared.ui.components.CurrencySelector
import com.vm.coinfold.app.shared.ui.components.DateField
import com.vm.coinfold.app.utils.Calculator
import com.vm.coinfold.app.utils.CalculatorInput
import com.vm.coinfold.app.utils.OP_DIVIDE
import com.vm.coinfold.app.utils.OP_MINUS
import com.vm.coinfold.app.utils.OP_PLUS
import com.vm.coinfold.app.utils.OP_TIMES
import com.vm.coinfold.app.utils.today
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

/** Bottom sheet opened from a category circle: amount with mini-calculator, currency, account, note, date. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseSheet(
    category: Category,
    accounts: List<AccountWithBalance>,
    initialCurrency: Currency,
    onSave: (amount: BigDecimal, currency: Currency, accountId: Long, note: String, date: LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    var expression by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(initialCurrency) }
    var accountId by remember { mutableStateOf(accounts.firstOrNull()?.account?.id) }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today()) }

    // The result is computed from the expression and only valid if it is a positive amount.
    val result = Calculator.evaluate(expression)
    val money = result?.let { Money.of(it, currency) }
    val canSave = money != null && money.minorUnits > 0 && accountId != null
    val hasOperator = expression.drop(1).any { it in "+$OP_MINUS$OP_TIMES$OP_DIVIDE" }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CategoryBadge(category.icon, Color(category.color), size = 36)
                Text(
                    stringResource(Res.string.expense_title, category.name),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            if (accounts.isEmpty()) {
                Text(stringResource(Res.string.expense_no_accounts), color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(24.dp))
                return@Column
            }

            Column {
                Text(
                    stringResource(Res.string.field_amount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = expression.ifEmpty { "0" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (expression.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                )
                if (hasOperator && money != null) {
                    Text(
                        "= ${money.toBigDecimal().toPlainString()} ${currency.code}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            CalculatorKeypad(
                onKey = { expression = CalculatorInput.append(expression, it) },
                onBackspace = { expression = CalculatorInput.backspace(expression) },
            )

            Text(stringResource(Res.string.field_currency), style = MaterialTheme.typography.labelLarge)
            CurrencySelector(currency, onSelected = { currency = it })

            Text(stringResource(Res.string.expense_account), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                accounts.forEach { item ->
                    FilterChip(
                        selected = item.account.id == accountId,
                        onClick = { accountId = item.account.id },
                        label = { Text("${item.account.name} · ${item.account.currency.code}") },
                    )
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(Res.string.field_note)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DateField(date, onDateChange = { date = it })
                Button(
                    enabled = canSave,
                    onClick = {
                        val id = accountId
                        if (result != null && id != null) onSave(result, currency, id, note, date)
                    },
                ) { Text(stringResource(Res.string.action_save)) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

private val KEY_ROWS = listOf(
    listOf('7', '8', '9', OP_DIVIDE),
    listOf('4', '5', '6', OP_TIMES),
    listOf('1', '2', '3', OP_MINUS),
    listOf('.', '0', '⌫', OP_PLUS),
)

@Composable
private fun CalculatorKeypad(onKey: (Char) -> Unit, onBackspace: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        KEY_ROWS.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { key ->
                    val isOperator = key in "+$OP_MINUS$OP_TIMES$OP_DIVIDE"
                    Surface(
                        onClick = { if (key == '⌫') onBackspace() else onKey(key) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOperator) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f).height(48.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(key.toString(), style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }
    }
}
