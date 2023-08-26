package org.danbrough.kotlinxtras.source

import org.danbrough.kotlinxtras.XtrasDSLMarker
import org.danbrough.kotlinxtras.library.XtrasLibrary

data class GitSourceConfig(val url: String, val commit: String? = null, val tag: String? = null) :
  XtrasLibrary.SourceConfig

@XtrasDSLMarker
fun XtrasLibrary.gitSource(url: String, commit: String? = null, tag: String? = null) {

  sourceConfig = GitSourceConfig(url, commit, tag)
}