package com.vm.coinfold.app.feature.currency.net.models

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.shared.domain.models.Currency

/** 1 unit of [from] costs [rate] units of [to]. */
data class Rate(val from: Currency, val to: Currency, val rate: BigDecimal)
