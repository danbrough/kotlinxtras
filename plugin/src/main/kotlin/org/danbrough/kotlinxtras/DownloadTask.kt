@file:Suppress("unused")

package org.danbrough.kotlinxtras

import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import java.io.File

fun Project.createDownloadTask(name:String, url:String, destDir: File): TaskProvider<Exec> = tasks.register(name,Exec::class.java){
  commandLine("wget","-c",url,"-P",destDir.absolutePath)
  outputs.file(destDir.resolve(url.substringAfterLast('/')).also{
    println("OUTPUT FILE: $it")
  })

}