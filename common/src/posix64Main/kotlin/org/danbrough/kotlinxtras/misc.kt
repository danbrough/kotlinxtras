package org.danbrough.kotlinxtras
import platform.posix.size_t

actual fun Int.toSizeT(): size_t = toULong()
actual fun Long.toSizeT(): size_t= toULong()
