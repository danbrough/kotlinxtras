import org.danbrough.kotlinxtras.enableCurl
import org.danbrough.kotlinxtras.enableIconv
import org.danbrough.kotlinxtras.enableOpenssl
import org.danbrough.kotlinxtras.enableSqlite

plugins {
  // `kotlin-dsl`
  //kotlin("multiplatform")
  xtras("sonatype", Xtras.version)
  xtras("core", Xtras.version)
}


enableIconv {
}

enableOpenssl {
}

enableCurl {
}

enableSqlite {
}
/*
gradlePlugin {
  isAutomatedPublishing = false
}*/



afterEvaluate {
  tasks.create("thang") {
    doFirst {
      println("RUNNING THANG!!!")
    }
    actions.add {
      tasks.withType(Sign::class.java) {
        println("TASK: $name type: ${this::class.java}")
      }
    }
  }
}