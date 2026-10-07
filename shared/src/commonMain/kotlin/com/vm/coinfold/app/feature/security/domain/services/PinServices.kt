package com.vm.coinfold.app.feature.security.domain.services

import kotlin.random.Random

/** The PIN is exactly this many digits; entering the last one submits it. */
const val PIN_LENGTH = 4

/** Number of hashing rounds; slows down guessing a stolen hash a little without being noticeable to the user. */
private const val HASH_ROUNDS = 20_000

/** Random per-device value mixed into the hash so equal PINs do not give equal hashes. */
fun newPinSalt(random: Random = Random.Default): String = random.nextBytes(16).toHex()

/** Salted, iterated SHA-256 of the PIN as hex; the PIN itself is never stored. */
fun hashPin(pin: String, saltHex: String): String {
    val salt = saltHex.hexToBytes()
    val secret = pin.encodeToByteArray()
    var digest = Sha256.hash(salt + secret)
    repeat(HASH_ROUNDS - 1) { digest = Sha256.hash(digest + salt + secret) }
    return digest.toHex()
}

/** Compares without stopping at the first difference, so timing does not reveal how much matched. */
fun constantTimeEquals(a: String, b: String): Boolean {
    if (a.length != b.length) return false
    var diff = 0
    for (i in a.indices) diff = diff or (a[i].code xor b[i].code)
    return diff == 0
}

fun isValidPin(pin: String): Boolean = pin.length == PIN_LENGTH && pin.all { it in '0'..'9' }

/**
 * How long to block PIN entry after wrong guesses: nothing for the first four, then 30 seconds after every
 * fifth consecutive failure, 5 minutes from the tenth and an hour from the fifteenth.
 */
fun lockoutMillisAfter(failedAttempts: Int): Long = when {
    failedAttempts <= 0 || failedAttempts % 5 != 0 -> 0L
    failedAttempts >= 15 -> 60 * 60_000L
    failedAttempts >= 10 -> 5 * 60_000L
    else -> 30_000L
}
