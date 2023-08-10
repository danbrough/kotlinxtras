/*
* From the ktor project.
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package org.danbrough.utils.io

import kotlin.experimental.and


private const val BASE64_ALPHABET =
  "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
private const val BASE64_MASK: Byte = 0x3f
private const val BASE64_MASK_INT: Int = 0x3f
private const val BASE64_PAD = '='
fun ByteArray.toBase64(): String {
  val array = this@toBase64
  var position = 0
  var writeOffset = 0
  val charArray = CharArray(size * 8 / 6 + 3)

  while (position + 3 <= array.size) {
    val first = array[position].toInt()
    val second = array[position + 1].toInt()
    val third = array[position + 2].toInt()
    position += 3

    val chunk = ((first and 0xFF) shl 16) or ((second and 0xFF) shl 8) or (third and 0xFF)
    for (index in 3 downTo 0) {
      val char = (chunk shr (6 * index)) and BASE64_MASK_INT
      charArray[writeOffset++] = (char.toBase64())
    }
  }

  val remaining = array.size - position
  if (remaining == 0) return charArray.concatToString(0, writeOffset)

  val chunk = if (remaining == 1) {
    ((array[position].toInt() and 0xFF) shl 16) or ((0 and 0xFF) shl 8) or (0 and 0xFF)
  } else {
    ((array[position].toInt() and 0xFF) shl 16) or ((array[position + 1].toInt() and 0xFF) shl 8) or (0 and 0xFF)
  }

  val padSize = (3 - remaining) * 8 / 6
  for (index in 3 downTo padSize) {
    val char = (chunk shr (6 * index)) and BASE64_MASK_INT
    charArray[writeOffset++] = char.toBase64()
  }

  repeat(padSize) { charArray[writeOffset++] = BASE64_PAD }

  return charArray.concatToString(0, writeOffset)
}


fun String.fromBase64(): ByteArray {
  val bytes = dropLastWhile { it == BASE64_PAD }.encodeToByteArray()
  val bytesLength = bytes.size
  val data = ByteArray(4)
  val output = mutableListOf<Byte>()

  for (n in 0 until bytesLength step 4) {
    var endIndex = n + 4
    if (endIndex > bytesLength) endIndex = bytesLength
    bytes.copyInto(data, 0, n, endIndex)
    val read = endIndex - n

    val chunk = data.foldIndexed(0) { index, result, current ->
      result or (current.fromBase64().toInt() shl ((3 - index) * 6))
    }


    for (index in data.size - 2 downTo (data.size - read)) {
      val origin = (chunk shr (8 * index)) and 0xff
      output.add(origin.toByte())
    }
  }

  return output.toByteArray()
}

internal inline fun Int.toBase64(): Char = BASE64_ALPHABET[this]

private val BASE64_INVERSE_ALPHABET = IntArray(256) {
  BASE64_ALPHABET.indexOf(it.toChar())
}

internal inline fun Byte.fromBase64(): Byte =
  BASE64_INVERSE_ALPHABET[toInt() and 0xff].toByte() and BASE64_MASK