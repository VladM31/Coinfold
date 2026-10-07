package com.vm.coinfold.app.feature.accounts.domain.models

import com.vm.coinfold.app.shared.domain.models.Money

/** [hasMissingRates] is true when some accounts could not be converted and are left out of [money]. */
data class TotalBalance(val money: Money, val hasMissingRates: Boolean)
