package com.vm.coinfold.app.shared.domain.models

enum class Currency(val code: String, val isoNumeric: Int, val symbol: String, val fractionDigits: Int = 2) {
    UAH("UAH", 980, "₴"),
    USD("USD", 840, "$"),
    EUR("EUR", 978, "€");

    companion object {
        fun fromCode(code: String): Currency = entries.first { it.code == code }
    }
}
