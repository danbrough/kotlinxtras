/*
* From the ktor project.
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/
package org.danbrough.utils.io

private val digits = "0123456789abcdef".toCharArray()

/**
 * Encode [bytes] as a HEX string with no spaces, newlines and `0x` prefixes.
 */
fun hex(bytes: ByteArray): String {
  val result = CharArray(bytes.size * 2)
  var resultIndex = 0
  val digits = digits

  for (element in bytes) {
    val b = element.toInt() and 0xff
    result[resultIndex++] = digits[b shr 4]
    result[resultIndex++] = digits[b and 0x0f]
  }

  return result.concatToString()
}


fun hex(s: String): ByteArray {
  val result = ByteArray(s.length / 2)
  for (idx in result.indices) {
    val srcIdx = idx * 2
    val high = s[srcIdx].toString().toInt(16) shl 4
    val low = s[srcIdx + 1].toString().toInt(16)
    result[idx] = (high or low).toByte()
  }

  return result
}


fun ByteArray.toHex(): String = hex(this)

fun String.fromHex(): ByteArray = hex(this)
