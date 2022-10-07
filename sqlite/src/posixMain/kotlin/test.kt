import libsqlite.*
import cnames.structs.sqlite3
import kotlinx.cinterop.*

fun test() {


  val db = memScoped {
    val dbPtr = alloc<CPointerVar<sqlite3>>()
    val sqlFlags = SQLITE_OPEN_READWRITE or SQLITE_OPEN_CREATE
    val openResult = sqlite3_open_v2(":memory", dbPtr.ptr, sqlFlags, null)
    if (openResult != SQLITE_OK) {
      val msg = sqlite3_errmsg(dbPtr.value)?.toKString() ?: "error in open"
      throw Error(msg)
    }
    dbPtr.value!!
  }
}