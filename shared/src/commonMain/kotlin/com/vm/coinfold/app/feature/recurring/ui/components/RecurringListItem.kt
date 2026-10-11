package com.vm.coinfold.app.feature.recurring.ui.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.freq_daily
import coinfold.shared.generated.resources.freq_every_days
import coinfold.shared.generated.resources.freq_every_n_days
import coinfold.shared.generated.resources.freq_monthly_on
import coinfold.shared.generated.resources.freq_monthly
import coinfold.shared.generated.resources.freq_weekly
import coinfold.shared.generated.resources.freq_yearly
import coinfold.shared.generated.resources.recurring_next
import coinfold.shared.generated.resources.transaction_manual_withdrawal
import com.vm.coinfold.app.feature.recurring.domain.models.Frequency
import com.vm.coinfold.app.feature.recurring.domain.models.RecurringPayment
import com.vm.coinfold.app.shared.domain.models.TransactionType
import com.vm.coinfold.app.shared.ui.components.CategoryBadge
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.shared.ui.components.displayName
import com.vm.coinfold.app.shared.ui.theme.IncomeGreen
import com.vm.coinfold.app.utils.format
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Full description of the schedule, for example "Every 28 days" or "Every month on day 15". */
@Composable
private fun RecurringPayment.scheduleText(): String = when (frequency) {
    Frequency.EVERY_N_DAYS -> stringResource(Res.string.freq_every_days, anchorDay)
    Frequency.MONTHLY -> stringResource(Res.string.freq_monthly_on, anchorDay)
    else -> stringResource(frequency.label())
}

/** Label of a frequency, for example "Every month". */
fun Frequency.label(): StringResource = when (this) {
    Frequency.DAILY -> Res.string.freq_daily
    Frequency.WEEKLY -> Res.string.freq_weekly
    Frequency.EVERY_N_DAYS -> Res.string.freq_every_n_days
    Frequency.MONTHLY -> Res.string.freq_monthly
    Frequency.YEARLY -> Res.string.freq_yearly
}

/** One schedule: badge, title, "every month, next on ...", amount and an on/off switch. Tap to edit. */
@Composable
fun RecurringListItem(
    payment: RecurringPayment,
    onClick: () -> Unit,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLanguage.current
    val isIncome = payment.type == TransactionType.INCOME
    val category = payment.category
    val title = payment.note.ifBlank {
        when {
            category != null -> category.name
            isIncome -> payment.source?.displayName().orEmpty()
            else -> stringResource(Res.string.transaction_manual_withdrawal)
        }
    }

    Row(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .alpha(if (payment.isActive) 1f else 0.5f)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (category != null) {
            CategoryBadge(category.icon, Color(category.color))
        } else {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isIncome) IncomeGreen else MaterialTheme.colorScheme.outline),
                contentAlignment = Alignment.Center,
            ) { Text(if (isIncome) "+" else "−", color = Color.White, style = MaterialTheme.typography.titleMedium) }
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                payment.scheduleText() + " · " +
                    stringResource(Res.string.recurring_next, payment.nextDate.format()) + " · " + payment.accountName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            (if (isIncome) payment.amount else -payment.amount).format(language, showPlus = true),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (isIncome) IncomeGreen else MaterialTheme.colorScheme.onSurface,
        )
        Switch(checked = payment.isActive, onCheckedChange = onActiveChange)
    }
}
