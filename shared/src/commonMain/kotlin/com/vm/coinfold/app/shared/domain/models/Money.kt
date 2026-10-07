package com.vm.coinfold.app.shared.domain.models

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode

/** Amount in minor units (cents/kopecks), so no floating point error can creep in. */
data class Money(val minorUnits: Long, val currency: Currency) {

    operator fun plus(other: Money): Money {
        requireSameCurrency(other)
        return copy(minorUnits = minorUnits + other.minorUnits)
    }

    operator fun minus(other: Money): Money {
        requireSameCurrency(other)
        return copy(minorUnits = minorUnits - other.minorUnits)
    }

    operator fun unaryMinus(): Money = copy(minorUnits = -minorUnits)

    /** Converts using [rate] = how many units of [target] one unit of this currency costs. */
    fun convertTo(target: Currency, rate: BigDecimal): Money {
        if (target == currency) return this
        val converted = BigDecimal.fromLong(minorUnits).multiply(rate)
        return Money(converted.roundHalfUp().longValue(exactRequired = false), target)
    }

    fun toBigDecimal(): BigDecimal =
        BigDecimal.fromLong(minorUnits).moveDecimalPoint(-currency.fractionDigits)

    private fun requireSameCurrency(other: Money) =
        require(currency == other.currency) { "Currency mismatch: $currency vs ${other.currency}" }

    companion object {
        fun zero(currency: Currency) = Money(0, currency)

        /** Rounds HALF_UP to the currency's minor unit. */
        fun of(amount: BigDecimal, currency: Currency): Money {
            val minor = amount.moveDecimalPoint(currency.fractionDigits).roundHalfUp()
            return Money(minor.longValue(exactRequired = false), currency)
        }
    }
}

// HALF_UP in the java.math sense: ties go away from zero.
private fun BigDecimal.roundHalfUp(): BigDecimal =
    roundToDigitPositionAfterDecimalPoint(0, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
