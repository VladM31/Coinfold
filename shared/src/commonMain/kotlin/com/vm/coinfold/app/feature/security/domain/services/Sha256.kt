package com.vm.coinfold.app.feature.security.domain.services

/**
 * SHA-256 in plain Kotlin, so the PIN hash is identical on every platform and needs no platform crypto API.
 * Verified against the standard test vectors in the unit tests.
 */
object Sha256 {
    // First 32 bits of the fractional parts of the cube roots of the first 64 primes.
    private val K = longArrayOf(
        0x428a2f98L, 0x71374491L, 0xb5c0fbcfL, 0xe9b5dba5L,
        0x3956c25bL, 0x59f111f1L, 0x923f82a4L, 0xab1c5ed5L,
        0xd807aa98L, 0x12835b01L, 0x243185beL, 0x550c7dc3L,
        0x72be5d74L, 0x80deb1feL, 0x9bdc06a7L, 0xc19bf174L,
        0xe49b69c1L, 0xefbe4786L, 0x0fc19dc6L, 0x240ca1ccL,
        0x2de92c6fL, 0x4a7484aaL, 0x5cb0a9dcL, 0x76f988daL,
        0x983e5152L, 0xa831c66dL, 0xb00327c8L, 0xbf597fc7L,
        0xc6e00bf3L, 0xd5a79147L, 0x06ca6351L, 0x14292967L,
        0x27b70a85L, 0x2e1b2138L, 0x4d2c6dfcL, 0x53380d13L,
        0x650a7354L, 0x766a0abbL, 0x81c2c92eL, 0x92722c85L,
        0xa2bfe8a1L, 0xa81a664bL, 0xc24b8b70L, 0xc76c51a3L,
        0xd192e819L, 0xd6990624L, 0xf40e3585L, 0x106aa070L,
        0x19a4c116L, 0x1e376c08L, 0x2748774cL, 0x34b0bcb5L,
        0x391c0cb3L, 0x4ed8aa4aL, 0x5b9cca4fL, 0x682e6ff3L,
        0x748f82eeL, 0x78a5636fL, 0x84c87814L, 0x8cc70208L,
        0x90befffaL, 0xa4506cebL, 0xbef9a3f7L, 0xc67178f2L,
    ).map { it.toInt() }.toIntArray()

    // First 32 bits of the fractional parts of the square roots of the first 8 primes.
    private val INITIAL = longArrayOf(
        0x6a09e667L, 0xbb67ae85L, 0x3c6ef372L, 0xa54ff53aL,
        0x510e527fL, 0x9b05688cL, 0x1f83d9abL, 0x5be0cd19L,
    ).map { it.toInt() }

    fun hash(data: ByteArray): ByteArray {
        var h0 = INITIAL[0]
        var h1 = INITIAL[1]
        var h2 = INITIAL[2]
        var h3 = INITIAL[3]
        var h4 = INITIAL[4]
        var h5 = INITIAL[5]
        var h6 = INITIAL[6]
        var h7 = INITIAL[7]

        // Padding: 0x80, zeros, then the message length in bits as a 64-bit big-endian number.
        val bitLength = data.size.toLong() * 8
        val paddedLength = ((data.size + 9 + 63) / 64) * 64
        val padded = ByteArray(paddedLength)
        data.copyInto(padded)
        padded[data.size] = 0x80.toByte()
        for (i in 0 until 8) padded[paddedLength - 1 - i] = (bitLength ushr (8 * i)).toByte()

        val w = IntArray(64)
        for (block in 0 until paddedLength step 64) {
            for (t in 0 until 16) {
                val i = block + t * 4
                w[t] = ((padded[i].toInt() and 0xff) shl 24) or ((padded[i + 1].toInt() and 0xff) shl 16) or
                    ((padded[i + 2].toInt() and 0xff) shl 8) or (padded[i + 3].toInt() and 0xff)
            }
            for (t in 16 until 64) {
                val s0 = w[t - 15].rotr(7) xor w[t - 15].rotr(18) xor (w[t - 15] ushr 3)
                val s1 = w[t - 2].rotr(17) xor w[t - 2].rotr(19) xor (w[t - 2] ushr 10)
                w[t] = w[t - 16] + s0 + w[t - 7] + s1
            }
            var a = h0; var b = h1; var c = h2; var d = h3
            var e = h4; var f = h5; var g = h6; var h = h7
            for (t in 0 until 64) {
                val s1 = e.rotr(6) xor e.rotr(11) xor e.rotr(25)
                val ch = (e and f) xor (e.inv() and g)
                val temp1 = h + s1 + ch + K[t] + w[t]
                val s0 = a.rotr(2) xor a.rotr(13) xor a.rotr(22)
                val maj = (a and b) xor (a and c) xor (b and c)
                val temp2 = s0 + maj
                h = g; g = f; f = e; e = d + temp1
                d = c; c = b; b = a; a = temp1 + temp2
            }
            h0 += a; h1 += b; h2 += c; h3 += d; h4 += e; h5 += f; h6 += g; h7 += h
        }

        val out = ByteArray(32)
        intArrayOf(h0, h1, h2, h3, h4, h5, h6, h7).forEachIndexed { index, value ->
            for (i in 0 until 4) out[index * 4 + i] = (value ushr (24 - 8 * i)).toByte()
        }
        return out
    }

    private fun Int.rotr(bits: Int): Int = (this ushr bits) or (this shl (32 - bits))
}

fun ByteArray.toHex(): String = joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }

fun String.hexToBytes(): ByteArray = ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }
