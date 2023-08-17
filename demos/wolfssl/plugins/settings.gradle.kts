// empty.


dependencyResolutionManagement {
    versionCatalogs {
        // declares an additional catalog, named 'testLibs', from the 'test-libs.versions.toml' file
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

