package com.vm.coinfold.app.shared.db

import androidx.room.TypeConverter
import com.vm.coinfold.app.shared.domain.Currency
import com.vm.coinfold.app.shared.domain.TransactionType

/** Enums are stored as TEXT; money is stored as two plain columns (minor units + currency code). */
class Converters {
    @TypeConverter
    fun currencyToString(value: Currency): String = value.code

    @TypeConverter
    fun stringToCurrency(value: String): Currency = Currency.fromCode(value)

    @TypeConverter
    fun typeToString(value: TransactionType): String = value.name

    @TypeConverter
    fun stringToType(value: String): TransactionType = TransactionType.valueOf(value)
}
