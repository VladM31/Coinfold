package com.vm.coinfold.app.feature.currency.main

import com.vm.coinfold.app.shared.domain.Currency
import com.vm.coinfold.app.shared.domain.Money
import kotlinx.coroutines.flow.first

/** Public entry point for other features; returns null when the needed rate is unknown. */
class ConvertMoneyUseCase(private val repository: CurrencyRepository) {
    suspend operator fun invoke(money: Money, target: Currency): Conversion? =
        repository.rateTable.first().convert(money, target)
}
