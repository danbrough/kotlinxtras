import platform.posix.size_t

actual fun Number.toSizeT(): size_t =  toLong().toULong()
