package com.vm.coinfold.app.feature.accounts.domain.services

import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.feature.accounts.domain.models.TotalBalance
import com.vm.coinfold.app.feature.currency.domain.models.RateTable
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Money

fun calculateTotalBalance(
    accounts: List<AccountWithBalance>,
    target: Currency,
    rates: RateTable,
): TotalBalance {
    var total = Money.zero(target)
    var missing = false
    for (item in accounts) {
        val converted = rates.convert(item.balance, target)
        if (converted == null) missing = true else total += converted.money
    }
    return TotalBalance(total, missing)
}
