package com.vm.coinfold.app.feature.security.domain.models

/** Result of checking an entered PIN. */
sealed interface PinCheck {
    data object Correct : PinCheck

    /** Wrong PIN; [attemptsLeft] more wrong tries are allowed before entry is blocked for a while. */
    data class Wrong(val attemptsLeft: Int) : PinCheck

    /** Too many wrong tries: nothing is checked until [untilMillis] (UTC epoch millis). */
    data class LockedOut(val untilMillis: Long) : PinCheck
}

/** [LOADING] lasts only until the stored settings are read, so no content is shown before the lock decision. */
enum class LockStatus { LOADING, LOCKED, UNLOCKED }
