// empty.
pluginManagement {

    repositories {
        maven("/usr/local/kotlinxtras/build/xtras/maven")


        maven("https://s01.oss.sonatype.org/content/groups/staging")
        mavenCentral()

        gradlePluginPortal()

        google()
    }


}

dependencyResolutionManagement {
    versionCatalogs {
        // declares an additional catalog, named 'testLibs', from the 'test-libs.versions.toml' file
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

