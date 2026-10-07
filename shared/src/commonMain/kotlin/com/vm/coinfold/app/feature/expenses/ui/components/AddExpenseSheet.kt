package com.vm.coinfold.app.feature.expenses.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.date_today
import coinfold.shared.generated.resources.expense_amount_label
import coinfold.shared.generated.resources.expense_no_accounts
import coinfold.shared.generated.resources.expense_notes
import coinfold.shared.generated.resources.expense_withdrawal
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.feature.currency.domain.models.RateTable
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.feature.transactions.domain.models.NoteSuggestion
import com.vm.coinfold.app.feature.transactions.domain.services.suggestCategory
import com.vm.coinfold.app.feature.transactions.domain.services.suggestNotes
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.ui.components.CalculatorKeypad
import com.vm.coinfold.app.shared.ui.components.CategoryIcon
import com.vm.coinfold.app.shared.ui.components.DatePickerDialogHost
import com.vm.coinfold.app.shared.ui.components.ExchangeRateDialog
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.shared.ui.components.RateSettingsButton
import com.vm.coinfold.app.utils.Calculator
import com.vm.coinfold.app.utils.CalculatorInput
import com.vm.coinfold.app.utils.OP_DIVIDE
import com.vm.coinfold.app.utils.OP_MINUS
import com.vm.coinfold.app.utils.OP_PLUS
import com.vm.coinfold.app.utils.OP_TIMES
import com.vm.coinfold.app.utils.format
import com.vm.coinfold.app.utils.parseRate
import com.vm.coinfold.app.utils.today
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

private const val OPERATORS = "$OP_PLUS$OP_MINUS$OP_TIMES$OP_DIVIDE"

/**
 * Bottom sheet opened from a category circle. Only what is needed to record an expense: the amount
 * (with a calculator), its currency, the account and an optional note; the date is one key away.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseSheet(
    category: Category,
    categories: List<Category>,
    noteSuggestions: List<NoteSuggestion>,
    accounts: List<AccountWithBalance>,
    rates: RateTable,
    initialCurrency: Currency,
    onSave: (
        amount: BigDecimal,
        currency: Currency,
        accountId: Long,
        categoryId: Long,
        note: String,
        date: LocalDate,
        rateOverride: BigDecimal?,
    ) -> Unit,
    onDismiss: () -> Unit,
) {
    val language = LocalAppLanguage.current
    var expression by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(initialCurrency) }
    var accountId by remember { mutableStateOf(accounts.firstOrNull()?.account?.id) }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today()) }
    var datePickerOpen by remember { mutableStateOf(false) }
    // The category can change when the user accepts a suggestion based on the note.
    var selectedCategory by remember { mutableStateOf(category) }
    // Exchange rate typed for this one expense; blank means "use the bank rate".
    var rateText by remember { mutableStateOf("") }
    var rateDialogOpen by remember { mutableStateOf(false) }

    val account = accounts.firstOrNull { it.account.id == accountId }
    val categoryColor = Color(selectedCategory.color)

    // The typed expression is evaluated live; it is valid only if it gives a positive amount.
    val result = Calculator.evaluate(expression)
    val entered = result?.let { Money.of(it, currency) }?.takeIf { it.minorUnits > 0 }
    // What leaves the account: the same amount, or the converted one when the currencies differ.
    val accountCurrency = account?.account?.currency
    val crossCurrency = accountCurrency != null && currency != accountCurrency
    val rateOverride = if (crossCurrency) parseRate(rateText) else null
    val autoRate = if (crossCurrency && accountCurrency != null) rates.find(currency, accountCurrency) else null
    val fromAccount = when {
        entered == null || accountCurrency == null -> null
        rateOverride != null -> entered.convertTo(accountCurrency, rateOverride)
        else -> rates.convert(entered, accountCurrency)?.money
    }
    val rateValid = !crossCurrency || rateText.isBlank() || rateOverride != null
    val canSave = entered != null && fromAccount != null && account != null && rateValid

    val noteHints = suggestNotes(noteSuggestions, note, selectedCategory.id)
    val suggestedCategory = suggestCategory(noteSuggestions, note)
        ?.let { id -> categories.firstOrNull { it.id == id && it.id != selectedCategory.id } }
    val hasOperator = expression.drop(1).any { it in OPERATORS }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null,
    ) {
        Column(Modifier.navigationBarsPadding().verticalScroll(rememberScrollState())) {
            if (accounts.isEmpty()) {
                Text(
                    stringResource(Res.string.expense_no_accounts),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(24.dp),
                )
                return@Column
            }

            ExpenseSheetHeader(
                accounts = accounts,
                selectedAccount = account,
                category = selectedCategory,
                onAccountSelected = {
                    accountId = it.account.id
                    rateText = ""
                },
            )

            Column(
                Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AmountCard(
                        label = stringResource(Res.string.expense_withdrawal),
                        value = fromAccount?.format(language) ?: "—",
                        subtitle = null,
                        container = MaterialTheme.colorScheme.primaryContainer,
                        content = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    AmountCard(
                        label = stringResource(Res.string.expense_amount_label),
                        value = "${expression.ifEmpty { "0" }} ${currency.code}",
                        subtitle = if (hasOperator && entered != null) "= ${entered.format(language)}" else null,
                        container = categoryColor.copy(alpha = 0.16f),
                        content = categoryColor,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text(stringResource(Res.string.expense_notes)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (suggestedCategory != null || noteHints.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // "Silpo" was used with Groceries before: offer to switch to that category.
                        suggestedCategory?.let { suggestion ->
                            AssistChip(
                                onClick = { selectedCategory = suggestion },
                                label = { Text(suggestion.name + "?") },
                                leadingIcon = {
                                    CategoryIcon(suggestion.icon, tint = MaterialTheme.colorScheme.primary, size = 18)
                                },
                            )
                        }
                        noteHints.forEach { hint ->
                            AssistChip(onClick = { note = hint }, label = { Text(hint) })
                        }
                    }
                }

                CalculatorKeypad(
                    currency = currency,
                    accent = categoryColor,
                    confirmEnabled = canSave,
                    onKey = { expression = CalculatorInput.append(expression, it) },
                    onBackspace = { expression = CalculatorInput.backspace(expression) },
                    onCurrencyClick = {
                        currency = Currency.entries[(currency.ordinal + 1) % Currency.entries.size]
                        rateText = ""
                    },
                    onDateClick = { datePickerOpen = true },
                    onConfirm = {
                        val id = accountId
                        if (result != null && id != null) onSave(result, currency, id, selectedCategory.id, note, date, rateOverride)
                    },
                )

                // The date on the left, the (quiet) exchange rate button on the right of the same line.
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (date == today()) {
                            stringResource(Res.string.date_today) + ", " + date.format()
                        } else {
                            date.format()
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    // Only relevant when the currency differs from the account; absent otherwise.
                    if (crossCurrency) {
                        RateSettingsButton(customRate = rateText, onClick = { rateDialogOpen = true })
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }

    if (rateDialogOpen && accountCurrency != null) {
        ExchangeRateDialog(
            fromCode = currency.code,
            toCode = accountCurrency.code,
            bankRate = autoRate?.toPlainString(),
            current = rateText,
            onConfirm = {
                rateText = it
                rateDialogOpen = false
            },
            onDismiss = { rateDialogOpen = false },
        )
    }

    if (datePickerOpen) {
        DatePickerDialogHost(date, onDateChange = { date = it }, onDismiss = { datePickerOpen = false })
    }
}

@Composable
private fun AmountCard(
    label: String,
    value: String,
    subtitle: String?,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = container) {
        Column(
            Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = content)
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = content)
            }
        }
    }
}
