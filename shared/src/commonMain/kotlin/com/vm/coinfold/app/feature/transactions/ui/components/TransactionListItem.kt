package com.vm.coinfold.app.feature.transactions.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.transaction_manual_withdrawal
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionItem
import com.vm.coinfold.app.shared.domain.models.TransactionType
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.shared.ui.components.displayName
import com.vm.coinfold.app.shared.ui.theme.IncomeGreen
import com.vm.coinfold.app.utils.format
import org.jetbrains.compose.resources.stringResource

@Composable
fun TransactionListItem(item: TransactionItem, onClick: () -> Unit) {
    val language = LocalAppLanguage.current
    val isIncome = item.type == TransactionType.INCOME
    val title = when {
        item.category != null -> item.category.name
        isIncome -> item.source?.displayName().orEmpty()
        else -> stringResource(Res.string.transaction_manual_withdrawal)
    }
    val badgeColor = item.category?.let { Color(it.color) } ?: if (isIncome) IncomeGreen else MaterialTheme.colorScheme.outline
    val badgeText = item.category?.icon ?: if (isIncome) "+" else "−"

    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(badgeColor),
            contentAlignment = Alignment.Center,
        ) { Text(badgeText, style = MaterialTheme.typography.titleMedium, color = Color.White) }

        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val subtitle = listOf(item.note, item.accountName).filter { it.isNotBlank() }.joinToString(" · ")
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                item.signedAmount.format(language, showPlus = true),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isIncome) IncomeGreen else MaterialTheme.colorScheme.onSurface,
            )
            if (item.amount.currency != item.accountCurrency) {
                // What actually moved on the account, at the rate fixed when the expense was made.
                val accountSide = if (isIncome) item.accountAmount else -item.accountAmount
                Text(
                    accountSide.format(language, showPlus = true),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}
