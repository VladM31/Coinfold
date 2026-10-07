package com.vm.coinfold.app.feature.expenses.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.expense_from_account
import coinfold.shared.generated.resources.expense_to_category
import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.shared.ui.components.CategoryIcon
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.utils.format
import org.jetbrains.compose.resources.stringResource

/**
 * Top of the expense sheet: the account the money leaves from (tap to pick another one) on the left
 * and the category it goes to on the right.
 */
@Composable
fun ExpenseSheetHeader(
    accounts: List<AccountWithBalance>,
    selectedAccount: AccountWithBalance?,
    category: Category,
    onAccountSelected: (AccountWithBalance) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLanguage.current
    var menuOpen by remember { mutableStateOf(false) }
    val categoryColor = Color(category.color)

    Row(modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(Modifier.weight(1f).background(MaterialTheme.colorScheme.primary)) {
            HeaderHalf(
                label = stringResource(Res.string.expense_from_account),
                title = selectedAccount?.account?.name.orEmpty(),
                modifier = Modifier.clickable(enabled = accounts.size > 1) { menuOpen = true },
            ) {
                Icon(
                    Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                accounts.forEach { item ->
                    DropdownMenuItem(
                        text = { Text("${item.account.name} · ${item.balance.format(language)}") },
                        onClick = {
                            onAccountSelected(item)
                            menuOpen = false
                        },
                    )
                }
            }
        }
        Box(Modifier.weight(1f).background(categoryColor)) {
            HeaderHalf(label = stringResource(Res.string.expense_to_category), title = category.name) {
                CategoryIcon(category.icon, tint = categoryColor, size = 24)
            }
        }
    }
}

@Composable
private fun HeaderHalf(
    label: String,
    title: String,
    modifier: Modifier = Modifier,
    badge: @Composable () -> Unit,
) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(Color.White),
            contentAlignment = Alignment.Center,
        ) { badge() }
    }
}
