package com.vm.coinfold.app.feature.currency.domain.repositories.impls

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.currency.db.daos.RateDao
import com.vm.coinfold.app.feature.currency.db.entities.RateEntity
import com.vm.coinfold.app.feature.currency.domain.usecases.GetRateUseCase
import com.vm.coinfold.app.feature.currency.net.clients.CurrencyClient
import com.vm.coinfold.app.feature.currency.net.models.Rate
import com.vm.coinfold.app.shared.domain.models.Currency
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class CurrencyRepositoryTest {

    private class FakeDao : RateDao {
        val rows = MutableStateFlow<List<RateEntity>>(emptyList())
        override fun observeAll(): Flow<List<RateEntity>> = rows
        override suspend fun upsertAll(rates: List<RateEntity>) {
            rows.value = (rows.value.filter { old -> rates.none { it.pair == old.pair } }) + rates
        }
    }

    private class FakeClient(var fail: Boolean = false) : CurrencyClient {
        var calls = 0
        override suspend fun getRates(): List<Rate> {
            calls++
            if (fail) error("no network")
            return listOf(Rate(Currency.USD, Currency.UAH, BigDecimal.parseString("41")))
        }
    }

    private val minute = 60_000L

    @Test
    fun refreshStoresRatesAndThrottlesToFiveMinutes() = runTest {
        var now = 1_000_000L
        val client = FakeClient()
        val repo = CurrencyRepositoryImpl(client, FakeDao()) { now }

        assertTrue(repo.refresh())
        now += 4 * minute
        assertFalse(repo.refresh())
        assertEquals(1, client.calls)

        now += 2 * minute
        assertTrue(repo.refresh())
        assertEquals(2, client.calls)
        assertEquals(BigDecimal.parseString("41"), repo.rateTable.first().find(Currency.USD, Currency.UAH))
    }

    @Test
    fun networkErrorKeepsCache() = runTest {
        var now = 1_000_000L
        val client = FakeClient()
        val repo = CurrencyRepositoryImpl(client, FakeDao()) { now }
        repo.refresh()

        client.fail = true
        now += 10 * minute
        assertFalse(repo.refresh())
        assertEquals(BigDecimal.parseString("41"), repo.rateTable.first().find(Currency.USD, Currency.UAH))
        assertEquals(1_000_000L, repo.updatedAt.first())
    }

    @Test
    fun neverLoadedMeansNoRate() = runTest {
        val client = FakeClient(fail = true)
        val repo = CurrencyRepositoryImpl(client, FakeDao()) { 1L }
        assertFalse(repo.refresh())
        assertNull(repo.updatedAt.first())
        assertNull(GetRateUseCase(repo)(Currency.USD, Currency.UAH))
    }
}
