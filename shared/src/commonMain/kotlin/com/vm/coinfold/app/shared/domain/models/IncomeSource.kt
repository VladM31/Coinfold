package com.vm.coinfold.app.shared.domain.models

/** Where an income came from: a built-in preset (localized in the UI) or a name typed by the user. */
sealed interface IncomeSource {
    enum class Preset(val key: String) : IncomeSource {
        SALARY("salary"),
        DEBT_RETURN("debt_return"),
        GIFT("gift"),
        OTHER("other"),
    }

    data class Custom(val name: String) : IncomeSource

    /** Value stored in the database. */
    fun toStored(): String = when (this) {
        is Preset -> PRESET_PREFIX + key
        is Custom -> name
    }

    companion object {
        private const val PRESET_PREFIX = "preset:"

        fun fromStored(value: String): IncomeSource =
            if (value.startsWith(PRESET_PREFIX)) {
                val key = value.removePrefix(PRESET_PREFIX)
                Preset.entries.firstOrNull { it.key == key } ?: Preset.OTHER
            } else {
                Custom(value)
            }
    }
}
