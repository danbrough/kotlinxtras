package demo

import demo.db.HockeyPlayer
import klog.*
import kotlin.test.Test

val log by lazy {
  klog("demo") {
    level = Level.TRACE
    messageFormatter = KMessageFormatters.verbose.colored
    writer = KLogWriters.stdOut
  }
}

class Tests {

  @Test
  fun test1() {
    log.info("test1()")
    val db = createDatabase(DriverFactory())
    log.debug("got db: $db")

    val playerQueries = db.playerQueries

    println(playerQueries.selectAll().executeAsList())
// Prints [HockeyPlayer(15, "Ryan Getzlaf")]

    playerQueries.insert(player_number = 10, full_name = "Corey Perry")
    println(playerQueries.selectAll().executeAsList())
// Prints [HockeyPlayer(15, "Ryan Getzlaf"), HockeyPlayer(10, "Corey Perry")]

    val player = HockeyPlayer(10, "Ronald McDonald")
    playerQueries.insertFullPlayerObject(player)

  }
}