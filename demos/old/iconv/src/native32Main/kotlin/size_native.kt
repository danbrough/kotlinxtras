import platform.posix.size_t

actual fun Int.toSizeT(): size_t = toUInt()
actual fun Long.toSizeT(): size_t = toUInt()
