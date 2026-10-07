package com.vm.coinfold.app.feature.currency.domain.usecases

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.currency.domain.repositories.CurrencyRepository
import com.vm.coinfold.app.shared.domain.models.Currency
import kotlinx.coroutines.flow.first

/** Current rate for 1 [from] in [to]; null means no rate has ever been loaded (cross-currency ops are blocked). */
class GetRateUseCase(private val repository: CurrencyRepository) {
    suspend operator fun invoke(from: Currency, to: Currency): BigDecimal? =
        repository.rateTable.first().find(from, to)
}
