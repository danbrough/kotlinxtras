
dependencyResolutionManagement {
  versionCatalogs {
    val libs by creating {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}
