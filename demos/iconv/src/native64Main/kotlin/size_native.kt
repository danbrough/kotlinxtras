import platform.posix.size_t

actual fun Long.toSizeT(): size_t =  toULong()
actual fun Int.toSizeT(): size_t =  toULong()
