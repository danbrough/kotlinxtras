package org.danbrough.kotlinxtras

import platform.posix.size_t

expect fun Int.toSizeT(): size_t
expect fun Long.toSizeT(): size_t

