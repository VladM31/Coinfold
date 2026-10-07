package com.vm.coinfold.app.feature.accounts.main

import com.vm.coinfold.app.feature.currency.main.RateTable
import com.vm.coinfold.app.shared.domain.Currency
import com.vm.coinfold.app.shared.domain.Money

/** [hasMissingRates] is true when some accounts could not be converted and are left out of [money]. */
data class TotalBalance(val money: Money, val hasMissingRates: Boolean)

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
