package com.vm.coinfold.app.shared.launch

import com.vm.coinfold.app.feature.transactions.domain.models.TransactionFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A mailbox for "show me these transactions": another screen (e.g. the overview) posts a filter and opens the
 * transactions tab, which applies it and consumes the request.
 */
object TransactionFilterRequests {
    private val _pending = MutableStateFlow<TransactionFilter?>(null)
    val pending: StateFlow<TransactionFilter?> = _pending.asStateFlow()

    fun post(filter: TransactionFilter) {
        _pending.value = filter
    }

    fun consume(filter: TransactionFilter) {
        if (_pending.value == filter) _pending.value = null
    }
}
