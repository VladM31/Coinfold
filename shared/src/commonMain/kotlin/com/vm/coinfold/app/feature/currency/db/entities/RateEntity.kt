package com.vm.coinfold.app.feature.currency.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Cached exchange rate; [pair] looks like "USD_UAH", [rate] is a decimal stored as TEXT. */
@Entity(tableName = "rates")
data class RateEntity(
    @PrimaryKey val pair: String,
    val rate: String,
    /** UTC epoch millis of the last successful refresh. */
    val updatedAt: Long,
)
