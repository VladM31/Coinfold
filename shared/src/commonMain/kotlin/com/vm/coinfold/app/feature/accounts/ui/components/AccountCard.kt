package com.vm.coinfold.app.feature.accounts.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.accounts_top_up
import coinfold.shared.generated.resources.accounts_withdraw
import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.feature.accounts.domain.viewmodels.AccountsIntent
import com.vm.coinfold.app.shared.ui.components.ColorDot
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.utils.format
import org.jetbrains.compose.resources.stringResource

@Composable
fun AccountCard(item: AccountWithBalance, onIntent: (AccountsIntent) -> Unit) {
    val language = LocalAppLanguage.current
    val account = item.account
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().clickable { onIntent(AccountsIntent.EditAccountClicked(item)) },
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ColorDot(account.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary, size = 14)
                Column(Modifier.weight(1f)) {
                    Text(account.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        account.currency.code,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    item.balance.format(language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Row {
                TextButton(onClick = { onIntent(AccountsIntent.TopUpClicked(item)) }) {
                    Text("+ " + stringResource(Res.string.accounts_top_up))
                }
                TextButton(onClick = { onIntent(AccountsIntent.WithdrawClicked(item)) }) {
                    Text("− " + stringResource(Res.string.accounts_withdraw))
                }
            }
        }
    }
}
