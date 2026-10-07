package com.vm.coinfold.app.feature.currency.domain.repositories.impls

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.currency.db.daos.RateDao
import com.vm.coinfold.app.feature.currency.db.entities.RateEntity
import com.vm.coinfold.app.feature.currency.domain.models.RateTable
import com.vm.coinfold.app.feature.currency.domain.repositories.CurrencyRepository
import com.vm.coinfold.app.feature.currency.net.clients.CurrencyClient
import com.vm.coinfold.app.shared.domain.models.Currency
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CurrencyRepositoryImpl(
    private val client: CurrencyClient,
    private val dao: RateDao,
    private val nowMillis: () -> Long = ::systemNowMillis,
) : CurrencyRepository {

    private val mutex = Mutex()
    private var lastAttempt: Long? = null

    override val rateTable: Flow<RateTable> = dao.observeAll().map { rows ->
        val parsed = buildMap {
            for (row in rows) {
                val (from, to) = parsePair(row.pair) ?: continue
                put(from to to, BigDecimal.parseString(row.rate))
            }
        }
        RateTable(parsed)
    }

    override val updatedAt: Flow<Long?> = dao.observeAll().map { rows -> rows.maxOfOrNull { it.updatedAt } }

    override suspend fun refresh(): Boolean = mutex.withLock {
        val now = nowMillis()
        val last = lastAttempt ?: updatedAt.first()
        if (last != null && now - last < MIN_INTERVAL_MS) return false
        lastAttempt = now
        try {
            val rates = client.getRates()
            if (rates.isEmpty()) return false
            dao.upsertAll(rates.map { RateEntity("${it.from.code}_${it.to.code}", it.rate.toPlainString(), now) })
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }

    private fun parsePair(pair: String): Pair<Currency, Currency>? {
        val parts = pair.split('_')
        if (parts.size != 2) return null
        val from = Currency.entries.firstOrNull { it.code == parts[0] } ?: return null
        val to = Currency.entries.firstOrNull { it.code == parts[1] } ?: return null
        return from to to
    }

    private companion object {
        const val MIN_INTERVAL_MS = 5 * 60 * 1000L
    }
}

@OptIn(ExperimentalTime::class)
private fun systemNowMillis(): Long = Clock.System.now().toEpochMilliseconds()
