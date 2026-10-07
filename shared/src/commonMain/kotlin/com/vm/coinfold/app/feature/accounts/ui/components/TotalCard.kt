package com.vm.coinfold.app.feature.accounts.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.accounts_rates_missing
import coinfold.shared.generated.resources.accounts_total_balance
import com.vm.coinfold.app.feature.accounts.domain.viewmodels.AccountsIntent
import com.vm.coinfold.app.feature.accounts.domain.viewmodels.AccountsState
import com.vm.coinfold.app.shared.ui.components.CurrencySelector
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.utils.format
import org.jetbrains.compose.resources.stringResource

@Composable
fun TotalCard(state: AccountsState, onIntent: (AccountsIntent) -> Unit) {
    val language = LocalAppLanguage.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(Res.string.accounts_total_balance),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = state.total?.money?.format(language) ?: "",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (state.total?.hasMissingRates == true) {
                Text(
                    stringResource(Res.string.accounts_rates_missing),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            CurrencySelector(
                selected = state.mainCurrency,
                onSelected = { onIntent(AccountsIntent.MainCurrencySelected(it)) },
            )
        }
    }
}
