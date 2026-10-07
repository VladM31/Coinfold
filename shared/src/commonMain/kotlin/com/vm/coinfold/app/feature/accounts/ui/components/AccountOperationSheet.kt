package com.vm.coinfold.app.feature.accounts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.accounts_balance_after
import coinfold.shared.generated.resources.accounts_operation_label
import coinfold.shared.generated.resources.accounts_top_up
import coinfold.shared.generated.resources.accounts_withdraw
import coinfold.shared.generated.resources.date_today
import coinfold.shared.generated.resources.expense_account
import coinfold.shared.generated.resources.expense_notes
import coinfold.shared.generated.resources.field_amount
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.TransactionType
import com.vm.coinfold.app.shared.ui.components.CalculatorKeypad
import com.vm.coinfold.app.shared.ui.components.CategoryIcon
import com.vm.coinfold.app.shared.ui.components.DatePickerDialogHost
import com.vm.coinfold.app.shared.ui.components.IncomeSourcePicker
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.shared.ui.components.SheetHeaderHalf
import com.vm.coinfold.app.shared.ui.components.resolveSource
import com.vm.coinfold.app.shared.ui.theme.IncomeGreen
import com.vm.coinfold.app.utils.Calculator
import com.vm.coinfold.app.utils.CalculatorInput
import com.vm.coinfold.app.utils.OP_DIVIDE
import com.vm.coinfold.app.utils.OP_MINUS
import com.vm.coinfold.app.utils.OP_PLUS
import com.vm.coinfold.app.utils.OP_TIMES
import com.vm.coinfold.app.utils.format
import com.vm.coinfold.app.utils.today
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

private const val OPERATORS = "$OP_PLUS$OP_MINUS$OP_TIMES$OP_DIVIDE"

/**
 * Calculator sheet opened by tapping an account: the same keypad as the expense sheet, plus a switch between
 * topping up (with a source) and withdrawing. The amount is always in the currency of the account.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountOperationSheet(
    item: AccountWithBalance,
    customSources: List<String>,
    onSave: (type: TransactionType, amount: BigDecimal, source: IncomeSource?, note: String, date: LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val language = LocalAppLanguage.current
    val account = item.account
    var type by remember { mutableStateOf(TransactionType.INCOME) }
    var expression by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today()) }
    var datePickerOpen by remember { mutableStateOf(false) }
    var selectedSource by remember { mutableStateOf<IncomeSource>(IncomeSource.Preset.SALARY) }
    var customText by remember { mutableStateOf("") }

    val isIncome = type == TransactionType.INCOME
    val accent = if (isIncome) IncomeGreen else MaterialTheme.colorScheme.error
    val accountColor = Color(account.color ?: DEFAULT_ACCOUNT_COLOR)

    val result = Calculator.evaluate(expression)
    val entered = result?.let { Money.of(it, account.currency) }?.takeIf { it.minorUnits > 0 }
    val balanceAfter = when {
        entered == null -> item.balance
        isIncome -> item.balance + entered
        else -> item.balance - entered
    }
    val hasOperator = expression.drop(1).any { it in OPERATORS }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null,
    ) {
        Column(Modifier.navigationBarsPadding().verticalScroll(rememberScrollState())) {
            // Left: the account. Right: the operation; tap it to switch between top-up and withdrawal.
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Box(Modifier.weight(1f).background(accountColor)) {
                    SheetHeaderHalf(label = stringResource(Res.string.expense_account), title = account.name) {
                        CategoryIcon(account.icon ?: DEFAULT_ACCOUNT_ICON, tint = accountColor, size = 24)
                    }
                }
                Box(
                    Modifier
                        .weight(1f)
                        .background(accent)
                        .clickable {
                            type = if (isIncome) TransactionType.EXPENSE else TransactionType.INCOME
                        },
                ) {
                    SheetHeaderHalf(
                        label = stringResource(Res.string.accounts_operation_label),
                        title = stringResource(if (isIncome) Res.string.accounts_top_up else Res.string.accounts_withdraw),
                    ) {
                        Text(if (isIncome) "+" else "−", style = MaterialTheme.typography.titleLarge, color = accent)
                    }
                }
            }

            Column(
                Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AmountCard(
                        label = stringResource(Res.string.accounts_balance_after),
                        value = balanceAfter.format(language),
                        subtitle = null,
                        container = MaterialTheme.colorScheme.primaryContainer,
                        content = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    AmountCard(
                        label = stringResource(Res.string.field_amount),
                        value = "${expression.ifEmpty { "0" }} ${account.currency.code}",
                        subtitle = if (hasOperator && entered != null) "= ${entered.format(language)}" else null,
                        container = accent.copy(alpha = 0.16f),
                        content = accent,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }

                if (isIncome) {
                    IncomeSourcePicker(
                        selected = selectedSource,
                        customText = customText,
                        customSources = customSources,
                        onSelected = { selectedSource = it },
                        onCustomTextChange = { customText = it },
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

                CalculatorKeypad(
                    currency = account.currency,
                    accent = accent,
                    confirmEnabled = entered != null,
                    onKey = { expression = CalculatorInput.append(expression, it) },
                    onBackspace = { expression = CalculatorInput.backspace(expression) },
                    // The currency of an account is fixed, so the key is just a label here.
                    onCurrencyClick = null,
                    onDateClick = { datePickerOpen = true },
                    onConfirm = {
                        if (result != null) {
                            val source = if (isIncome) resolveSource(selectedSource, customText) else null
                            onSave(type, result, source, note, date)
                        }
                    },
                )

                Text(
                    text = if (date == today()) {
                        stringResource(Res.string.date_today) + ", " + date.format()
                    } else {
                        date.format()
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(4.dp))
            }
        }
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
